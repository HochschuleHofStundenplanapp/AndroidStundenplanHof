/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget

import android.content.Intent
import android.widget.RemoteViewsService
import de.hof.university.app.widget.adapters.AppWidgetRemoteViewsFactoryChanges
import de.hof.university.app.widget.adapters.AppWidgetRemoteViewsFactoryMySchedule
import de.hof.university.app.widget.adapters.AppWidgetRemoteViewsFactorySchedule
import de.hof.university.app.util.Define.INTENT_EXTRA_WIDGET_MODE
import de.hof.university.app.util.Define.WIDGET_MODE_CHANGES
import de.hof.university.app.util.Define.WIDGET_MODE_INVALID
import de.hof.university.app.util.Define.WIDGET_MODE_MY_SCHEDULE
import de.hof.university.app.util.Define.WIDGET_MODE_SCHEDULE
import de.hof.university.app.widget.AppWidgetBroadcastReceiver.Companion.INTENT_EXTRA_LIGHT_STYLE_SELECTED
import java.lang.Exception

/**
 * The service to be connected to for a remote adapter to request RemoteViews.
 * Users should extend the RemoteViewsService to provide the appropriate RemoteViewsFactory's used to populate the remote collection view (ListView, GridView, etc).
 * @see RemoteViewsService - https://developer.android.com/reference/android/widget/RemoteViewsService
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
class AppWidgetRemoteViewService : RemoteViewsService() {

	/**
	 * This is called by the System & this Services will be created or destroyed by the System asap (usualy after n-Seconds where this is not in use)
	 * --> produces some Lag infavor of RAM/CPU
	 *
	 * @param intent - The Intent fired from the Widget when it needs a Adapter, set by the [AppWidgetBroadcastReceiver]
	 * @see AppWidgetBroadcastReceiver.updateAppWidget
	 *
	 * @see de.hof.university.app.util.Define for constants, for better readability directly imported
	 */
	override fun onGetViewFactory(intent: Intent): RemoteViewsFactory
		= if(intent.extras != null) intent.extras!!.run {
			getBoolean(INTENT_EXTRA_LIGHT_STYLE_SELECTED).let { lightStyleIsSelected ->
				when (getInt(INTENT_EXTRA_WIDGET_MODE, WIDGET_MODE_INVALID)) {

					WIDGET_MODE_CHANGES -> AppWidgetRemoteViewsFactoryChanges(applicationContext, lightStyleIsSelected)
					WIDGET_MODE_MY_SCHEDULE -> AppWidgetRemoteViewsFactoryMySchedule(applicationContext, lightStyleIsSelected)
					WIDGET_MODE_SCHEDULE -> AppWidgetRemoteViewsFactorySchedule(applicationContext, lightStyleIsSelected)

					else -> throw Exception("AppWidgetRemoteViewService: Unsupported WidgetMode = ${getInt(INTENT_EXTRA_WIDGET_MODE, WIDGET_MODE_INVALID)} in onGetViewFactory()")
				} }
		} else throw Exception("AppWidgetRemoteViewService: Empty extras in onGetViewFactory()")
}