/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.browsing.article.util;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.EdgeEffect;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utilities we use, mostly for UI modification.
 */
public final class ArticleUtil {
  /**
   * Changes the overscroll highlight effect on a recyclerview to be the given color.
   */
  public static void changeRecyclerOverscrollColors(RecyclerView recyclerView, final int color) {
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      private boolean invoked = false;

      @Override
      @TargetApi(21)
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        // only invoke this once
        if (invoked) {
          return;
        } else {
          invoked = true;
        }

        try {
          final Class<?> clazz = RecyclerView.class;

          for (final String name : new String[]{"ensureTopGlow", "ensureBottomGlow"}) {
            Method method = clazz.getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(recyclerView);
          }

          for (final String name : new String[]{"mTopGlow", "mBottomGlow"}) {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            final Object edge = field.get(recyclerView);
            final Field fEdgeEffect = edge.getClass().getDeclaredField("mEdgeEffect");
            fEdgeEffect.setAccessible(true);
            ((EdgeEffect) fEdgeEffect.get(edge)).setColor(color);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Changes the progress bar's color.
   */
  public static void changeProgressBarColors(ProgressBar progressBar, int color) {
    progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
  }

  /**
   * Changes the text selection handle colors.
   */
  public static void changeTextSelectionHandleColors(TextView textView, int color) {
    textView.setHighlightColor(Color.argb(
      40, Color.red(color), Color.green(color), Color.blue(color)));

    try {
      Field editorField = TextView.class.getDeclaredField("mEditor");
      if (!editorField.isAccessible()) {
        editorField.setAccessible(true);
      }

      Object editor = editorField.get(textView);
      Class<?> editorClass = editor.getClass();

      String[] handleNames = {
        "mSelectHandleLeft",
        "mSelectHandleRight",
        "mSelectHandleCenter"
      };
      String[] resNames = {
        "mTextSelectHandleLeftRes",
        "mTextSelectHandleRightRes",
        "mTextSelectHandleRes"
      };

      for (int i = 0; i < handleNames.length; i++) {
        Field handleField = editorClass.getDeclaredField(handleNames[i]);
        if (!handleField.isAccessible()) {
          handleField.setAccessible(true);
        }

        Drawable handleDrawable = (Drawable) handleField.get(editor);

        if (handleDrawable == null) {
          Field resField = TextView.class.getDeclaredField(resNames[i]);
          if (!resField.isAccessible()) {
            resField.setAccessible(true);
          }
          int resId = resField.getInt(textView);
          handleDrawable = ContextCompat.getDrawable(textView.getContext(), resId);
        }

        if (handleDrawable != null) {
          Drawable drawable = handleDrawable.mutate();
          drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
          handleField.set(editor, drawable);
        }
      }
    } catch (Exception ignored) {
    }
  }
}
