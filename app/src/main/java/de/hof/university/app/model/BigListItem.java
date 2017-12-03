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

import java.io.Serializable;

import de.hof.university.app.util.Define;

/**
 * Created by Lars on 29.11.2015.
 */
public class BigListItem implements Serializable{
    private static final long serialVersionUID = Define.serialVersionUIDv1;

    private final String title;

    public BigListItem(final String title) {
	    super();
	    this.title = title;
    }

    public final String getTitle() {
        return title;
    }
}
