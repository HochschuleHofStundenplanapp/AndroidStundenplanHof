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

package de.hof.university.app.model.settings;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import de.hof.university.app.util.Define;

/**
 * Studiengang
 */
public class StudyCourse implements Serializable, Comparable<StudyCourse> {
    private static final long serialVersionUID = Define.serialVersionUIDv1;

    private long id = 0L;
    private final String name;
    private final String tag;
    private List<String> terms = null;

    public StudyCourse(final long id, final String name, final String tag) {
	    super();
	    this.id = id;
	    this.name = name;
	    this.tag = tag;
    }

    public StudyCourse(final String course, final String courseTag, final List<String> termsParams) {
	    super();
	    name = course;
	    tag = courseTag;
	    // sortieren
	    Collections.sort(termsParams);
	    this.terms = termsParams;
    }


    public final String getName() {
        return name;
    }

    public final String getTag() {
        return tag;
    }

    public final List<String> getTerms() {
        return terms;
    }

    @Override
    public final String toString() {
        return "StudyCourse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull final StudyCourse other) {
        return name.compareTo(other.name);
    }
}
