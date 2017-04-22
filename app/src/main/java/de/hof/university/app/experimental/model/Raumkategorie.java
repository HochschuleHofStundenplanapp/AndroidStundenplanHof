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

package de.hof.university.app.experimental.model;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * Created by Lukas on 05.07.2016.
 */
@Keep
public class Raumkategorie implements Level, Serializable {

    public final static String TAG = "Raumkategorie";

    private final String title;

    public Raumkategorie(final String title) {
        this.title = title;
    }

    public final String getTitle() {
        return title;
    }

    @Override
    public final int getLevel() {
        return 1;
    }

}
