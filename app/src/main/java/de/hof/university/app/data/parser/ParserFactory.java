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

/**
 * Created by larsg on 17.06.2016.
 */
public final class ParserFactory {

    public final static String TAG = "ParserFactory";

    public static Parser create(Enum<ParserFactory.EParser> parserEnum) {
        if (parserEnum == ParserFactory.EParser.SCHEDULE) {
            return new ScheduleParser();
        } else if (parserEnum == ParserFactory.EParser.CHANGES) {
            return new ChangesParser();
        } else if (parserEnum == ParserFactory.EParser.COURSES) {
            return new StudyCourseParser();
        } else if (parserEnum == ParserFactory.EParser.MENU) {
            return new MealParser();
        } else if (parserEnum == ParserFactory.EParser.MYSCHEDULE) {
            return new MyScheduleParser();
        } else
            assert (false);
        return null;
    }

    public enum EParser {CHANGES, SCHEDULE, COURSES, MENU, MYSCHEDULE}
}
