/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
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

package de.hof.university.app.model;

/**
 * Created by Lukas on 25.11.2015.
 */
public class MediumListItem {
    private final String title;

    public MediumListItem(final String title) {
        this.title = title;
    }

// --Commented out by Inspection START (17.07.2016 20:12):
//    protected MediumListItem(final Parcel in) {
//        title = in.readString();
//    }
// --Commented out by Inspection STOP (17.07.2016 20:12)

    public final String getTitle() {
        return title;
    }

}
