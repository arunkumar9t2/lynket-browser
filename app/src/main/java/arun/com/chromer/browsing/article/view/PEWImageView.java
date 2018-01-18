/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
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

package arun.com.chromer.browsing.article.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A parallax image view that reverses the Y direction animation by default.
 */
public class PEWImageView extends com.fmsirvent.ParallaxEverywhere.PEWImageView {
    public PEWImageView(Context context) {
        super(context);
        init();
    }

    public PEWImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PEWImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setReverseY(true);
    }
}
