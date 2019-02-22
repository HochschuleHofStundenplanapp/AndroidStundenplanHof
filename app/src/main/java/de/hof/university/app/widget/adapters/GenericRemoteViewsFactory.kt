/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import de.hof.university.app.R
import de.hof.university.app.widget.data.AppWidgetDataCache
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

/**
 * What is this?
 * Similar to [de.hof.university.app.adapter] this is used as a 'special' Adapter for a ListView in a Widget,
 * this is the base implementation for a RemoteViewFactory which full functionality is gained by a final implementation of it.
 *
 * What is a RemoteViewFactory?
 * An interface for an adapter between a remote collection view (ListView, GridView, etc) and the underlying data for that view.
 * The implementor is responsible for making a RemoteView for each item in the data set. This interface is a thin wrapper around Adapter.
 * @see RemoteViewsService.RemoteViewsFactory - https://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory
 *
 * Whats does the abstract-class do?
 * storing the data, adding the 'lastSaved'-Element and requesting at the correct position,
 * basic-functionality (i.e. [getCount], [hasStableIds], ... ) while supplying concrete functions to implementations (i.e. [setData], [getRemoteView], ... )
 *
 * What does a implementation of it do?
 * Offering a [context], a [amountOfViewTypes] and obviously a [DATATYPE]. When [getRemoteView] is called the requested [DATATYPE] for this will be delivered.
 * When [setData] or [getRemoteViewForLastSaved] is called the [AppWidgetDataCache]-instance will be delivered, requested shortly before to full-fill the task.
 * For more information on abstract-declared functions read their comments
 *
 * !! CAUTION !!
 * Avoid any [de.hof.university.app.data.DataManager.getInstance]-Calls (Reason can be found in description of [AppWidgetDataCache])
 * They are hidden in [de.hof.university.app.model] !
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
internal abstract class GenericRemoteViewsFactory<DATATYPE>(
	internal val context: Context,
	lightStyleIsSelected: Boolean,
	private val amountOfViewTypes: Int
	): RemoteViewsService.RemoteViewsFactory {

	/**
	 * HELPER-VARS
	 */
	internal val packageName = context.packageName
	private var data: ArrayList<DATATYPE> = setDataAndSize()
	private var dataSizePlusOne: Int = 0
	private var injectedCount = 0
	internal val primaryTextColor: Int
	internal val secondaryTextColor: Int
	internal val dissatisfiedIcon: Int

	init {
		if(lightStyleIsSelected) {
			primaryTextColor = ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Primary_For_LightStyle)
			secondaryTextColor = ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Secondary_For_LightStyle)
			dissatisfiedIcon = R.drawable.ic_baseline_search_light_24
		} else {
			primaryTextColor = ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Primary_For_DarkStyle)
			secondaryTextColor = ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Secondary_For_DarkStyle)
			dissatisfiedIcon = R.drawable.ic_baseline_search_dark_24
		}
	}

	/**
	 * HELPER-FUNS
	 */
	internal fun Date.toDayString(): String = dateFormatter.format(this)

	internal fun Date.toHourString(): String = hourFormatter.format(this)

	/**
	 * COMPANION
	 */
	private companion object {
		@SuppressLint("SimpleDateFormat") // default local will be used
		private val dateFormatter = SimpleDateFormat("dd.MM.yyyy, HH:mm ")
		@SuppressLint("SimpleDateFormat") // default local will be used
		private val hourFormatter = SimpleDateFormat("HH:mm")
	}

	/**
	 * OVERRIDES
	 */
	override fun onDataSetChanged() { data = setDataAndSize() }

	private fun setDataAndSize() = setData(AppWidgetDataCache.getInstance()).also{ dataSizePlusOne = it.size + 1}.also { injectedCount = injectCount(dataSizePlusOne - 1) }

	override fun onCreate() {}

	override fun onDestroy() {}

	override fun getCount(): Int = dataSizePlusOne + injectedCount // i.e. (orginal + lastSaved) + (emptyView) for Changes

	open fun injectCount(size: Int):Int = 0

	override fun hasStableIds(): Boolean = true

	override fun getItemId(position: Int): Long = position.toLong()

	override fun getLoadingView(): RemoteViews = RemoteViews(packageName, R.layout.widget_list_item_loading).apply { setTextColor(R.id.widget_list_item_loading, primaryTextColor) }

	override fun getViewAt(position: Int): RemoteViews
		= when {
			position < dataSizePlusOne - 1 - injectedCount -> getRemoteView(data[position])
			position == 0 && dataSizePlusOne == 1 -> getRemoteViewForEmptyView()
			else -> getRemoteViewForLastSaved(AppWidgetDataCache.getInstance())
		}

	override fun getViewTypeCount(): Int = amountOfViewTypes

	/**
	 * ABSTRACT-FUNS
	 */

	/**
	 * Function to set the [data] of type [ArrayList]<GIVEN_DATA_TYPE> used.
	 *
	 * @param dataCache - [AppWidgetDataCache]-Object delivered
	 *
	 * @return the [ArrayList] full of data which should be used
	 */
	internal abstract fun setData(dataCache: AppWidgetDataCache): ArrayList<DATATYPE>

	/**
	 * Function to initialize a [RemoteViews] at position of the ListView.
	 *
	 * @param data - the Data-Object which should be the source for the RemoteViews
	 *
	 * @return the [RemoteViews]-Objects used in the ListView a 'regular'-item
	 */
	internal abstract fun getRemoteView(data: DATATYPE): RemoteViews

	/**
	 * Function to initialize a [RemoteViews] at the last position of the ListView.
	 *
	 * @param dataCache - [AppWidgetDataCache]-Object delivered
	 *
	 * @return the [RemoteViews]-Objects used in the ListView for a 'LastSaved'-Item
	 */
	internal abstract fun getRemoteViewForLastSaved(dataCache: AppWidgetDataCache): RemoteViews

	/**
	 * Function to initalize a [RemoteViews] if the [data] was empty
	 *
	 * @return the [RemoteViews]-Objects used in the ListView to indicate a empty [data]
	 */
	internal abstract fun getRemoteViewForEmptyView(): RemoteViews
}
