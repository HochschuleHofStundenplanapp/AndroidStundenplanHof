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

import java.util.List;

/**
 * Created by larsg on 13.04.2016.
 */
public class Course {

    private long id = 0L;
    private String name;
    private String tag;
    private List<String> terms = null;

    public Course(final long id, final String name, final String tag) {
        this.id=id;
        this.name=name;
        this.tag =tag;
    }

    public Course(final String course, final String courseTag, final List<String> termsParams) {
        name=course;
        tag=courseTag;
        this.terms = termsParams;
    }


    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getTag() {
        return tag;
    }

    public final void setTag(String tag) {
        this.tag = tag;
    }

    public final List<String> getTerms() {
        return terms;
    }

    public final void setTerms(final List<String> terms) {
        this.terms = terms;
    }

    @Override
    public final String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
