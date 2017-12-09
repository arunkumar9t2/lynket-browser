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

package arun.com.chromer.search.suggestion.items;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class TitleSuggestionItem implements SuggestionItem {
    public final String title;
    public final String subTitle;
    private final int type;

    public TitleSuggestionItem(@NonNull String title, @Nullable String subTitle) {
        this.title = title;
        this.subTitle = subTitle;
        this.type = getType();
    }

    @NonNull
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSubTitle() {
        return subTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TitleSuggestionItem that = (TitleSuggestionItem) o;

        if (type != that.type) return false;
        if (!title.equals(that.title)) return false;
        return subTitle != null ? subTitle.equals(that.subTitle) : that.subTitle == null;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (subTitle != null ? subTitle.hashCode() : 0);
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "TitleSuggestionItem{" +
                "title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", type=" + type +
                '}';
    }
}
