/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget.adapters

import android.content.Context
import android.widget.RemoteViews
import de.hof.university.app.R
import de.hof.university.app.model.schedule.LectureItem
import de.hof.university.app.widget.AppWidgetBroadcastReceiver
import de.hof.university.app.widget.data.AppWidgetDataCache
import java.lang.Exception
import java.util.ArrayList

/**
 * @see [GenericRemoteViewsFactory] for description
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
internal class AppWidgetRemoteViewsFactoryMySchedule(context: Context, lightStyleIsSelected: Boolean)
	: GenericRemoteViewsFactory<Any>(context, lightStyleIsSelected, 3){

	override fun setData(dataCache: AppWidgetDataCache): ArrayList<Any> {
		var tmpWeekDayString = ""
		return ArrayList<Any>().apply {
			dataCache.getMyScheduleData(context).forEach{ i -> i.weekday.also { d ->
				if(d != tmpWeekDayString) add(d)
				add(i)
				tmpWeekDayString = d
			} }
		}
	}

	override fun getRemoteView(data: Any): RemoteViews
		= when (data) {
			is String -> getRemoteViewForWeekday(data)
			is LectureItem -> getRemoteViewForLectureItem(data)
			else -> throw Exception("AppWidgetRemoteViewsFactoryMySchedule: Unsupported Item in getRemoteView()")
		}

	private fun getRemoteViewForLectureItem(lectureItem: LectureItem): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_schedule).apply {
			with(lectureItem) {
				setTextViewText(R.id.widget_list_item_schedule_time, "${startDate.toHourString()} - ${endDate.toHourString()}")
				setTextColor(R.id.widget_list_item_schedule_time, primaryTextColor)
				setTextViewText(R.id.widget_list_item_schedule_room, room)
				setTextColor(R.id.widget_list_item_schedule_room, primaryTextColor)
				setTextViewText(R.id.widget_list_item_schedule_details, details)
				setTextColor(R.id.widget_list_item_schedule_details, primaryTextColor)
			}
		}

	private fun getRemoteViewForWeekday(weekDay: String): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_big).apply {
			setTextViewText(R.id.widget_list_item_big_text, weekDay)
			setTextColor(R.id.widget_list_item_big_text, secondaryTextColor)
		}

	override fun getRemoteViewForLastSaved(dataCache: AppWidgetDataCache): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_last_saved).apply {
			setTextViewText(R.id.widget_list_item_last_updated,
					"${context.getString(R.string.lastUpdated)}: ${with(dataCache.getMyScheduleLastSaved(context)) { this?.toDayString() ?: context.getString(R.string.appwidget_list_item_default_empty_entry) }}" +
							"\n${context.getString(R.string.appwidget_update_instructions_changes)}"
			)
			setTextColor(R.id.widget_list_item_last_updated, primaryTextColor)
		}

	override fun getRemoteViewForEmptyView(): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_empty).apply {
			setTextViewText(R.id.widget_list_item_empty_message,
					context.getString(R.string.appwidget_list_item_empty_my_schedule)
			)
			setTextColor(R.id.widget_list_item_empty_message, primaryTextColor)
			setInt(R.id.widget_list_item_empty_ic, AppWidgetBroadcastReceiver.FOR_BACKGROUND, dissatisfiedIcon)
		}
}
