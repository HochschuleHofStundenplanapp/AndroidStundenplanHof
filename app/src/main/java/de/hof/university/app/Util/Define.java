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
	public static final String PARSER_STYLE = "style"; //NON-NLS
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
	public static final String raumlistFilename = "raumliste";

	// Namen für die Fragmente
	public static final String myScheduleFragmentName = "MYSCHEDULE_FRAGMENT";
	public static final String scheduleFragmentName = "SCHEDULE_FRAGMENT";
	public static final String changesFragmentName = "CHANGES_FRAGMENT";
	public static final String mealsFragmentName = "MEALS_FRAGMENT";


	public static final long serialVersionUID = 1L;

	// Essensplan
	public final static String URL_STUDENTENWERK = "https://www.studentenwerk-oberfranken.de/?eID=bwrkSpeiseplanRss&tx_bwrkspeiseplan_pi2%5Bbar%5D=340&tx_bwrkspeiseplan_pi2%5Bdate%5D=";


	// Server Web-Services

	// Beispielaufrufe
	// https://soapuser:F%98z&12@app.hof-university.de/soap/client.php?
	//
	// https://app.hof-university.de/soap/client.php?f=Courses&tt=WS
	//
	// https://soapuser:F%98z&12@app.hof-university.de/soap/client.php?f=Schedule&stg=MC&tt=WS&sem=1
	// https://soapuser:F%98z&12@app.hof-university.de/soap/client.php?f=Changes&stg=MC&tt=WS&sem=1

	// Server-URL
	// alt:		https://www.hof-university.de/
	// neu:		https://app.hof-university.de/
	// test:	http://sh-web02.hof-university.de/
	public final static String APP_SERVER_HOF = "https://app.hof-university.de";


	/* Communication with the Web-Services SOAP API */
	// Produktiv-Server:
	// user:		soapuser
	// password:	F%98z&12
	// Test-Server:
	// user: 		test
	// password:	test
	public final static String URL_WEBSERVICE_HOF = APP_SERVER_HOF + "/soap/client.php";
	public final static String sAuthSoapUserName = "soapuser";
	public final static String sAuthSoapPassword = "F%98z&12";

	public final static String URL_STUDYCOURSE 	= (URL_WEBSERVICE_HOF+"?f=Courses&tt=%s");
	public final static String URL_SCHEDULE 	= (URL_WEBSERVICE_HOF+"?f=Schedule&stg=%s&sem=%s&tt=%s");
	public final static String URL_CHANGES 		= (URL_WEBSERVICE_HOF+"?f=Changes");
	public final static String URL_MYSCHEDULE 	= (URL_WEBSERVICE_HOF+"?f=MySchedule");

	public final static String URL_MEAL 		= URL_STUDENTENWERK;


	// show the fragments dealing with the location of the user
	public static final boolean SHOW_LOCATION_FRAGMENT = true;
	// show Notenblatt and Notenfreigabe
	// TODO Weil ausblenden solange die neue Authentifizierungsmethode noch nicht funktioniert
	public static final boolean SHOW_NOTEN = false;
	// publish notification FMC von Google verwenden
	// Push-Notifications
	public final static boolean PUSH_NOTIFICATIONS_ENABLED = true;
	public final static String URL_REGISTER_PUSH_NOTIFICATIONS_HOF = APP_SERVER_HOF + "/soap/fcm_register_user.php";


	// Additional URLS
	public final static String ABOUTUSURL="http://www.hof-university.de/ueber-uns.html";
	public final static String IMPRESSUMURL="http://www.hof-university.de/impressum.html";
	public final static String DATENSCHUTZURL="http://www.hof-university.de/datenschutz.html";
	public final static String PLAYSTOREURL="https://play.google.com/store/apps/details?id=de.hof.university.app";


	// how long to store content from
	public static final int MAX_CACHE_TIME = 60 * 24 * 2;			// zwei Tage

	//how long to not auto refresh
	public static final int SCHEDULE_CACHE_TIME = 60 * 24 * 7;		// eine Woche
	public static final int MYSCHEDULE_CACHE_TIME = 60 * 24 * 7;	// eine Woche
	public static final int CHANGES_CACHE_TIME = 60 * 8;			// acht Stunden
	public static final int MEAL_CACHE_TIME = 60 * 24 * 7;			// eine Woche
	public static final int COURSES_CACHE_TIME = 60 * 24 * 7;		// eine Woche

	//timeouts
	public static final int shortTimeout = 1000;		// eine Sekunde
	public static final int longTimeout = 3000;			// drei Sekunden

	// Raumsuche
	public final static String URL_RAUMSUCHE_LOGIN = "https://www.hof-university.de/anmelden.html";
	public final static String URL_RAUMSUCHE_LOGIN_SUCCESS = "http://www.hof-university.de/anmeldung-erfolgreich.html";
	public final static String URL_RAUMSUCHE = "https://www.hof-university.de/studierende/info-service/it-service/raumhardsoftwaresuche.html";


	// Pushnotifications
	public final static String FCM_TOKEN="de.hof-university.app.fcm_token";
	public final static String FCM_PREF="de.hof-university.app.fcm_pref";


	//EMail
	public final static String FEEDBACKEMAILADDRESS = "androidapps@hof-university.de";

}
