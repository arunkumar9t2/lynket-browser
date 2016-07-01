package arun.com.chromer.preferences.widgets;

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
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.preferences.manager.Preferences;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.Util;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class AppPreferenceCardView extends CardView {
    private static final int CUSTOM_TAB_PROVIDER = 0;
    private static final int SECONDARY_BROWSER = 1;
    private static final int FAVORITE_SHARE = 2;

    @BindView(R.id.app_preference_icon)
    public ImageView mIcon;
    @BindView(R.id.app_preference_category)
    public TextView mCategoryTextView;
    @BindView(R.id.app_preference_selection)
    public TextView mAppNameTextView;

    private Unbinder mUnBinder;

    private String mCategoryText;
    private String mAppName;
    private String mAppPackage;
    private int mPreferenceType;

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

        mPreferenceType = a.getInt(R.styleable.AppPreferenceCardView_preferenceType, 0);
        setInitialValues();
        a.recycle();

        addView(LayoutInflater.from(getContext()).inflate(R.layout.app_preference_cardview_content, this, false));
        mUnBinder = ButterKnife.bind(this);
    }

    private void setInitialValues() {
        switch (mPreferenceType) {
            case CUSTOM_TAB_PROVIDER:
                mCategoryText = getResources().getString(R.string.default_provider);
                final String customTabProvider = Preferences.customTabApp(getContext());
                if (customTabProvider != null) {
                    mAppName = Util.getAppNameWithPackage(getContext(), customTabProvider);
                    mAppPackage = customTabProvider;
                } else {
                    mAppName = getResources().getString(R.string.not_found);
                    mAppPackage = null;
                }
                break;
            case SECONDARY_BROWSER:
                mCategoryText = getResources().getString(R.string.choose_secondary_browser);
                final String secondaryBrowser = Preferences.secondaryBrowserPackage(getContext());
                if (secondaryBrowser != null) {
                    mAppName = Util.getAppNameWithPackage(getContext(), secondaryBrowser);
                    mAppPackage = secondaryBrowser;
                } else {
                    mAppName = getResources().getString(R.string.not_set);
                    mAppPackage = null;
                }
                break;
            case FAVORITE_SHARE:
                mCategoryText = getResources().getString(R.string.fav_share_app);
                final String favSharePackage = Preferences.favSharePackage(getContext());
                if (favSharePackage != null) {
                    mAppName = Util.getAppNameWithPackage(getContext(), favSharePackage);
                    mAppPackage = favSharePackage;
                } else {
                    mAppName = getResources().getString(R.string.not_set);
                    mAppPackage = null;
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
        mAppNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.material_dark_color));
        mCategoryTextView.setText(mCategoryText);
        mAppNameTextView.setText(mAppName);
        applyIcon();
    }

    private void applyIcon() {
        if (Util.isPackageInstalled(getContext(), mAppPackage)) {
            final PackageManager pm = getContext().getApplicationContext().getPackageManager();
            Drawable appIcon = null;
            try {
                ApplicationInfo ai = pm.getApplicationInfo(mAppPackage, 0);
                appIcon = ai.loadIcon(pm);
            } catch (PackageManager.NameNotFoundException e) {
                Timber.e("Failed to load icon for %s", mAppName);
            }
            if (appIcon != null) {
                setIconDrawable(appIcon, true);
                Palette.from(Util.drawableToBitmap(appIcon))
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
            mIcon.setScaleType(ImageView.ScaleType.CENTER);
            switch (mPreferenceType) {
                case CUSTOM_TAB_PROVIDER:
                    setIconDrawable(new IconicsDrawable(getContext())
                            .icon(CommunityMaterial.Icon.cmd_comment_alert_outline)
                            .colorRes(R.color.error)
                            .sizeDp(30), false);
                    mAppNameTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
                    break;
                case SECONDARY_BROWSER:
                    setIconDrawable(new IconicsDrawable(getContext())
                            .icon(GoogleMaterial.Icon.gmd_open_in_browser)
                            .colorRes(R.color.material_dark_light)
                            .sizeDp(30), false);
                    break;
                case FAVORITE_SHARE:
                    setIconDrawable(new IconicsDrawable(getContext())
                            .icon(GoogleMaterial.Icon.gmd_share)
                            .colorRes(R.color.material_dark_light)
                            .sizeDp(30), false);
                    break;
            }
        }
    }

    private void setIconDrawable(final Drawable newIconDrawable, final boolean overrideScaleType) {
        if (mIcon != null) {
            TransitionDrawable transitionDrawable = new TransitionDrawable(
                    new Drawable[]{
                            getCurrentIcon(),
                            newIconDrawable
                    });
            mIcon.setImageDrawable(transitionDrawable);
            if (mIcon.getScaleType() != ImageView.ScaleType.FIT_CENTER && overrideScaleType) {
                mIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
        return mIcon.getDrawable() == null ? new ColorDrawable(Color.TRANSPARENT) : mIcon.getDrawable();
    }

    public void updatePreference(@Nullable final ComponentName componentName) {
        final String flatComponent = componentName == null ? null : componentName.flattenToString();
        switch (mPreferenceType) {
            case CUSTOM_TAB_PROVIDER:
                if (componentName != null) {
                    Preferences.customTabApp(getContext(), componentName.getPackageName());
                }
                break;
            case SECONDARY_BROWSER:
                Preferences.secondaryBrowserComponent(getContext(), flatComponent);
                break;
            case FAVORITE_SHARE:
                Preferences.favShareComponent(getContext(), flatComponent);
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
        mUnBinder.unbind();
    }


}
