/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget.data

import de.hof.university.app.util.Define
import de.hof.university.app.model.schedule.Changes
import de.hof.university.app.model.schedule.LectureItem
import de.hof.university.app.model.schedule.MySchedule
import de.hof.university.app.model.schedule.Schedule
import java.util.Date
import android.util.Log
import android.content.Context
import de.hof.university.app.widget.AppWidgetBroadcastReceiver
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.lang.Exception

/**
 *                           **************************************************************************
 *                           *                              !! CAUTION !!                             *
 *                           *                  !! DO NOT USE THIS TO GET YOUR DATA !!                *
 *                           *  !! THIS WILL BE AND SHOULD ONLY AVAILABLE IF A WIDGET IS ACTIVE !!    *
 *                           **************************************************************************
 *
 * What is this?
 * Similar to [de.hof.university.app.data.DataManager] this is used to cache data which should be shown in a widget,
 * to supply x-amount of widgets it follows the singleton-pattern (see [getInstance])
 *
 * Why is this?
 * If you look inside [de.hof.university.app.data.DataManager]-constructor you will find that the context used to set the SharedPreference-value
 * is based on [de.hof.university.app.MainActivity], since widgets do not require this Activity to be launched at all a [ExceptionInInitializerError]
 * is thrown since the context is null - which itself is to hard to handle without fundamental modifications to [de.hof.university.app.data.DataManager].
 * Instead of doing these modifications this class fits the needs while being smaller.
 *
 * What does this?
 * Caching any Data needed to display a Widget including WidgetSettings ( which gets fully managed by this - including creation / saving / deleting / mapping ).
 * No Schedule, MySchedule, Changes -Data will and should be written by this - only read-access on these.
 *
 * @reacts_on [android.content.Intent.ACTION_SHUTDOWN] - see for more [AppWidgetBroadcastReceiver]
 * @reacts_on [android.content.Intent.ACTION_BOOT_COMPLETED] - see for more [AppWidgetBroadcastReceiver]
 *
 * @constructor Singleton-Pattern [getInstance]
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
class AppWidgetDataCache private constructor() {

	/**
	 * Data
	 */
	private var scheduleDataCache: ArrayList<LectureItem>? = null // cached scheduleLectureItems
	private var scheduleLastSaved: Date? = null // date of last modification to ^
	private var myScheduleDataCache: ArrayList<LectureItem>? = null // cached myScheduleLectureItems
	private var myScheduleLastSaved: Date? = null // date of last modification to ^
	private var changesDataCache: ArrayList<Any>? = null // cached lectureChangeItems
	private var changesLastSaved: Date? = null // date of last modification to ^

	private lateinit var widgetSettings: MutableMap<Int, AppWidgetSettingsHolder> // a map full of AppWidgetSettingsHolder as #value where AppWidgetId is the #key

	/**
	 * Companion
	 */
	companion object {
		private const val TAG = "AppWidgetDataCache"
		private const val CONFIG_FILE_NAME = "AppWidgetConfig" // the filename for WidgetSettings

		private var instance: AppWidgetDataCache? = null // the instance

		fun getInstance(): AppWidgetDataCache = instance ?: run { instance = AppWidgetDataCache(); instance!!}

		fun hasInstance() = instance != null

		// !! CAUTION !!
		// called to delete any WidgetSettings immediate, including cleaning up this -> will cause problems when called while a AppWidget is active
		internal fun cleanUp(context: Context)
			= instance?.apply {
				try { File(context.filesDir, CONFIG_FILE_NAME).delete() } catch (e: SecurityException) {/* fine */}

				scheduleDataCache = null; myScheduleDataCache = null; changesDataCache = null
				scheduleLastSaved = null; myScheduleLastSaved = null; changesLastSaved = null
				if(::widgetSettings.isInitialized) widgetSettings.clear()

				instance = null
			}
	}

	/**
	 * Data-Modification from outside
	 *
	 * @see [de.hof.university.app.data.DataManager.getSchedule]
	 * @see [de.hof.university.app.data.DataManager.getMySchedule]
	 * @see [de.hof.university.app.data.DataManager.getChanges]
	 */
	fun shareScheduleData(context: Context, data: ArrayList<LectureItem>, lastSaved: Date) {
		scheduleDataCache = data
		scheduleLastSaved = lastSaved
		AppWidgetBroadcastReceiver.informAllWidgetsDataChanged(context)
	}

	fun shareMyScheduleData(context: Context, data: ArrayList<LectureItem>?, lastSaved: Date?) {
		myScheduleDataCache = data
		myScheduleLastSaved = lastSaved
		AppWidgetBroadcastReceiver.informAllWidgetsDataChanged(context)
	}

	fun shareChangesData(context: Context, data: ArrayList<Any>, lastSaved: Date) {
		changesDataCache = data
		changesLastSaved = lastSaved
		AppWidgetBroadcastReceiver.informAllWidgetsDataChanged(context)
	}

	/**
	 * Data-Access from inside
	 *
	 * @see [de.hof.university.app.widget.adapters]
	 */
	internal fun getScheduleData(context: Context): ArrayList<LectureItem> = scheduleDataCache ?: grabScheduleData(context)

	internal fun getMyScheduleData(context: Context): ArrayList<LectureItem> = myScheduleDataCache ?: grabMyScheduleData(context)

	internal fun getChangesData(context: Context): ArrayList<Any> = changesDataCache ?: grabChangesData(context)

	internal fun getScheduleLastSaved(context: Context): Date? = scheduleLastSaved ?: grabScheduleData(context).run{scheduleLastSaved}

	internal fun getMyScheduleLastSaved(context: Context): Date? = myScheduleLastSaved ?: grabMyScheduleData(context).run{myScheduleLastSaved}

	internal fun getChangesLastSaved(context: Context): Date? = changesLastSaved ?: grabChangesData(context).run{changesLastSaved}

	private fun grabMyScheduleData(context: Context): ArrayList<LectureItem>
		= grabData < MySchedule, ArrayList<LectureItem> > ( context, Define.myScheduleFilename,
				success = { it.run{ myScheduleLastSaved = it.lastSaved; it.lectures }},
				failure = { ArrayList() }
		)

	private fun grabScheduleData(context: Context): ArrayList<LectureItem>
		= grabData < Schedule, ArrayList<LectureItem> > ( context, Define.scheduleFilename,
				success = { it.run{ scheduleLastSaved = it.lastSaved; it.lectures }},
				failure = { ArrayList() }
		)

	private fun grabChangesData(context: Context): ArrayList<Any>
		= grabData < Changes, ArrayList<Any> > ( context, Define.changesFilename,
				success = { it.run{ changesLastSaved = it.lastSaved; it.changes }},
				failure = { ArrayList() }
		)

	/**
	 * Widget-Settings-Functionality
	 *
	 * @see [AppWidgetSettingsHolder]
	 */
	internal fun getWidgetSettingsFor(context: Context, appwidgetId: Int): AppWidgetSettingsHolder?
		= initWidgetSettingsIfNotAlreadyDone(context).run { widgetSettings[appwidgetId] }

	internal fun putWidgetSettingsFor(context: Context, appWidgetId: Int, settings: AppWidgetSettingsHolder)
		= initWidgetSettingsIfNotAlreadyDone(context).run {
			widgetSettings[appWidgetId] = settings
			// also inititate a write-cycle to ensure at least some data is available before shutdown
			saveWidgetSettings(context)
		}

	internal fun removeWidgetSettingsFor(context: Context, appWidgetId: Int)
			= initWidgetSettingsIfNotAlreadyDone(context).let { widgetSettings.remove(appWidgetId) }

	internal fun saveWidgetSettings(context: Context) {
		if(::widgetSettings.isInitialized)
			try { FileOutputStream(File(context.filesDir, CONFIG_FILE_NAME)).use { fos -> ObjectOutputStream(fos).use { oos ->
				oos.writeObject(widgetSettings)
			} } } catch (e: Exception) { Log.e(TAG, "Failed writing $CONFIG_FILE_NAME", e)}
	}

	private fun initWidgetSettingsIfNotAlreadyDone(context: Context) {
		if (!::widgetSettings.isInitialized || widgetSettings.isEmpty())
			widgetSettings = grabData < MutableMap<Int,AppWidgetSettingsHolder> , MutableMap<Int,AppWidgetSettingsHolder> > ( context, CONFIG_FILE_NAME,
					success = { it },
					failure = { mutableMapOf() }
			)
	}

	/**
	 * HELPER-FUN
	 */
	private inline fun <reified TARGET, RESULT> grabData(context: Context, fileName: String, success: (TARGET) -> RESULT, failure: () -> RESULT): RESULT{
		try {
			File(context.filesDir, fileName).apply {
				if (exists()) FileInputStream(this).use { fis -> ObjectInputStream(fis).use { ois ->
					ois.readObject()?.let {
						if (it is TARGET) { return success(it) }
					}
				} }
			}
		} catch (e: Exception) { Log.e(TAG, "Failed reading $fileName", e)}
		return failure()
	}

	/**
	 * NOT-SURE-WHEN-THIS-EVER-BE-CALLED-FUN
	 */
	internal fun remapWidgetIds(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray)
		= initWidgetSettingsIfNotAlreadyDone(context).also {
			widgetSettings.apply {
				oldWidgetIds.forEachIndexed{ index, old->
					put(newWidgetIds[index], get(old)!!)
					remove(old)
				}
			}
		}
}