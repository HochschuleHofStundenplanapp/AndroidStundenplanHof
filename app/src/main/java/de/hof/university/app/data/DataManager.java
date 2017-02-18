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

package de.hof.university.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hof.university.app.Communication.RegisterLectures;
import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.Util.MyString;
import de.hof.university.app.data.parser.Parser;
import de.hof.university.app.data.parser.ParserFactory;
import de.hof.university.app.data.parser.ParserFactory.EParser;
import de.hof.university.app.model.HofObject;
import de.hof.university.app.model.meal.Meal;
import de.hof.university.app.model.meal.Meals;
import de.hof.university.app.model.schedule.Changes;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.model.schedule.MySchedule;
import de.hof.university.app.model.schedule.Schedule;
import de.hof.university.app.model.settings.StudyCourse;
import de.hof.university.app.model.settings.StudyCourses;

/**
 *
 */
public class DataManager {

	public static final String TAG = "DataManager";

	private enum CONNECTION {

		// Essen
		MEAL(Define.URL_STUDENTENWERK, Define.MAX_CACHE_TIME ),
		STUDYCOURSE(Define.URL_STUDYCOURSE, Define.MAX_CACHE_TIME);

		private final String url;
		private final int cache;

		CONNECTION(final String url, final int cache) {
			this.url = url;
			this.cache = cache;
		}

		public final int getCache() {
			return this.cache;
		}

		public final String getUrl() {
			return this.url;
		}
	}


	// single instance of the Factories
	static final private DataConnector dataConnector = new DataConnector();

	private MySchedule mySchedule;

	private static final DataManager dataManager = new DataManager();


	public static DataManager getInstance() {
		return DataManager.dataManager;
	}

	private DataManager() {
	}

	public final ArrayList<Meal> getMeals(Context context, boolean forceRefresh) {
		Object object = readObject(context, Define.mealsFilename);
		Meals meals = new Meals();

		if ( object != null ) {
			meals = (Meals) object;
		}

		if ( forceRefresh || object == null || meals.getMeals().size() == 0 || meals.getLastSaved() == null || !cacheStillValid(meals, CONNECTION.MEAL.getCache()) ) {
			final Parser parser = ParserFactory.create(EParser.MENU);
			final Calendar calendar = Calendar.getInstance();
			final String url = DataManager.CONNECTION.MEAL.getUrl() + calendar.get(Calendar.YEAR) + '-' + (calendar.get(Calendar.MONTH) + 1) + '-' + calendar.get(Calendar.DAY_OF_MONTH);
			final String xmlString = this.getData(url);

//TODO check
			if ( xmlString.isEmpty() ) {
				if ( !forceRefresh && object != null ) {
					return meals.getMeals();
				} else {
					return null;
				}
			}

			final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			final String[] params = { xmlString, sharedPreferences.getString("speiseplan_tarif", "1") };
			assert parser != null;

			ArrayList<Meal> tmpMeals = (ArrayList<Meal>) parser.parse(params);

			if ( tmpMeals.isEmpty() ) {
				if ( !forceRefresh && meals.getMeals().size() > 0 ) {
					return meals.getMeals();
				} else {
					return new ArrayList<>();
				}
			}

			meals.setMeals(tmpMeals);

			meals.setLastSaved(new Date());
			saveObject(context, meals, Define.mealsFilename);
		}

		return meals.getMeals();
	}

