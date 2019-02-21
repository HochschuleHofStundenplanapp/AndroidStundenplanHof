/*
 * Copyright (c) 2017 Hochschule Hof, Daniel Glaser
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


package de.hof.university.app.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Daniel on 19.07.2017.
 */

public class DateCorrection {

    private static DateCorrection dateCorrection = null;

    /* Konstanten */
    private static final int summerStartDay = 15;
    private static final int summerStartMonth = GregorianCalendar.MARCH;
    private static final int winterStartDay = 1;
    private static final int winterStartMonth = GregorianCalendar.OCTOBER;

    private static final int summerEndDay = 10;
    private static final int summerEndMonth = GregorianCalendar.JULY;
    private static final int winterEndDay = 25;
    private static final int winterEndMonth = GregorianCalendar.JANUARY;

    private Date lastYearSummerStartDate = new Date();
    private Date lastYearSummerEndDate = new Date();
    private Date lastYearWinterStartDate = new Date();
    private Date thisYearWinterEndDate = new Date();
    private Date thisYearSummerStartDate = new Date();
    private Date thisYearSummerEndDate = new Date();
    private Date thisYearWinterStartDate = new Date();
    private Date nextYearWinterEndDate = new Date();

    private Date beginOfThisYearDate = new Date();
    private Date endOfThisYearDate = new Date();

    public static DateCorrection getInstance() {
        if (DateCorrection.dateCorrection == null) {
            DateCorrection.dateCorrection = new DateCorrection();
        }
        return DateCorrection.dateCorrection;
    }

    private DateCorrection() {
	    super();
	    Calendar currentDateCal = GregorianCalendar.getInstance();
	    currentDateCal.setTime(new Date());

	    lastYearSummerStartDate = getSemesterStartDate(currentDateCal.get(GregorianCalendar.YEAR) - 1, "SS");
	    lastYearSummerEndDate = getSemesterEndDate(currentDateCal.get(GregorianCalendar.YEAR) - 1, "SS");
	    lastYearWinterStartDate = getSemesterStartDate(currentDateCal.get(GregorianCalendar.YEAR) - 1, "WS");

	    thisYearWinterEndDate = getSemesterEndDate(currentDateCal.get(GregorianCalendar.YEAR), "WS");
	    thisYearSummerStartDate = getSemesterStartDate(currentDateCal.get(GregorianCalendar.YEAR), "SS");
	    thisYearSummerEndDate = getSemesterEndDate(currentDateCal.get(GregorianCalendar.YEAR), "SS");
	    thisYearWinterStartDate = getSemesterStartDate(currentDateCal.get(GregorianCalendar.YEAR), "WS");

	    nextYearWinterEndDate = getSemesterEndDate(currentDateCal.get(GregorianCalendar.YEAR) + 1, "WS");

	    Calendar beginOfYearCalendar = GregorianCalendar.getInstance();
	    beginOfYearCalendar.setTime(new Date());
	    beginOfYearCalendar.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
	    beginOfYearCalendar.set(GregorianCalendar.DATE, 1);
	    beginOfThisYearDate = beginOfYearCalendar.getTime();

	    Calendar endOfYearCalendar = GregorianCalendar.getInstance();
	    endOfYearCalendar.setTime(new Date());
	    endOfYearCalendar.set(GregorianCalendar.MONTH, GregorianCalendar.DECEMBER);
	    endOfYearCalendar.set(GregorianCalendar.DATE, 31);
	    endOfThisYearDate = endOfYearCalendar.getTime();
    }

