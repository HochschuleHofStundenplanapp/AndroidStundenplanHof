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

package de.hof.university.app.data.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hof.university.app.BuildConfig;
import de.hof.university.app.Util.Define;
import de.hof.university.app.model.settings.Course;

/**
 * Created by larsg on 17.06.2016.
 */
public class CourseParser implements Parser<Course> {
	private String language;

	@Override
	public final ArrayList<Course> parse(String[] params) {
		ArrayList<Course> result = new ArrayList<Course>();
		if ( params.length == 2 ) {
			final String jsonString = params[ 0 ];
			language = params[ 1 ];
			//Escape, if String is empty
			if ( jsonString.isEmpty() ) {
				return result;
			}
			JSONArray jsonArray = null;
			try {
				jsonArray = new JSONObject(jsonString).getJSONArray("courses");
			} catch ( final JSONException e ) {
				if ( BuildConfig.DEBUG ) e.printStackTrace();
			}
			for ( int i = 0; i < jsonArray.length(); ++i ) {
				try {
					Course course = convertJsonObject(jsonArray.getJSONObject(i));
					if ( course != null ) {
						result.add(course);
					}
				} catch ( final JSONException ignored ) {
				}
			}
		}
		return result;
	}

	protected final Course convertJsonObject(JSONObject jsonObject) {
		try {
			final String name = jsonObject.getJSONObject(Define.COURSE_PARSER_LABELS).getString(language);
			final String tag = jsonObject.getString(Define.COURSE_PARSER_COURSE);
			JSONArray jsonArray = jsonObject.getJSONArray(Define.COURSE_PARSER_SEMESTER);
			ArrayList<String> terms = new ArrayList<>();
			for ( int i = 0; i < jsonArray.length(); ++i ) {
				terms.add(jsonArray.getString(i));
			}
			return new Course(name, tag, terms);
		} catch ( final JSONException e ) {
			if (BuildConfig.DEBUG) e.printStackTrace();
			return null;
		}
	}
}