	public final ArrayList<LectureItem> getSchedule(Context context, String language, String course, String semester,
	                                                String termTime, boolean forceRefresh) {
		Object object = readObject(context, Define.scheduleFilename);
		Schedule schedule = new Schedule();

		if ( object != null ) {
			schedule = (Schedule) object;
		}

		if ( forceRefresh
				     || object == null
				     || schedule.getLectures().size() == 0
				     || schedule.getLastSaved() == null
//TODO				     || !cacheStillValid(schedule, Define.URL_SCHEDULE.getCache())
				     || !schedule.getCourse().equals(course)
				     || !schedule.getSemester().equals(semester)
				     || !schedule.getTermtime().equals(termTime) )
			{
			// Änderungen sollen neu geholt werden
			resetChangesLastSave(context);

			final Parser parser = ParserFactory.create(EParser.SCHEDULE);
			final String aString = String.format(Define.URL_SCHEDULE, MyString.URLReplaceWhitespace(course), MyString.URLReplaceWhitespace(semester), MyString.URLReplaceWhitespace(termTime));
			final String jsonString = this.getData( aString );

			if ( jsonString.isEmpty() ) {
				if ( !forceRefresh && object != null ) {
					return schedule.getLectures();
				} else {
					return null;
				}
			}

			final String[] params = { jsonString, language };
			assert parser != null;

			ArrayList<LectureItem> lectures = (ArrayList<LectureItem>) parser.parse(params);

			if ( lectures.isEmpty() ) {
				if ( !forceRefresh && schedule.getLectures().size() > 0 ) {
					return schedule.getLectures();
				} else {
					return null;
				}
			}

			schedule.setLectures(lectures);

			schedule.setCourse(course);
			schedule.setSemester(semester);
			schedule.setTermtime(termTime);

			schedule.setLastSaved(new Date());
			saveObject(context, schedule, Define.scheduleFilename);
		}

		return schedule.getLectures();
	}

