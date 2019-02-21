/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget.data

import java.io.Serializable
import de.hof.university.app.util.Define.WIDGET_MODE_SCHEDULE

/**
 * The data class for all widget-specific-settings.
 * Will be connected to #AppWidgetId by key-value-pair in [AppWidgetDataCache].
 *
 * @see AppWidgetDataCache.widgetSettings
 * @see de.hof.university.app.widget.AppWidgetConfigureActivity - first init ( uses default-value for [widgetMode] )
 *
 * @see Serializable :
 *  - serialized in [AppWidgetDataCache.saveWidgetSettings]
 *  - deserialized in [AppWidgetDataCache.initWidgetSettingsIfNotAlreadyDone]
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
data class AppWidgetSettingsHolder(
		val lightStyleIsSelected: Boolean,
		val sharpStyleIsSelected: Boolean,
		var widgetMode: Int = WIDGET_MODE_SCHEDULE,
		var titleSize: Float? = null
) : Serializable {

	// ..when a WidgetMode gets changed the TitleSize shall be calculated
	fun replaceWidgetMode(mode: Int) {
		titleSize = null
		widgetMode = mode
	}
}