    private Date getSemesterStartDate(int year, String semester) {
        Date correctStartDate = new Date();

        if (semester.equals("SS")) {
            Calendar summerStartDateCalendar = GregorianCalendar.getInstance();
            summerStartDateCalendar.set(GregorianCalendar.YEAR, year);
            summerStartDateCalendar.set(GregorianCalendar.MONTH, summerStartMonth);
            summerStartDateCalendar.set(GregorianCalendar.DATE, summerStartDay);

            // Wenn der 15.3 ein Freitag/Samstag/Sonntag ist beginnt der Vorlesungszeitraum am nächstfolgenden Montag

            switch (summerStartDateCalendar.get(GregorianCalendar.DAY_OF_WEEK)) {
                case GregorianCalendar.FRIDAY:
                    summerStartDateCalendar.add(GregorianCalendar.DATE, 3);
                    break;
                case GregorianCalendar.SATURDAY:
                    summerStartDateCalendar.add(GregorianCalendar.DATE, 2);
                    break;
                case GregorianCalendar.SUNDAY:
                    summerStartDateCalendar.add(GregorianCalendar.DATE, 1);
                    break;
            }

            correctStartDate = summerStartDateCalendar.getTime();
        } else if (semester.equals("WS")) {
            Calendar winterStartDateCalendar = GregorianCalendar.getInstance();
            winterStartDateCalendar.set(GregorianCalendar.YEAR, year);
            winterStartDateCalendar.set(GregorianCalendar.MONTH, winterStartMonth);
            winterStartDateCalendar.set(GregorianCalendar.DATE, winterStartDay);

            // Wenn der 1.10 ein Freitag/Samstag/Sonntag ist beginnt der Vorlesungszeitraum am nächstfolgenden Montag

            switch (winterStartDateCalendar.get(GregorianCalendar.DAY_OF_WEEK)) {
                case GregorianCalendar.FRIDAY:
                    winterStartDateCalendar.add(GregorianCalendar.DATE, 3);
                    break;
                case GregorianCalendar.SATURDAY:
                    winterStartDateCalendar.add(GregorianCalendar.DATE, 2);
                    break;
                case GregorianCalendar.SUNDAY:
                    winterStartDateCalendar.add(GregorianCalendar.DATE, 1);
                    break;
            }

            correctStartDate = winterStartDateCalendar.getTime();
        }

        return correctStartDate;
    }

    private Date getSemesterEndDate(int year, String semester) {
        Date correctEndDate = new Date();
    
        switch (semester) {
            case "SS":
            
                final Calendar summerEndDateCalendar = GregorianCalendar.getInstance();
                summerEndDateCalendar.set(GregorianCalendar.YEAR, year);
                summerEndDateCalendar.set(GregorianCalendar.MONTH, summerEndMonth);
                summerEndDateCalendar.set(GregorianCalendar.DATE, summerEndDay);
            
                // Wenn der 10.7 ein Samstag/Sonntag/Montag ist endet der Vorlesungszeitraum am vorausgehenden Freitag
            
                switch (summerEndDateCalendar.get(GregorianCalendar.DAY_OF_WEEK)) {
                    case GregorianCalendar.SATURDAY:
                        summerEndDateCalendar.add(GregorianCalendar.DATE, -1);
                        break;
                    case GregorianCalendar.SUNDAY:
                        summerEndDateCalendar.add(GregorianCalendar.DATE, -2);
                        break;
                    case GregorianCalendar.MONDAY:
                        summerEndDateCalendar.add(GregorianCalendar.DATE, -3);
                        break;
                }
            
                correctEndDate = summerEndDateCalendar.getTime();
                break;
            case "WS":
            
                final Calendar winterEndDateCalendar = GregorianCalendar.getInstance();
                winterEndDateCalendar.set(GregorianCalendar.YEAR, year);
                winterEndDateCalendar.set(GregorianCalendar.MONTH, winterEndMonth);
                winterEndDateCalendar.set(GregorianCalendar.DATE, winterEndDay);
            
                // Wenn der 25.1 ein Samstag/Sonntag/Montag ist endet der Vorlesungszeitraum am vorausgehenden Freitag
            
                switch (winterEndDateCalendar.get(GregorianCalendar.DAY_OF_WEEK)) {
                    case GregorianCalendar.SATURDAY:
                        winterEndDateCalendar.add(GregorianCalendar.DATE, -1);
                        break;
                    case GregorianCalendar.SUNDAY:
                        winterEndDateCalendar.add(GregorianCalendar.DATE, -2);
                        break;
                    case GregorianCalendar.MONDAY:
                        winterEndDateCalendar.add(GregorianCalendar.DATE, -3);
                        break;
                }
            
                correctEndDate = winterEndDateCalendar.getTime();
                break;
            default:
//noinspection ConstantConditions
                org.junit.Assert.assertTrue(false);
                break;
        }

        return correctEndDate;
    }

