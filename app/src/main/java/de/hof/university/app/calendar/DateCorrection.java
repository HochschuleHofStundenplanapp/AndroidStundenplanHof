package de.hof.university.app.calendar;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Daniel on 19.07.2017.
 */

public class DateCorrection {

    private String getSemesterFromStartDate(Calendar startDateCal) {
        if (startDateCal.getTime().after(getSemesterEndDate(startDateCal.get(Calendar.YEAR), "WS")) && startDateCal.getTime().before(getSemesterEndDate(startDateCal.get(Calendar.YEAR), "SS"))) {
            return "SS";
        } else {
            return "WS";
        }
    }

    private String getSemesterFromEndDate(Calendar endDateCal) {
        Calendar endOfYearCalendar = Calendar.getInstance();
        endOfYearCalendar.setTime(new Date());
        endOfYearCalendar.set(Calendar.MONTH, 12 - 1);
        endOfYearCalendar.set(Calendar.DATE, 31);
        Date endOfYear = endOfYearCalendar.getTime();

        if (endDateCal.getTime().after(getSemesterEndDate(endDateCal.get(Calendar.YEAR), "SS")) && endDateCal.getTime().before(endOfYear)) {
            return "SS";
        } else {
            return "WS";
        }
    }

    private Date getSemesterStartDate(int year, String semester) {
        Date correctStartDate = new Date();

        int summerStartDay = 15;
        int summerStartMonth = 3 - 1;
        int winterStartDay = 1;
        int winterStartMonth = 10 - 1;

        if (semester.equals("SS")) {
            Calendar summerStartDateCalendar = Calendar.getInstance();
            summerStartDateCalendar.set(Calendar.YEAR, year);
            summerStartDateCalendar.set(Calendar.MONTH, summerStartMonth);
            summerStartDateCalendar.set(Calendar.DATE, summerStartDay);

            // Wenn der 15.3 ein Freitag/Samstag/Sonntag ist beginnt der Vorlesungszeitraum am nächstfolgenden Montag

            switch (summerStartDateCalendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.FRIDAY:
                    summerStartDateCalendar.add(Calendar.DATE, 3);
                    break;
                case Calendar.SATURDAY:
                    summerStartDateCalendar.add(Calendar.DATE, 2);
                    break;
                case Calendar.SUNDAY:
                    summerStartDateCalendar.add(Calendar.DATE, 1);
                    break;
            }

            correctStartDate = summerStartDateCalendar.getTime();
        } else if (semester.equals("WS")) {
            Calendar winterStartDateCalendar = Calendar.getInstance();
            winterStartDateCalendar.set(Calendar.YEAR, year);
            winterStartDateCalendar.set(Calendar.MONTH, winterStartMonth);
            winterStartDateCalendar.set(Calendar.DATE, winterStartDay);

            // Wenn der 1.10 ein Freitag/Samstag/Sonntag ist beginnt der Vorlesungszeitraum am nächstfolgenden Montag

            switch (winterStartDateCalendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.FRIDAY:
                    winterStartDateCalendar.add(Calendar.DATE, 3);
                    break;
                case Calendar.SATURDAY:
                    winterStartDateCalendar.add(Calendar.DATE, 2);
                    break;
                case Calendar.SUNDAY:
                    winterStartDateCalendar.add(Calendar.DATE, 1);
                    break;
            }

            correctStartDate = winterStartDateCalendar.getTime();
        }

        return correctStartDate;
    }

