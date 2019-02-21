/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget.adapters

import android.content.Context
import android.widget.RemoteViews
import de.hof.university.app.R
import de.hof.university.app.model.schedule.LectureChange
import de.hof.university.app.widget.AppWidgetBroadcastReceiver
import de.hof.university.app.widget.data.AppWidgetDataCache
import java.util.*

/**
 * @see [GenericRemoteViewsFactory] for description
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
internal class AppWidgetRemoteViewsFactoryChanges(context: Context, lightStyleIsSelected: Boolean): GenericRemoteViewsFactory<Any>(context, lightStyleIsSelected, 3) {

	override fun setData(dataCache: AppWidgetDataCache): ArrayList<Any> = dataCache.getChangesData(context)

	override fun injectCount(size: Int): Int = if(size == 0) 1 else 0

	/**
	 * TODO: add getRemoteViewFor???-function for missing entry (see below)
	 * not sure why / when / how [de.hof.university.app.model.schedule.Changes] does not contain LectureChange-objects but in case inform user about that
	 */
	override fun getRemoteView(data: Any): RemoteViews
		= when (data) {
			is LectureChange -> getRemoteViewForLectureChange(data)
			else -> RemoteViews(packageName, R.layout.widget_list_item_changes_error).apply { setTextColor(R.id.widget_list_item_changes_error_text, primaryTextColor) }
			// else -> throw Exception("AppWidgetRemoteViewsFactoryChanges: Unsupported Item in getRemoteView()") // add after todo
		}

	private fun getRemoteViewForLectureChange(data: LectureChange): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_changes).apply {
			with(data) {
				setTextViewText(R.id.widget_list_item_changes_details, details)
				setTextColor(R.id.widget_list_item_changes_details, primaryTextColor)
				setTextViewText(R.id.widget_list_item_changes_important, text)
				//setTextColor(R.id.widget_list_item_changes_important, secondaryTextColor)
				setTextViewText(R.id.widget_list_item_changes_old_date, "${begin_old.toHourString()} - ${begin_old.toHourString()}")
				setTextColor(R.id.widget_list_item_changes_old_date, primaryTextColor)
				setTextViewText(R.id.widget_list_item_changes_new_date, "${begin_new.toHourString()} - ${begin_new.toHourString()}")
				//setTextColor(R.id.widget_list_item_changes_new_date, secondaryTextColor)
			}
		}

	override fun getRemoteViewForLastSaved(dataCache: AppWidgetDataCache): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_last_saved).apply {
			setTextViewText(R.id.widget_list_item_last_updated,
			"${context.getString(R.string.lastUpdated)}: ${with(dataCache.getChangesLastSaved(context)) { this?.toDayString() ?: "---" }}" +
				"\n${context.getString(R.string.appwidget_update_instructions_changes)}"
			)
			setTextColor(R.id.widget_list_item_last_updated, primaryTextColor)
		}

	override fun getRemoteViewForEmptyView(): RemoteViews
		= RemoteViews(packageName, R.layout.widget_list_item_empty).apply {
			setTextViewText(R.id.widget_list_item_empty_message, context.getString(R.string.appwidget_list_item_empty_changes))
			setTextColor(R.id.widget_list_item_empty_message, primaryTextColor)
			setInt(R.id.widget_list_item_empty_ic, AppWidgetBroadcastReceiver.FOR_BACKGROUND, dissatisfiedIcon)
		}
}