    public Date getCorrectStartDate(Date startDate, Date endDate) {
        final Calendar correctStartDateCal = GregorianCalendar.getInstance();

        final Calendar startDateCal = GregorianCalendar.getInstance();
        startDateCal.setTime(startDate);

        Date semesterStartDate = thisYearSummerStartDate;

        if (endDate.after(endOfThisYearDate)) {
            semesterStartDate = thisYearWinterStartDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.after(beginOfThisYearDate)) {
            semesterStartDate = lastYearWinterStartDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.before(beginOfThisYearDate)) {
            semesterStartDate = lastYearSummerStartDate;
        }

        //Date semesterStartDate = getSemesterStartDate(startDateCal.get(GregorianCalendar.YEAR), getSemesterFromStartDate(startDateCal));

        correctStartDateCal.setTime(semesterStartDate);

        // zu nächsten Wochentag des startDates setzen
        while (correctStartDateCal.get(GregorianCalendar.DAY_OF_WEEK) != startDateCal.get(GregorianCalendar.DAY_OF_WEEK)) {
            correctStartDateCal.add(GregorianCalendar.DATE, 1);
        }

        // Zeit richtig setzen
        correctStartDateCal.set(GregorianCalendar.HOUR_OF_DAY, startDateCal.get(GregorianCalendar.HOUR_OF_DAY));
        correctStartDateCal.set(GregorianCalendar.MINUTE, startDateCal.get(GregorianCalendar.MINUTE));
        correctStartDateCal.set(GregorianCalendar.SECOND, startDateCal.get(GregorianCalendar.SECOND));
        correctStartDateCal.set(GregorianCalendar.MILLISECOND, startDateCal.get(GregorianCalendar.MILLISECOND));

        return correctStartDateCal.getTime();
    }

    public Date getCorrectEndDate(Date startDate, Date endDate) {
        final Calendar correctEndDateCal = GregorianCalendar.getInstance();

        final Calendar endDateCal = GregorianCalendar.getInstance();
        endDateCal.setTime(endDate);

        Date semesterEndDate = thisYearSummerEndDate;

        if (endDate.after(endOfThisYearDate)) {
            semesterEndDate = nextYearWinterEndDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.after(beginOfThisYearDate)) {
            semesterEndDate = thisYearWinterEndDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.before(beginOfThisYearDate)) {
            semesterEndDate = lastYearSummerEndDate;
        }

        //Date semesterEndDate = getSemesterEndDate(endDateCal.get(GregorianCalendar.YEAR), getSemesterFromEndDate(endDateCal));

        correctEndDateCal.setTime(semesterEndDate);

        // zu vorherigem Wochentag des startDates setzen
        while (correctEndDateCal.get(GregorianCalendar.DAY_OF_WEEK) != endDateCal.get(GregorianCalendar.DAY_OF_WEEK)) {
            correctEndDateCal.add(GregorianCalendar.DATE, -1);
        }

        // Zeit richtig setzen
        correctEndDateCal.set(GregorianCalendar.HOUR_OF_DAY, endDateCal.get(GregorianCalendar.HOUR_OF_DAY));
        correctEndDateCal.set(GregorianCalendar.MINUTE, endDateCal.get(GregorianCalendar.MINUTE));
        correctEndDateCal.set(GregorianCalendar.SECOND, endDateCal.get(GregorianCalendar.SECOND));
        correctEndDateCal.set(GregorianCalendar.MILLISECOND, endDateCal.get(GregorianCalendar.MILLISECOND));

        return correctEndDateCal.getTime();
    }
}