    private Date getSemesterEndDate(int year, String semester) {
        Date correctEndDate = new Date();

        int summerEndDay = 10;
        int summerEndMonth = 7 - 1;
        int winterEndDay = 25;
        int winterEndMonth = 1 - 1;

        if (semester.equals("SS")) {
            Calendar summerEndDateCalendar = Calendar.getInstance();
            summerEndDateCalendar.set(Calendar.YEAR, year);
            summerEndDateCalendar.set(Calendar.MONTH, summerEndMonth);
            summerEndDateCalendar.set(Calendar.DATE, summerEndDay);

            // Wenn der 10.7 ein Samstag/Sonntag/Montag ist endet der Vorlesungszeitraum am vorausgehenden Freitag

            switch (summerEndDateCalendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SATURDAY:
                    summerEndDateCalendar.add(Calendar.DATE, -1);
                    break;
                case Calendar.SUNDAY:
                    summerEndDateCalendar.add(Calendar.DATE, -2);
                    break;
                case Calendar.MONDAY:
                    summerEndDateCalendar.add(Calendar.DATE, -3);
                    break;
            }

            correctEndDate = summerEndDateCalendar.getTime();
        } else if (semester.equals("WS")) {
            Calendar winterEndDateCalendar = Calendar.getInstance();
            winterEndDateCalendar.set(Calendar.YEAR, year);
            winterEndDateCalendar.set(Calendar.MONTH, winterEndMonth);
            winterEndDateCalendar.set(Calendar.DATE, winterEndDay);

            // Wenn der 25.1 ein Samstag/Sonntag/Montag ist endet der Vorlesungszeitraum am vorausgehenden Freitag

            switch (winterEndDateCalendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SATURDAY:
                    winterEndDateCalendar.add(Calendar.DATE, -1);
                    break;
                case Calendar.SUNDAY:
                    winterEndDateCalendar.add(Calendar.DATE, -2);
                    break;
                case Calendar.MONDAY:
                    winterEndDateCalendar.add(Calendar.DATE, -3);
                    break;
            }

            correctEndDate = winterEndDateCalendar.getTime();
        }

        return correctEndDate;
    }

    public Date getCorrectStartDate(Date startDate) {
        Calendar correctStartDateCal = Calendar.getInstance();

        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTime(startDate);

        Date semesterStartDate = getSemesterStartDate(startDateCal.get(Calendar.YEAR), getSemesterFromStartDate(startDateCal));

        correctStartDateCal.setTime(semesterStartDate);

        // zu nächsten Wochentag des startDates setzen
        while (correctStartDateCal.get(Calendar.DAY_OF_WEEK) != startDateCal.get(Calendar.DAY_OF_WEEK)) {
            correctStartDateCal.add(Calendar.DATE, 1);
        }

        // Zeit richtig setzen
        correctStartDateCal.set(Calendar.HOUR_OF_DAY, startDateCal.get(Calendar.HOUR_OF_DAY));
        correctStartDateCal.set(Calendar.MINUTE, startDateCal.get(Calendar.MINUTE));
        correctStartDateCal.set(Calendar.SECOND, startDateCal.get(Calendar.SECOND));
        correctStartDateCal.set(Calendar.MILLISECOND, startDateCal.get(Calendar.MILLISECOND));

        return correctStartDateCal.getTime();
    }

    public Date getCorrectEndDate(Date endDate) {
        Calendar correctEndDateCal = Calendar.getInstance();

        Calendar endDateCal = Calendar.getInstance();
        endDateCal.setTime(endDate);

        Date semesterEndDate = getSemesterEndDate(endDateCal.get(Calendar.YEAR), getSemesterFromEndDate(endDateCal));

        correctEndDateCal.setTime(semesterEndDate);

        // zu vorherigem Wochentag des startDates setzen
        while (correctEndDateCal.get(Calendar.DAY_OF_WEEK) != endDateCal.get(Calendar.DAY_OF_WEEK)) {
            correctEndDateCal.add(Calendar.DATE, -1);
        }

        // Zeit richtig setzen
        correctEndDateCal.set(Calendar.HOUR_OF_DAY, endDateCal.get(Calendar.HOUR_OF_DAY));
        correctEndDateCal.set(Calendar.MINUTE, endDateCal.get(Calendar.MINUTE));
        correctEndDateCal.set(Calendar.SECOND, endDateCal.get(Calendar.SECOND));
        correctEndDateCal.set(Calendar.MILLISECOND, endDateCal.get(Calendar.MILLISECOND));

        return correctEndDateCal.getTime();
    }
}
