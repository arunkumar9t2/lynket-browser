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

package arun.com.chromer.search;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Arun on 03/08/2016.
 */
public class SuggestionItem {
    public static final int COPY = -1;
    public static final int GOOGLE = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COPY, GOOGLE})
    public @interface SuggestionType {
    }

    public String suggestion = "";
    @SuggestionType
    public int type;

    public SuggestionItem() {

    }

    @SuppressWarnings("SameParameterValue")
    public SuggestionItem(@Nullable String suggestion, @SuggestionType int type) {
        if (suggestion != null) {
            this.suggestion = suggestion;
        }
        this.type = type;
    }

    public SuggestionItem(@Nullable String suggestion) {
        if (suggestion != null) {
            this.suggestion = suggestion;
        }
        this.type = GOOGLE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SuggestionItem) {
            final SuggestionItem other = (SuggestionItem) obj;
            return this.suggestion.equalsIgnoreCase(other.suggestion);
        } else return false;
    }

    @Override
    public int hashCode() {
        return suggestion.hashCode();
    }
}