	public final ArrayList<LectureItem> getMySchedule(Context context, String language,
	                                                  boolean forceRefresh) {
		MySchedule mySchedule = this.getMySchedule(context);

		if ( forceRefresh
			|| mySchedule.getLectures().size() == 0
		     || mySchedule.getLastSaved() == null
//TODO		     || !cacheStillValid(mySchedule, CONNECTION.MYSCHEDULE.getCache())
		     || mySchedule.getIds().size() != mySchedule.getLectures().size()
			)
		{
			// Änderungen sollen neu geholt werden
			resetChangesLastSave(context);

			final Iterator<String> iterator = this.getMySchedule(context).getIds().iterator();
			String url = Define.URL_MYSCHEDULE;
			while ( iterator.hasNext() ) {
				try {
					url += "&id[]=" + URLEncoder.encode(iterator.next(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			final Parser parser = ParserFactory.create(EParser.MYSCHEDULE);

			final String jsonString = this.getData(url);

			if ( jsonString.isEmpty() ) {
				if ( !forceRefresh && mySchedule.getLectures().size() > 0 ) {
					return mySchedule.getLectures();
				} else {
					return null;
				}
			}

			final String[] params = { jsonString, language };

			ArrayList<LectureItem> tmpMySchedule = (ArrayList<LectureItem>) parser.parse(params);

			if ( tmpMySchedule.isEmpty() ) {
				if ( !forceRefresh && mySchedule.getLectures().size() > 0 ) {
					return mySchedule.getLectures();
				} else {
					return null;
				}
			}

			getMySchedule(context).setLectures(tmpMySchedule);

			Set<String> ids = new HashSet<>();
			for ( LectureItem li : tmpMySchedule ) {
				ids.add(String.valueOf(li.getId()));
			}

			getMySchedule(context).setIds(ids);

			getMySchedule(context).setLastSaved(new Date());
			this.saveObject(context, getMySchedule(context), Define.myScheduleFilename);
		}

		return this.getMySchedule(context).getLectures();
	}

	public final ArrayList<Object> getChanges(Context context, String course, String semester,
	                                          String termTime, boolean forceRefresh) {
		Object object = readObject(context, Define.changesFilename);
		Changes changes = new Changes();

		if ( object != null ) {
			changes = (Changes) object;
		}

		if ( forceRefresh || object == null
				     || changes.getChanges().size() == 0
				     || changes.getLastSaved() == null
//TODO				     || !cacheStillValid(changes, CONNECTION.CHANGES.getCache())
				) {
			final Iterator<String> iterator = this.getMySchedule(context).getIds().iterator();

			String url = Define.URL_CHANGES;

			if ( !iterator.hasNext() ) {
				url += "&stg=" + MyString.URLReplaceWhitespace(course);
				url += "&sem=" + MyString.URLReplaceWhitespace(semester);
				url += "&tt=" + MyString.URLReplaceWhitespace(termTime);
			} else {
				// Fügt die ID's der Vorlesungen hinzu die in Mein Stundenplan sind
				// dadurch werden nur Änderungen von Mein Stundenplan geholt
				while ( iterator.hasNext() ) {
					try {
						url += "&id[]=" + URLEncoder.encode(iterator.next(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}

			final Parser parser = ParserFactory.create(EParser.CHANGES);
			final String jsonString = this.getData(url);

			if ( jsonString.isEmpty() ) {
				if ( !forceRefresh && object != null ) {
					return changes.getChanges();
				} else {
					return null;
				}
			}

			final String[] params = { jsonString };
			assert parser != null;

			ArrayList<Object> tmpChanges = (ArrayList<Object>) parser.parse(params);
			changes.setChanges(tmpChanges);

			changes.setLastSaved(new Date());
			saveObject(context, changes, Define.changesFilename);
		}

		return changes.getChanges();
	}

	// es muss immer ein StudyCourse zurückgegeben werden.
	// wenn es aber
	public final ArrayList<StudyCourse> getCourses(final Context context, final String language,
	                                               final String termTime, boolean forceRefresh) {
		StudyCourses studyCourses = (StudyCourses) readObject(context, Define.coursesFilename);
		// wenn noch keine StudyCourses cohanden sind neu anlegen
		if ( studyCourses == null ) {
			studyCourses = new StudyCourses();
		}

		// Änderungen sollen neu geholt werden
		resetChangesLastSave(context);

		if ( forceRefresh
		     || studyCourses.getCourses().size() == 0
		     || studyCourses.getLastSaved() == null
		//     || !cacheStillValid(studyCourses, CONNECTION.COURSE.getCache())
			) {
			final Parser parser = ParserFactory.create(EParser.COURSES);

			final String sTermType = MyString.URLReplaceWhitespace(termTime);
			final String sURL = String.format(Define.URL_STUDYCOURSE, sTermType );
			final String jsonString = this.getData( sURL );

//TODO checken
			if ( jsonString.isEmpty() ) {
				if ( !forceRefresh ) {
					return studyCourses.getCourses();
				} else {
					return null;
				}
			}

			final String[] params = { jsonString, language };
			assert parser != null;

			ArrayList<StudyCourse> tmpCourses = (ArrayList<StudyCourse>) parser.parse(params);

//TODO checken
			if ( tmpCourses.isEmpty() ) {
				if ( !forceRefresh && studyCourses.getCourses().size() > 0 ) {
					return studyCourses.getCourses();
				} else {
					return null;
				}
			}

			studyCourses.setCourses(tmpCourses);

			studyCourses.setLastSaved(new Date());
			saveObject(context, studyCourses, Define.coursesFilename);
		}

		return studyCourses.getCourses();
	}

	// Änderungen sollen neu geholt werden
	private void resetChangesLastSave(Context context) {
		Changes changes = (Changes) readObject(context, Define.changesFilename);
		// Überprüfen ob Datei leer ist dann neu anlegen
		if (changes == null) {
			changes = new Changes();
		}
		// LastSaved zurücksetzten damit Änderungen neu geholt werden
		changes.setLastSaved(null);
		saveObject(context, changes, Define.changesFilename);
	}


	private String getData(String url) {
		return dataConnector.getStringFromUrl(url);
	}

	public final void addToMySchedule(final Context context, final LectureItem s) {
		this.getMySchedule(context).getIds().add(String.valueOf(s.getId()));
		this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
	}

	public final boolean myScheduleContains(final Context context, final LectureItem s) {
		return this.getMySchedule(context).getIds().contains(String.valueOf(s.getId()));
	}

	public final void deleteFromMySchedule(final Context context, final LectureItem s) {
		this.getMySchedule(context).getIds().remove(String.valueOf(s.getId()));
		LectureItem lectureToRemove = null;
		for ( LectureItem li : this.getMySchedule(context).getLectures() ) {
			if ( li.getId().equals(s.getId()) ) {
				lectureToRemove = li;
			}
		}
		if ( lectureToRemove != null ) {
			this.getMySchedule(context).getLectures().remove(lectureToRemove);
		}
		this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
	}

	public final void addAllToMySchedule(final Context context, final Set<String> schedulesIds) {
		this.getMySchedule(context).getIds().addAll(schedulesIds);
		this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
	}

	public final void deleteAllFromMySchedule(final Context context) {
		this.getMySchedule(context).getIds().clear();
		this.getMySchedule(context).getLectures().clear();
		this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
	}

	private MySchedule getMySchedule(final Context context) {
		if ( this.mySchedule == null ) {
			Object object = DataManager.readObject(context, Define.myScheduleFilename);
			if ( object != null && object instanceof Set ) {
				this.mySchedule = new MySchedule();
				this.mySchedule.setIds((Set<String>) object);
			} else if ( object != null && object instanceof MySchedule ) {
				this.mySchedule = (MySchedule) object;
			} else {
				this.mySchedule = new MySchedule();
			}
		}
		return this.mySchedule;
	}

	public final int getMyScheduleSize(final Context context) {
		return this.getMySchedule(context).getIds().size();
	}

	// this is the general method to serialize an object
	//
	private void saveObject(final Context context, Object object, final String filename) {
		try {
			final File file = new File(context.getFilesDir(), filename);
			final FileOutputStream fos = new FileOutputStream(file);
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(object);
			os.close();
			fos.close();
		} catch ( IOException e ) {
			Log.e( TAG, "Fehler beim Speichern des Objektes", e );
		}

		// TODO Fehlerwert zurückgeben?

		// Stundenplan registrieren
		registerFcmServer(context);
	}

	// this is the general method to serialize an object
	private static Object readObject(final Context context, String filename) {
		Object result = null;
		try {
			final File file = new File(context.getFilesDir(), filename);
			if ( file.exists() ) {
				final FileInputStream fis = new FileInputStream(file);
				final ObjectInputStream is = new ObjectInputStream(fis);
				result = is.readObject();
				is.close();
				fis.close();
			}
		} catch ( Exception e ) {
			Log.e( TAG, "Fehler beim Einlesen", e );
		}
		return result;
	}

	private boolean cacheStillValid(HofObject hofObject, final int cacheTime) {
		final Date today = new Date();
		Date lastCached = new Date();

		if ( hofObject.getLastSaved() != null ) {
			lastCached = hofObject.getLastSaved();

			Calendar cal = Calendar.getInstance();
			cal.setTime(lastCached);
			cal.add(Calendar.MINUTE, cacheTime);
			lastCached = cal.getTime();
		}

		return lastCached.after(today);
	}

	public final void cleanCache(final Context context) {
		dataConnector.cleanCache(context, Define.MAX_CACHE_TIME);
	}

	private void registerFcmServer(Context context) {
		Set<String> ids = new HashSet<>();

		Object object = readObject(context, Define.scheduleFilename);
		Schedule schedule = new Schedule();

		if ( object != null ) {
			schedule = (Schedule) object;
		}

		if (getMySchedule(context).getIds().size() > 0) {
			ids = getMySchedule(context).getIds();
		} else if (schedule.getLectures().size() > 0) {
			for (LectureItem li : schedule.getLectures()) {
				// TODO ID muss splusname werden
				ids.add(String.valueOf(li.getId()));
			}
		} else {
			return;
		}

		RegisterLectures regLeg = new RegisterLectures();

		// TODO ID der Vorlesungen holen und dan Methode übergeben
		regLeg.registerLectures(ids);
	}
}
