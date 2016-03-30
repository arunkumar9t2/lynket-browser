package arun.com.chromer.about.changelog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.xml.sax.XMLReader;

import java.util.Stack;

import arun.com.chromer.BuildConfig;
import arun.com.chromer.R;

/**
 * Created by Arun on 25/12/2015.
 */
public class ChangelogUtil {
    public static void showChangelogDialog(Activity activity) {
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .customView(R.layout.changelog_text, true)
                .title("Changelog")
                .positiveText("Cool!")
                .build();
        TextView textView = (TextView) dialog.getCustomView();
        if (textView != null) {
            textView.setText(
                    Html.fromHtml(
                            activity.getResources().getString(R.string.changelog_text),
                            null,
                            new MyTagHandler()
                    ));
        }
        dialog.show();
    }

    public static boolean shouldShowChangelog(Context context) {
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(),
                Context.MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return false;
        } else if (savedVersionCode == DOESNT_EXIST) {
            // This is a new install (or the user cleared the shared preferences)
            return true;
        } else if (currentVersionCode > savedVersionCode) {
            // This is an upgrade
            return true;
        }
        return false;
    }
}

class MyTagHandler implements Html.TagHandler {
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);
    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    private final Stack<String> lists = new Stack<>();
    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    private final Stack<Integer> olNextIndex = new Stack<>();

    /**
     * @see android.text.Html
     */
    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
    }

    /**
     * Modified from {@link android.text.Html}
     */
    private static void end(Editable text, Class<?> kind, Object... replaces) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);
        text.removeSpan(obj);
        if (where != len) {
            for (Object replace : replaces) {
                text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * @see android.text.Html
     */
    private static Object getLast(Spanned text, Class<?> kind) {
        /*
         * This knows that the last returned object from getSpans()
		 * will be the most recently added.
		 */
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        }
        return objs[objs.length - 1];
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("ul")) {
            if (opening) {
                lists.push(tag);
            } else {
                lists.pop();
            }
        } else if (tag.equalsIgnoreCase("ol")) {
            if (opening) {
                lists.push(tag);
                olNextIndex.push(1).toString();//TODO: add support for lists starting other index than 1
            } else {
                lists.pop();
                olNextIndex.pop().toString();
            }
        } else if (tag.equalsIgnoreCase("li")) {
            if (opening) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                String parentList = lists.peek();
                if (parentList.equalsIgnoreCase("ol")) {
                    start(output, new Ol());
                    output.append(olNextIndex.peek().toString()).append(". ");
                    olNextIndex.push(olNextIndex.pop() + 1);
                } else if (parentList.equalsIgnoreCase("ul")) {
                    start(output, new Ul());
                }
            } else {
                if (lists.peek().equalsIgnoreCase("ul")) {
                    if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                        output.append("\n");
                    }
                    // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                    int bulletMargin = indent;
                    if (lists.size() > 1) {
                        bulletMargin = indent - bullet.getLeadingMargin(true);
                        if (lists.size() > 2) {
                            // This get's more complicated when we add a LeadingMarginSpan into the same line:
                            // we have also counter it's effect to BulletSpan
                            bulletMargin -= (lists.size() - 2) * listItemIndent;
                        }
                    }
                    BulletSpan newBullet = new BulletSpan(bulletMargin);
                    end(output,
                            Ul.class,
                            new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                            newBullet);
                } else if (lists.peek().equalsIgnoreCase("ol")) {
                    if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                        output.append("\n");
                    }
                    int numberMargin = listItemIndent * (lists.size() - 1);
                    if (lists.size() > 2) {
                        // Same as in ordered lists: counter the effect of nested Spans
                        numberMargin -= (lists.size() - 2) * listItemIndent;
                    }
                    end(output,
                            Ol.class,
                            new LeadingMarginSpan.Standard(numberMargin));
                }
            }
        } else {
            if (opening) Log.d("TagHandler", "Found an unsupported tag " + tag);
        }
    }

    private static class Ul {
    }

    private static class Ol {
    }

}