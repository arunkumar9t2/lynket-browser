/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.bubbles.webheads.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import arun.com.chromer.R;
import arun.com.chromer.util.Utils;

/**
 * Circle view that draws bitmap shadow layers on pre L and system drop shadow on post L systems.
 */
public class ElevatedCircleView extends CircleView {

    public ElevatedCircleView(Context context) {
        this(context, null, 0);
    }

    public ElevatedCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElevatedCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!Utils.isLollipopAbove()) {
            float shadowR = context.getResources().getDimension(R.dimen.web_head_shadow_radius);
            float shadowDx = context.getResources().getDimension(R.dimen.web_head_shadow_dx);
            float shadowDy = context.getResources().getDimension(R.dimen.web_head_shadow_dy);
            mBgPaint.setShadowLayer(shadowR, shadowDx, shadowDy, 0x55000000);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Utils.isLollipopAbove()) {
            setOutlineProvider(new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    int shapeSize = getMeasuredWidth();
                    outline.setRoundRect(0, 0, shapeSize, shapeSize, shapeSize / 2);
                }
            });
            setClipToOutline(true);
        }
    }

    /**
     * Use only on pre L devices. For post L use {@link androidx.core.view.ViewCompat#setElevation(View, float)}.
     * No op when called for post L devices.
     */
    public void clearElevation() {
        if (!Utils.isLollipopAbove()) {
            mBgPaint.clearShadowLayer();
            invalidate();
        }
    }
}
