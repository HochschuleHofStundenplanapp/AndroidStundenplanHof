package de.hof.university.app.calendar;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Daniel on 19.07.2017.
 */

public class DateCorrection {

    private static DateCorrection dateCorrection = null;

    int summerStartDay = 15;
    int summerStartMonth = Calendar.MARCH;
    int winterStartDay = 1;
    int winterStartMonth = Calendar.OCTOBER;

    int summerEndDay = 10;
    int summerEndMonth = Calendar.JULY;
    int winterEndDay = 25;
    int winterEndMonth = Calendar.JANUARY;

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
        Calendar currentDateCal = Calendar.getInstance();
        currentDateCal.setTime(new Date());

        lastYearSummerStartDate = getSemesterStartDate(currentDateCal.get(Calendar.YEAR) - 1, "SS");
        lastYearSummerEndDate = getSemesterEndDate(currentDateCal.get(Calendar.YEAR) - 1, "SS");
        lastYearWinterStartDate = getSemesterStartDate(currentDateCal.get(Calendar.YEAR) - 1, "WS");

        thisYearWinterEndDate = getSemesterEndDate(currentDateCal.get(Calendar.YEAR), "WS");
        thisYearSummerStartDate = getSemesterStartDate(currentDateCal.get(Calendar.YEAR), "SS");
        thisYearSummerEndDate = getSemesterEndDate(currentDateCal.get(Calendar.YEAR), "SS");
        thisYearWinterStartDate = getSemesterStartDate(currentDateCal.get(Calendar.YEAR), "WS");

        nextYearWinterEndDate = getSemesterEndDate(currentDateCal.get(Calendar.YEAR) + 1, "WS");

        Calendar beginOfYearCalendar = Calendar.getInstance();
        beginOfYearCalendar.setTime(new Date());
        beginOfYearCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        beginOfYearCalendar.set(Calendar.DATE, 1);
        beginOfThisYearDate = beginOfYearCalendar.getTime();

        Calendar endOfYearCalendar = Calendar.getInstance();
        endOfYearCalendar.setTime(new Date());
        endOfYearCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        endOfYearCalendar.set(Calendar.DATE, 31);
        endOfThisYearDate = endOfYearCalendar.getTime();
    }

    private Date getSemesterStartDate(int year, String semester) {
        Date correctStartDate = new Date();

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

    public Date getCorrectStartDate(Date startDate, Date endDate) {
        Calendar correctStartDateCal = Calendar.getInstance();

        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTime(startDate);

        Date semesterStartDate = thisYearSummerStartDate;

        if (endDate.after(endOfThisYearDate)) {
            semesterStartDate = thisYearWinterStartDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.after(beginOfThisYearDate)) {
            semesterStartDate = lastYearWinterStartDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.before(beginOfThisYearDate)) {
            semesterStartDate = lastYearSummerStartDate;
        }

        //Date semesterStartDate = getSemesterStartDate(startDateCal.get(Calendar.YEAR), getSemesterFromStartDate(startDateCal));

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

    public Date getCorrectEndDate(Date startDate, Date endDate) {
        Calendar correctEndDateCal = Calendar.getInstance();

        Calendar endDateCal = Calendar.getInstance();
        endDateCal.setTime(endDate);

        Date semesterEndDate = thisYearSummerEndDate;

        if (endDate.after(endOfThisYearDate)) {
            semesterEndDate = nextYearWinterEndDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.after(beginOfThisYearDate)) {
            semesterEndDate = thisYearWinterEndDate;
        } else if (startDate.before(beginOfThisYearDate) && endDate.before(beginOfThisYearDate)) {
            semesterEndDate = lastYearSummerEndDate;
        }

        //Date semesterEndDate = getSemesterEndDate(endDateCal.get(Calendar.YEAR), getSemesterFromEndDate(endDateCal));

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
