/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import de.hof.university.app.MainActivity
import de.hof.university.app.widget.data.AppWidgetDataCache
import de.hof.university.app.widget.data.AppWidgetSettingsHolder
import java.lang.Exception
import android.content.Intent.ACTION_LOCKED_BOOT_COMPLETED
import android.content.Intent.ACTION_SHUTDOWN
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import de.hof.university.app.R
import de.hof.university.app.util.Define.WIDGET_INTENT_SHOW_FRAGMENT
import de.hof.university.app.util.Define.INTENT_EXTRA_WIDGET_MODE
import de.hof.university.app.util.Define.WIDGET_MODE_CHANGES
import de.hof.university.app.util.Define.WIDGET_MODE_MY_SCHEDULE
import de.hof.university.app.util.Define.WIDGET_MODE_SCHEDULE

/**
 * What is this?
 * A [android.content.BroadcastReceiver] to control AppWidgets.
 * The Main-Controlling-Part of AppWidgets.
 *
 * What is a AppWidgetProvider?
 * A convenience class to aid in implementing an AppWidget provider. Everything you can do with AppWidgetProvider,
 * you can do with a regular BroadcastReceiver. AppWidgetProvider merely parses the relevant fields out of the Intent
 * that is received in onReceive(Context,Intent), and calls hook methods with the received extras.
 *
 * See descriptions for some insights.
 *
 * @see [de.hof.university.app.R.xml.widget_definition]
 *
 * @reactsTo ACTION_SHUTDOWN (also HTC-Devices)
 * @reactsTo ACTION_WIDGET_BUTTON_CLICKED
 * @reactsTo ACTION_LOCKED_BOOT_COMPLETED
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
class AppWidgetBroadcastReceiver : AppWidgetProvider() {

	/**
	 * OVERRIDES
	 */
	@SuppressLint("InlinedApi")
	/**
	 * The onReceive-Method
	 * Notice: All usual Widget-Related-Intents will be received by super to call this overridden-functions (with some functionality to get params for each)
	 * @see [AppWidgetProvider.onReceive]
	 *
	 * @param context - The Context in which the receiver is running.
	 * @param intent - The Intent being received.
	 */
	override fun onReceive(context: Context, intent: Intent)
		= when(intent.action) {
			ACTION_WIDGET_BUTTON_CLICKED // a widget button was clicked
				-> onWidgetButtonClicked(context, intent)

			ACTION_SHUTDOWN, "android.intent.action.QUICKBOOT_POWEROFF" // time to save widget-settings // didn't find another suitable solution w/o wasting write-cycles
				-> appWidgetDataCache.saveWidgetSettings(context)

			// only Api 24+ -> for lower regular "ACTION_BOOT_COMPLETED" which itself results eventually in "ACTION_APPWIDGET_UPDATE"
			ACTION_LOCKED_BOOT_COMPLETED
				-> with(AppWidgetManager.getInstance(context)) {onUpdate(context, this, getAllWidgetIds(context, this))}

			else -> super.onReceive(context, intent)
		}

	/**
	 * Called in response to ACTION_APPWIDGET_UPDATE && ACTION_APPWIDGET_RESTORED
	 * --> [AppWidgetManager.updateAppWidget] is the targeted function after this, in between the [RemoteViews] will be set up using [updateAppWidget]
	 * --> preferably in a static-function
	 *
	 * @param context - The Context in which this receiver is running.
	 * @param appWidgetManager - A AppWidgetManager-object you can call AppWidgetManager.updateAppWidget on.
	 * @param appWidgetIds -  The appWidgetIds for which an update is needed.
	 */
	override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray)
		= appWidgetIds.forEach { updateAppWidget(context, it, appWidgetManager) }

	/**
	 * Called in response to the ACTION_APPWIDGET_DELETED when one or more AppWidget instances have been deleted.
	 *
	 * @param context - The Context in which this receiver is running.
	 * @param appWidgetIds -  The appWidgetIds for which an update is needed.
	 */
	override fun onDeleted(context: Context, appWidgetIds: IntArray)
		= appWidgetIds.forEach{ appWidgetDataCache.removeWidgetSettingsFor(context, it) }

	/**
	 * Called in response to ACTION_APPWIDGET_ENABLED when the first Widget using this Provider was created
	 *
	 * @param context - The Context in which this receiver is running.
	 */
	override fun onEnabled(context: Context) { /* nothing to do */ }

	/**
	 * Called in response to ACTION_APPWIDGET_DISABLED when the last Widget using this Provider was deleted
	 *
	 * @param context - The Context in which this receiver is running.
	 */
	override fun onDisabled(context: Context) { AppWidgetDataCache.cleanUp(context)}

	/**
	 * Called in response to ACTION_APPWIDGET_RESTORED when instances of this Provider has been restored from backup
	 * --> will be followed immediately by a call to [AppWidgetBroadcastReceiver.onUpdate]
	 *
	 * @param context - The Context in which this receiver is running.
	 */
	override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray)
		= appWidgetDataCache.remapWidgetIds(context, oldWidgetIds, newWidgetIds) // not sure when this will be called or if at all

	/**
	 * Called in response to ACTION_APPWIDGET_OPTIONS_CHANGED when this widget has been layed out at a new size.
	 * --> [AppWidgetManager.updateAppWidget] is the targeted function after this
	 *
	 * @param context - The Context in which this receiver is running.
	 * @param appWidgetManager - A AppWidgetManager object you can call AppWidgetManager#updateAppWidget on.
	 * @param appWidgetId - The appWidgetId of the widget whose size changed.
	 * @param options - The appWidgetId of the widget whose size changed.
	 */
	override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, options: Bundle)
		= updateAppWidget(context, appWidgetId, appWidgetManager, options)

	/**
	 * HELPER-FUNS
	 */
	/**
	 * Changes the [AppWidgetSettingsHolder.widgetMode] & calls [updateAppWidget]
	 *
	 * @param context - The Context in which this receiver is running.
	 * @param intent - The Intent fired by a WidgetButton ([R.id.widget_header_next] with buttonId [BUTTON_NEXT] or [R.id.widget_header_prev] wiht buttonId [BUTTON_PREV])
	 */
	private fun onWidgetButtonClicked(context: Context, intent: Intent) {
		intent.extras?.run {
			val appWidgetId = getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
			val buttonId = getInt(INTENT_EXTRA_BUTTON_CLICKED, BUTTON_INVALID)
			if( appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&  buttonId != BUTTON_INVALID ) {
				changeWidgetModeFor(context, appWidgetId, buttonId)
				updateAppWidget(context, appWidgetId)
			}
		}
	}

	/**
	 * Changes the [AppWidgetSettingsHolder.widgetMode] to the correct value using [AppWidgetSettingsHolder.replaceWidgetMode],
	 * will change the WidgetMode by default to [WIDGET_MODE_SCHEDULE].
     *
	 * @param context - The Context in which this receiver is running.
	 * @param appWidgetId - The WidgetId to change the WidgetMode for.
	 * @param appWidgetButtonId - The ButtonId which was pressed in the Widget.
	 */
	private fun changeWidgetModeFor(context: Context, appWidgetId: Int, appWidgetButtonId: Int)
		= appWidgetDataCache.run{ getWidgetSettingsFor(context, appWidgetId) ?: return }.run {
			replaceWidgetMode( when (appWidgetButtonId) {
				BUTTON_NEXT -> when(widgetMode) {
					WIDGET_MODE_SCHEDULE -> WIDGET_MODE_MY_SCHEDULE
					WIDGET_MODE_CHANGES -> WIDGET_MODE_SCHEDULE
					WIDGET_MODE_MY_SCHEDULE -> WIDGET_MODE_CHANGES
					else -> WIDGET_MODE_SCHEDULE
				}

				BUTTON_PREV -> when(widgetMode) {
					WIDGET_MODE_SCHEDULE -> WIDGET_MODE_CHANGES
					WIDGET_MODE_CHANGES -> WIDGET_MODE_MY_SCHEDULE
					WIDGET_MODE_MY_SCHEDULE -> WIDGET_MODE_SCHEDULE
					else -> WIDGET_MODE_SCHEDULE
				}
				else -> throw Exception("Unsupported WidgetModeChange: for AppWidgetId = '$appWidgetId' with unsupported Button = '$appWidgetButtonId' ")
			})
		}

	/**
	 * COMPANION
	 */
	companion object {
		private val appWidgetDataCache = AppWidgetDataCache.getInstance() // see [AppWidgetDataCache]

		// used to identify what widget button was pressed to inform this.AppWidgetBroadcastReceiver about it (see BroadcastReceiver.onReceive)
		private const val ACTION_WIDGET_BUTTON_CLICKED = "de.hof.university.app.widget.ACTION_WIDGET_BUTTON_CLICKED" // intent.action used for a Button_Click
		private const val INTENT_EXTRA_BUTTON_CLICKED = "EXTRA_BUTTON_CLICKED" // intent.extra.key used for the Button_Click
		private const val BUTTON_INVALID = -1 // intent.extra.value used to indicate a non valid value (used as default-value)
		private const val BUTTON_NEXT = 1 // intent.extra.value used to indicate that the Next_Button was pressed && used as intent.flag
		private const val BUTTON_PREV = 2 // intent.extra.value used to indicate that the Prev_Button was pressed && used as intent.flag

		// used to style widget items, passed through intent // see [AppWidgetRemoteViewService]
		internal const val INTENT_EXTRA_LIGHT_STYLE_SELECTED = "INTENT_EXTRA_LIGHT_STYLE_SELECTED"

		/**
		 * COMPANION-FUNS
		 */
		/**
		 * Will return a IntArray of all AppWidgetId's which are controlled by [AppWidgetManager].
		 *
		 * @param context - A Context
		 * @param appWidgetManager - The AppWidgetManager-Instance which is used by Android to controll Widgets
         *
         * @return a IntArray of AppWidgetId's
		 */
		private fun getAllWidgetIds(context: Context, appWidgetManager: AppWidgetManager): IntArray
			= appWidgetManager.getAppWidgetIds(ComponentName(context, AppWidgetBroadcastReceiver::class.java))

		/**
		 * Will notify all Widgets-ListViews to update their Data using [AppWidgetManager.notifyAppWidgetViewDataChanged] on all AppWidget's.
		 *
		 * @param context - A Context
		 */
		internal fun informAllWidgetsDataChanged(context: Context)
			= with(AppWidgetManager.getInstance(context)) { notifyAppWidgetViewDataChanged(getAllWidgetIds(context, this), R.id.widget_listview) }

		/**
         * Will initialy save the [AppWidgetSettingsHolder] for the AppWidget and afterwards will [updateAppWidget].
		 *
		 * @param context - The Context where the [AppWidgetConfigureActivity] runs
		 * @param appWidgetId - The appWidgetId which settings where created
		 * @param settings - The [AppWidgetSettingsHolder] containing the settings which should be applied to the AppWidget
		 */
		internal fun updateNewAppWidget(context: Context, appWidgetId: Int, settings: AppWidgetSettingsHolder) {
			appWidgetDataCache.putWidgetSettingsFor(context, appWidgetId, settings)
			updateAppWidget(context, appWidgetId)
		}

		/**
		 * THE UpdateAppWidget-Function !
		 *
		 * Will require a corresponding [AppWidgetSettingsHolder] for styling. --> [applyWidgetStyle]
		 * Will be a [de.hof.university.app.widget.adapters] set on with the help of the [AppWidgetRemoteViewService].
		 * Will calculate the WidgetHeader-Title-Size ([R.id.widget_header_section_title]) if needed. --> [calculateTextSize]
		 * Will be set on any OnClick's using PendingIntents.
		 *
         * @see [R.layout.widget_base_layout]
         *
		 * @param context - A Context
		 * @param appWidgetId - The appWidgetId identifying the Widget which should be updated
		 * @param appWidgetManager (optional) - The AppWidgetManager instance used to finally call [AppWidgetManager.updateAppWidget] on
		 * @param options (optional) - The options-Bundle which contains the new Sizes the Widget took
		 *
		 * @return A [RemoteViews] reflecting the View which should be shown where the Widget took place
		 *  --> see [developer.andorid: https://developer.android.com/guide/topics/appwidgets/]
		 */
		internal fun updateAppWidget(context: Context, appWidgetId: Int, appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context), options: Bundle? = null) {
			RemoteViews(context.packageName, R.layout.widget_base_layout).let {
				// settings required to go on
				val settings = appWidgetDataCache.getWidgetSettingsFor(context, appWidgetId) ?: return

				// style it
				applyWidgetStyle(it, settings.lightStyleIsSelected, settings.angularStyleIsSelected,
					if(settings.lightStyleIsSelected) {ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Primary_For_LightStyle) }
					else { ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Primary_For_DarkStyle) }
				)

				// set adapter
				it.setRemoteAdapter(R.id.widget_listview, Intent(context, AppWidgetRemoteViewService::class.java).apply {
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
					putExtra(INTENT_EXTRA_WIDGET_MODE, settings.widgetMode)
					putExtra(INTENT_EXTRA_LIGHT_STYLE_SELECTED, settings.lightStyleIsSelected)
					data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // necessary
				})

				// set title + titleSize
				it.setTextViewText(R.id.widget_header_section_title,
					with(context) { when(settings.widgetMode) {
						WIDGET_MODE_MY_SCHEDULE -> getString(R.string.appwidget_my_schedule)
						WIDGET_MODE_CHANGES -> getString(R.string.appwidget_schedule_changes)
						WIDGET_MODE_SCHEDULE -> getString(R.string.appwidget_schedule)
						else -> throw Exception("AppWidgetBroadcastReceiver: Unsupported WidgetMode in updateAppWidget() for ${settings.widgetMode}")
					}.apply {
						calculateTextSize(this, context, options, settings, appWidgetManager, appWidgetId).let { size ->
							it.setTextViewTextSize(R.id.widget_header_section_title, TypedValue.COMPLEX_UNIT_PX, size)
					} } }
				)

				// set header on click
				it.setOnClickPendingIntent(R.id.widget_header, PendingIntent.getActivity(context, 0,
					Intent(context, MainActivity::class.java).apply {
						putExtra(INTENT_EXTRA_WIDGET_MODE, settings.widgetMode)
						action = WIDGET_INTENT_SHOW_FRAGMENT
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
					}, PendingIntent.FLAG_CANCEL_CURRENT))

				// set next & prev buttons
				it.setOnClickPendingIntent(R.id.widget_header_next, getPendingSelfIntentForWidgetButton(context, BUTTON_NEXT, appWidgetId))
				it.setOnClickPendingIntent(R.id.widget_header_prev, getPendingSelfIntentForWidgetButton(context, BUTTON_PREV, appWidgetId))

				// call AppWidgetManager to continue with it
				appWidgetManager.updateAppWidget(appWidgetId, it)
			}
		}

		// used to modify RemoteView
		const val FOR_BACKGROUND = "setBackgroundResource"
		private const val FOR_TEXT_COLOR = "setTextColor"

		/**
		 * Will apply a 'Style' to the RemoteView based on the params [AppWidgetSettingsHolder.lightStyleIsSelected] & [AppWidgetSettingsHolder.angularStyleIsSelected]]
		 * Notice: This will only style the Widget's [R.layout.widget_base_layout].
		 * --> ListItems will be styled in the corresponding [de.hof.university.app.widget.adapters] and will be based on the same [AppWidgetSettingsHolder]
		 *
		 * @param rv - The RemoteView which should get styled
		 * @param lightStyleIsSelected - If light style shall be used
		 * @param angularStyleIsSelected - If angular corners shall be used
		 * @param primaryTextColor - The TextColor to style the [R.id.widget_header_section_title]
		 */
		private fun applyWidgetStyle(rv: RemoteViews, lightStyleIsSelected: Boolean, angularStyleIsSelected: Boolean, primaryTextColor: Int)
			= rv.apply {
				//style title-textView
				setInt(R.id.widget_header_section_title, FOR_TEXT_COLOR, primaryTextColor)
				//style the backgrounds
				if(lightStyleIsSelected) {
					if(angularStyleIsSelected) {
						setInt(R.id.widget_header, FOR_BACKGROUND, R.drawable.widget_background_header_white_sharp)
						setInt(R.id.widget_body, FOR_BACKGROUND, R.drawable.widget_background_body_white_sharp)
					} else {
						setInt(R.id.widget_header, FOR_BACKGROUND, R.drawable.widget_background_header_white_round)
						setInt(R.id.widget_body, FOR_BACKGROUND, R.drawable.widget_background_body_white_round)
					}
				} else {
					if(angularStyleIsSelected) {
						setInt(R.id.widget_header, FOR_BACKGROUND, R.drawable.widget_background_header_dark_sharp)
						setInt(R.id.widget_body, FOR_BACKGROUND, R.drawable.widget_background_body_dark_sharp)
					} else {
						setInt(R.id.widget_header, FOR_BACKGROUND, R.drawable.widget_background_header_dark_round)
						setInt(R.id.widget_body, FOR_BACKGROUND, R.drawable.widget_background_body_dark_round)
					}
				}
			}

		/**
		 * Will calculate the TextSize for [R.id.widget_header_section_title] based on the WidgetSizes
         * in it's current orientation: [Configuration.ORIENTATION_LANDSCAPE] || by default [Configuration.ORIENTATION_PORTRAIT]
		 *
		 * Will calculate the Size by taking the center of a minimum and maximum (instead of stepping N-dips) until a accuracy is reached
		 * --> AutoSize for AppCompatTextViews does not work on API 25 / 26 / <more> ; bug since Support-v4 26+
		 *
		 * @param titleString - The text of [R.id.widget_header_section_title] which should be displayed, selected in [updateAppWidget]
		 * @param context - A Context
		 * @param options - The Bundles passed to this method from [onAppWidgetOptionsChanged] containing WidgetSizes in [TypedValue.COMPLEX_UNIT_DIP]
		 * @param settings - The [AppWidgetSettingsHolder] for the Widget (only used to cache the TextSize)
		 * @param appWidgetManager - The AppWidgetManager-Instance
		 * @param appWidgetId - The AppWidgetId for the Widget which TextSize should be calculated
		 *
		 * @return should be the TextSize in [TypedValue.COMPLEX_UNIT_PX] which should actually fit
		 */
		private fun calculateTextSize(titleString: String, context: Context, options: Bundle?, settings: AppWidgetSettingsHolder, appWidgetManager: AppWidgetManager, appWidgetId: Int): Float {
			// stop if already cached only when not called from onAppWidgetOptionsChanged
			settings.titleSize?.let { if(options == null) return it }

			// prep calculation
			val displayMetrics = context.resources.displayMetrics
			val maxWidthPx: Int // the available width for the textView --> height can be ignored
			with(options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)) {
				maxWidthPx = Math.round(
					// maxWidth of Widget
					TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) }
						else { getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) }
								.toFloat(), displayMetrics)
						// - (2x 7dp:RootPadding + 8dp:HS-Dots + 50dp:MarginLeft +50dp:MarginRight) * 15%:AccuracyPenalty
						- TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140F, displayMetrics) // if something doesn't work out to inc/dec this is always a good starting point
				)
			}

			// the maximum for textSize
			var roofTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24F, displayMetrics)
			// the minimum for textSize
			var floorTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4F, displayMetrics)
			// when to stop
			val accuracy = 0.5F
			// testing in Paint
			val paint = Paint()

			while ((roofTextSize - floorTextSize) > accuracy) {

				// test center
				val size = (roofTextSize + floorTextSize ) / 2
				paint.textSize = size

				// check if to big or to small
				if(paint.measureText(titleString) >= maxWidthPx) roofTextSize = size
				else floorTextSize = size
			}

			// cache result
			settings.titleSize = floorTextSize

			return floorTextSize
		}

		/**
		 * Helper to get a [PendingIntent] for a WidgetButton targeting this-[AppWidgetBroadcastReceiver].
		 *
		 * @param context - A Context
		 * @param button - The ButtonId which should fire the PendingIntent ( == [BUTTON_PREV] or [BUTTON_NEXT])
		 * @param appWidgetId - The AppWidgetId from which the PendingIntent is fired
		 *
		 * @return A [PendingIntent] aimed at this-[AppWidgetBroadcastReceiver]
		 */
		private fun getPendingSelfIntentForWidgetButton(context: Context, button: Int, appWidgetId: Int): PendingIntent
			= PendingIntent.getBroadcast(context, appWidgetId, // notice: requestCode == unique per widget
				Intent(context, AppWidgetBroadcastReceiver::class.java).apply {
					action = ACTION_WIDGET_BUTTON_CLICKED
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
					putExtra(INTENT_EXTRA_BUTTON_CLICKED, button)
				},
                PendingIntent.FLAG_UPDATE_CURRENT
			)
	}
}
