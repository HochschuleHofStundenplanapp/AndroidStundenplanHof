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

package de.hof.university.app.Util;

/**
 * Created by stepping on 17.07.2016.
 */
public final class Define {

	// Strings returned from the Webserverinterface from IT-Service of Hochschule Hof,
	// language-INDEPENDENT
	public static final String SCHEDULE_PARSER_LABEL = "label";     //NON-NLS
	public static final String SCHEDULE_PARSER_COMMENT = "comment";     //NON-NLS
	public static final String SCHEDULE_PARSER_GROUP = "group";     //NON-NLS
	public static final String SCHEDULE_PARSER_REASON = "reason";     //NON-NLS
	public static final String COURSE_PARSER_LABELS = "labels";     //NON-NLS
	public static final String COURSE_PARSER_COURSE = "course";     //NON-NLS
	public static final String COURSE_PARSER_SEMESTER = "semester";     //NON-NLS

	public static final String PARSER_SCHEDULE = "schedule"; // NON-NLS
	public static final String PARSER_DAY = "day"; //NON-NLS
	public static final String PARSER_TYPE = "type"; //NON-NLS
	public static final String PARSER_STARTTIME = "starttime"; //NON-NLS
	public static final String PARSER_ENDTIME = "endtime"; //NON-NLS
	public static final String PARSER_STARTDATE = "startdate"; //NON-NLS
	public static final String PARSER_ENDDATE = "enddate"; //NON-NLS
	public static final String PARSER_ROOM = "room"; //NON-NLS
	public static final String PARSER_DOCENT = "docent"; //NON-NLS
	public static final String PARSER_SPLUSNAME = "splusname"; 	//NON-NLS

	public static final String PARSER_CHANGES = "changes"; //NON-NLS
	public static final String PARSER_TIME = "time"; //NON-NLS
	public static final String PARSER_DATE = "date"; //NON-NLS
	public static final String PARSER_ORIGNAL = "original"; //NON-NLS
	public static final String PARSER_ALTERNATIVE = "alternative"; //NON-NLS


	// Semester term: Winterterm, summerterm (Wintersemester, Sommersemester)

	public static final String myScheduleFilename = "mySchedule";
	public static final String scheduleFilename = "schedule";
	public static final String changesFilename = "changes";
	public static final String coursesFilename = "courses";
	public static final String mealsFilename = "meals";

	// Namen für die Fragmente
	public static final String myScheduleFragmentName = "MYSCHEDULE_FRAGMENT";
	public static final String scheduleFragmentName = "SCHEDULE_FRAGMENT";
	public static final String changesFragmentName = "CHANGES_FRAGMENT";
	public static final String mealsFragmentName = "MEALS_FRAGMENT";

	// show the fragments dealing with the location of the user
	public static final int SHOW_LOCATION_FRAGMENT = 0 ;
	// show Notenblatt and Notenfreigabe
	// TODO Weil ausblenden solange die neue Authentifizierungsmethode noch nicht funktioniert
	public static final int SHOW_NOTEN = 0 ;
	// publish notification FMC von Google verwenden
	public static final int SHOW_PUSHNOTIFICATION = 0 ;

	public static final long serialVersionUID = 1L;

	// Beispielaufruf
	// https://soapuser:F%98z&12@www.hof-university.de/soap/client.php?
	//
	// https://www.hof-university.de/soap/client.php?f=Courses&tt=WS
	//
	// https://soapuser:F%98z&12@www.hof-university.de/soap/client.php?f=Schedule&stg=MC&tt=WS&sem=1
	// https://soapuser:F%98z&12@www.hof-university.de/soap/client.php?f=Changes&stg=MC&tt=WS&sem=1

	/* Communication with the Web-Services SOAP API */
	public final static String sAuthSoapUserName = "soapuser";
	public final static String sAuthSoapPassword = "F%98z&12";

	// Testserver
	public final static String sAuthSoapUserNameTest = "test";
	public final static String sAuthSoapPasswordTest = "test";


	// Essensplan
	public final static String URL_STUDENTENWERK = "https://www.studentenwerk-oberfranken.de/?eID=bwrkSpeiseplanRss&tx_bwrkspeiseplan_pi2%5Bbar%5D=340&tx_bwrkspeiseplan_pi2%5Bdate%5D=";


	// Produktiv-Server Web-Services
	public final static String URL_WEBSERVICE_HOF = "https://app.hof-university.de/soap/client.php";
	//public final static String URL_WEBSERVICE_HOF_ALT = "https://www.hof-university.de/soap/client.php";
	// Test-Server
	public final static String URL_WEBSERVICE_HOF_TEST = "http://sh-web02.hof-university.de/soap/client.php";

	public final static String URL_STUDYCOURSE = URL_WEBSERVICE_HOF+"?f=Courses&tt=%s";
	public final static String URL_SCHEDULE =   (URL_WEBSERVICE_HOF+"?f=Schedule&stg=%s&sem=%s&tt=%s");
	public final static String URL_CHANGES =    (URL_WEBSERVICE_HOF+"?f=Changes");
	public final static String URL_MYSCHEDULE = (URL_WEBSERVICE_HOF+"?f=MySchedule");

	// how long to store content from
	public static final int MAX_CACHE_TIME = 60 * 24 * 2;

	// Raumsuche
	public final static String URL_RAUMSUCHE_LOGIN = "https://www.hof-university.de/anmelden.html";
	public final static String URL_RAUMSUCHE_LOGIN_SUCCESS = "http://www.hof-university.de/anmeldung-erfolgreich.html";
	public final static String URL_RAUMSUCHE = "https://www.hof-university.de/studierende/info-service/it-service/raumhardsoftwaresuche.html";

	// Push-Notifications
	public final static boolean PUSH_NOTIFICATIONS_ENABLED = false;
}
