package arun.com.chromer.activities.settings.preferences.widgets;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static android.widget.ImageView.ScaleType.CENTER;

public class AppPreferenceCardView extends CardView {
    private static final int CUSTOM_TAB_PROVIDER = 0;
    private static final int SECONDARY_BROWSER = 1;
    private static final int FAVORITE_SHARE = 2;

    @BindView(R.id.app_preference_icon)
    public ImageView icon;
    @BindView(R.id.app_preference_category)
    public TextView categoryTextView;
    @BindView(R.id.app_preference_selection)
    public TextView appNameTextView;

    private Unbinder unbinder;

    private String category;
    private String appName;
    private String appPackage;
    private int preferenceType;

    public AppPreferenceCardView(Context context) {
        super(context);
        init(null, 0);
    }

    public AppPreferenceCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AppPreferenceCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AppPreferenceCardView, defStyle, 0);
        if (!a.hasValue(R.styleable.AppPreferenceCardView_preferenceType)) {
            throw new IllegalArgumentException("Must specify app:preferenceType in xml");
        }

        preferenceType = a.getInt(R.styleable.AppPreferenceCardView_preferenceType, 0);
        setInitialValues();
        a.recycle();
        addView(LayoutInflater.from(getContext()).inflate(R.layout.app_preference_cardview_content, this, false));
        unbinder = ButterKnife.bind(this);
    }

    private void setInitialValues() {
        switch (preferenceType) {
            case CUSTOM_TAB_PROVIDER:
                category = getResources().getString(R.string.default_provider);
                final String customTabProvider = Preferences.get(getContext()).customTabApp();
                if (customTabProvider != null) {
                    appName = Utils.getAppNameWithPackage(getContext(), customTabProvider);
                    appPackage = customTabProvider;
                } else {
                    appName = getResources().getString(R.string.not_found);
                    appPackage = null;
                }
                break;
            case SECONDARY_BROWSER:
                category = getResources().getString(R.string.choose_secondary_browser);
                final String secondaryBrowser = Preferences.get(getContext()).secondaryBrowserPackage();
                if (secondaryBrowser != null) {
                    appName = Utils.getAppNameWithPackage(getContext(), secondaryBrowser);
                    appPackage = secondaryBrowser;
                } else {
                    appName = getResources().getString(R.string.not_set);
                    appPackage = null;
                }
                break;
            case FAVORITE_SHARE:
                category = getResources().getString(R.string.fav_share_app);
                final String favSharePackage = Preferences.get(getContext()).favSharePackage();
                if (favSharePackage != null) {
                    appName = Utils.getAppNameWithPackage(getContext(), favSharePackage);
                    appPackage = favSharePackage;
                } else {
                    appName = getResources().getString(R.string.not_set);
                    appPackage = null;
                }
                break;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateUI();
    }

    private void updateUI() {
        appNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.material_dark_color));
        categoryTextView.setText(category);
        appNameTextView.setText(appName);
        applyIcon();
    }

    private void applyIcon() {
        if (Utils.isPackageInstalled(getContext(), appPackage)) {
            final PackageManager pm = getContext().getApplicationContext().getPackageManager();
            Drawable appIcon = null;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(appPackage, 0);
                appIcon = ai.loadIcon(pm);
            } catch (PackageManager.NameNotFoundException e) {
                Timber.e("Failed to load icon for %s", appName);
            }
            if (appIcon != null) {
                setIconDrawable(appIcon, true);
                Palette.from(Utils.drawableToBitmap(appIcon))
                        .clearFilters()
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                int bestColor = ColorUtil.getBestColorFromPalette(palette);
                                Drawable foreground = ColorUtil.getRippleDrawableCompat(bestColor);
                                // Bug in SDK requires redundant cast
                                //noinspection RedundantCast
                                ((FrameLayout) AppPreferenceCardView.this).setForeground(foreground);
                            }
                        });
            }
        } else {
            icon.setScaleType(CENTER);
            switch (preferenceType) {
                case CUSTOM_TAB_PROVIDER:
                    setIconDrawable(new IconicsDrawable(getContext())
                            .icon(CommunityMaterial.Icon.cmd_comment_alert_outline)
                            .colorRes(R.color.error)
                            .sizeDp(30), false);
                    appNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
                    break;
                case SECONDARY_BROWSER:
                    setIconDrawable(new IconicsDrawable(getContext())
                            .icon(CommunityMaterial.Icon.cmd_open_in_app)
                            .colorRes(R.color.material_dark_light)
                            .sizeDp(30), false);
                    break;
                case FAVORITE_SHARE:
                    setIconDrawable(new IconicsDrawable(getContext())
                            .icon(CommunityMaterial.Icon.cmd_share_variant)
                            .colorRes(R.color.material_dark_light)
                            .sizeDp(30), false);
                    break;
            }
        }
    }

    private void setIconDrawable(final Drawable newIconDrawable, final boolean overrideScaleType) {
        if (icon != null) {
            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{
                            getCurrentIcon(),
                            newIconDrawable
                    });
            icon.setImageDrawable(transitionDrawable);
            if (icon.getScaleType() != ImageView.ScaleType.FIT_CENTER && overrideScaleType) {
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            transitionDrawable.setCrossFadeEnabled(true);
            transitionDrawable.startTransition(300);
        }
    }

    /**
     * Returns the current icon present in the view or returns an empty icon
     *
     * @return Icon drawable
     */
    @NonNull
    private Drawable getCurrentIcon() {
        return icon.getDrawable() == null ? new ColorDrawable(Color.TRANSPARENT) : icon.getDrawable();
    }

    public void updatePreference(@Nullable final ComponentName componentName) {
        final String flatComponent = componentName == null ? null : componentName.flattenToString();
        switch (preferenceType) {
            case CUSTOM_TAB_PROVIDER:
                if (componentName != null) {
                    Preferences.get(getContext()).customTabApp(componentName.getPackageName());
                }
                break;
            case SECONDARY_BROWSER:
                Preferences.get(getContext()).secondaryBrowserComponent(flatComponent);
                break;
            case FAVORITE_SHARE:
                Preferences.get(getContext()).favShareComponent(flatComponent);
                break;
        }
        refreshState();
    }

    private void refreshState() {
        setInitialValues();
        updateUI();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbinder.unbind();
    }


}
