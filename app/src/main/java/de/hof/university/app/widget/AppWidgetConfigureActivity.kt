/* Copyright © 2018 Jan Gaida licensed under GNU GPLv3 */
@file:Suppress("SpellCheckingInspection")

package de.hof.university.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.RadioGroup
import android.widget.RadioButton
import de.hof.university.app.R
import de.hof.university.app.widget.data.AppWidgetSettingsHolder
import kotlinx.android.synthetic.main.widget_configure_layout.*

/**
 * The activity running immediately after a widget has been initialized (with the initial layout only: [de.hof.university.app.R.layout.widget_initial_layout]),
 * and should be styled by the options the user took here.
 *
 * @see [AppWidgetSettingsHolder] - the object holding the settings for a widget
 * @see [AppWidgetBroadcastReceiver] - the object called by [AppWidgetManager] to update the widget
 *
 * @author Jan Gaida
 * @since Version 4.8(37)
 * @date 18.02.2018
 */
class AppWidgetConfigureActivity : AppCompatActivity() {

	/**
	 * VARS
	 */
	// the appWidgetId this is configuring
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
	// some styling vars which could be only initialized when needed
	private val backgroundHeaderLightStyleRound by lazy { R.drawable.widget_background_header_white_round }
	private val backgroundHeaderDarkStyleRound by lazy { R.drawable.widget_background_header_dark_round }
	private val backgroundHeaderLightStyleSharp by lazy { R.drawable.widget_background_header_white_sharp }
	private val backgroundHeaderDarkStyleSharp by lazy { R.drawable.widget_background_header_dark_sharp }
	private val backgroundBodyLightStyleRound by lazy { R.drawable.widget_background_body_white_round }
	private val backgroundBodyDarkStyleRound by lazy { R.drawable.widget_background_body_dark_round }
	private val backgroundBodyLightStyleSharp by lazy { R.drawable.widget_background_body_white_sharp }
	private val backgroundBodyDarkStyleSharp by lazy { R.drawable.widget_background_body_dark_sharp }
	private val primaryColorLightStyle by lazy { ContextCompat.getColor(applicationContext, R.color.AppWidget_Text_Color_Primary_For_LightStyle) }
	private val primaryColorDarkStyle by lazy { ContextCompat.getColor(applicationContext, R.color.AppWidget_Text_Color_Primary_For_DarkStyle) }
	private val secondaryColorLightStyle by lazy { ContextCompat.getColor(applicationContext, R.color.AppWidget_Text_Color_Secondary_For_LightStyle) }
	private val secondaryColorDarkStyle by lazy { ContextCompat.getColor(applicationContext, R.color.AppWidget_Text_Color_Secondary_For_DarkStyle) }
	// the Views which actually get modified
	private lateinit var previewTitle: TextView
	private lateinit var previewBig: TextView
	private lateinit var previewRoom: TextView
	private lateinit var previewDetails: TextView
	private lateinit var previewTime: TextView
	private lateinit var previewHeader: View
	private lateinit var previewBody: View
	/**
	 * OVERRIDES
	 */
	@Suppress("DEPRECATION")
	public override fun onCreate(bundle: Bundle?) {
		super.onCreate(bundle)

		// cancel when back is pressed during configure
		setResult(Activity.RESULT_CANCELED)

		// get widget-id
		intent.extras?.let { appWidgetId = it.getInt( AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID ) }

		// check if valid intent-request
		if(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish(); return // if not stop
		}

		// actionbar
		supportActionBar?.run {
			setDisplayUseLogoEnabled(true)
			setDisplayShowHomeEnabled(true)
			setDisplayShowTitleEnabled(true)
			setLogo(R.mipmap.ic_launcher)
			title =
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { Html.fromHtml("<font color='#000000'>${getString(R.string.appwidget_configure_title)}</font>", Html.FROM_HTML_OPTION_USE_CSS_COLORS)
				} else { Html.fromHtml("<font color='#000000'>${getString(R.string.appwidget_configure_title)}</font>") }
			show()
		}

        // content
        setContentView(R.layout.widget_configure_layout)

		// init
		previewHeader = findViewById(R.id.widget_configure_preview_header)
		previewTitle = previewHeader.findViewById(R.id.widget_header_section_title)
		previewBody = findViewById(R.id.widget_configure_preview_body)
		previewBig = previewBody.findViewById(R.id.widget_list_item_big_text)
		previewRoom = previewBody.findViewById(R.id.widget_list_item_schedule_room)
		previewDetails = previewBody.findViewById(R.id.widget_list_item_schedule_details)
		previewTime = previewBody.findViewById(R.id.widget_list_item_schedule_time)

		// apply preview entries
		previewTitle.setText(R.string.appwidget_schedule)
		previewBig.setText(R.string.appwidget_configure_sample_day)
		previewRoom.setText(R.string.appwidget_configure_sample_room)
		previewDetails.setText(R.string.appwidget_configure_sample_details)
		previewTime.setText(R.string.appwidget_configure_sample_time)

		// apply defaults if needed
		if(bundle == null) {
			applyLightDesignToPreview(true)
			applyAngularCornersToPreview(true)
		}
	}

	public override fun onResume() {
		super.onResume()

		// style-radio-group
		widget_configure_design_radiogroup.setOnCheckedChangeListener(
			ColorTextChangingOnCheckChangeListener(arrayOf(widget_configure_design_radiobutton_light, widget_configure_design_radiobutton_dark),
				onPostChange = {
					when(it) {
						R.id.widget_configure_design_radiobutton_light -> applyLightDesignToPreview()
						R.id.widget_configure_design_radiobutton_dark -> applyDarkDesignToPreview()
					}
				}
			)
		)

		widget_configure_corners_radiogroup.setOnCheckedChangeListener(
			ColorTextChangingOnCheckChangeListener(arrayOf(widget_configure_corners_radiobutton_angular, widget_configure_corners_radiobutton_rounded),
				onPostChange = {
                    when(it){
                        R.id.widget_configure_corners_radiobutton_angular -> applyAngularCornersToPreview()
                        R.id.widget_configure_corners_radiobutton_rounded -> applyRoundedCornersToPreview()
                    }

				}
			)
		)

		// buttons
		widget_configure_cancel.setOnClickListener{ finish() }
		widget_configure_apply.setOnClickListener {
			if(widget_configure_disclaimer_switch.isChecked) {
				AppWidgetSettingsHolder(
					widget_configure_design_radiobutton_light.isChecked,
					widget_configure_corners_radiobutton_angular.isChecked
				).let {
					AppWidgetBroadcastReceiver.updateNewAppWidget(applicationContext, appWidgetId, it)
				}

				// pack intent
				Intent().run {
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
					setResult(Activity.RESULT_OK, this)
				}
				finish()
			} else {
				Snackbar.make(widget_configure_root, R.string.appwidget_configure_snackbar_disclaimer_false, Snackbar.LENGTH_LONG).apply {
					setActionTextColor(ContextCompat.getColor(context, R.color.AppWidget_Text_Color_Secondary_For_DarkStyle))
                    setAction(R.string.appwidget_configure_snackbar_ok) { dismiss() }
				}.show()
			}
		}
	}

	/**
	 * COMPANION for [onRestoreInstanceState] && [onRestoreInstanceState]
	 */
	private companion object {
		private const val OUTSTATE_KEY_LIGHT_IS_SELECTED = "LIGHT_SELECTED"
		private const val OUTSTATE_KEY_ANGULAR_IS_SELECTED = "ANGULAR_SELECTED"
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putBoolean(OUTSTATE_KEY_LIGHT_IS_SELECTED, widget_configure_design_radiobutton_light.isChecked)
		outState.putBoolean(OUTSTATE_KEY_ANGULAR_IS_SELECTED, widget_configure_corners_radiobutton_angular.isChecked)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		// reapply preview-configuration
		savedInstanceState.getBoolean(OUTSTATE_KEY_LIGHT_IS_SELECTED).let { lightSelected ->
			savedInstanceState.getBoolean(OUTSTATE_KEY_ANGULAR_IS_SELECTED).let { angularSelected ->
				if (lightSelected) { applyLightDesignToPreview(angularSelected) }
				else { applyDarkDesignToPreview(angularSelected) }
				if(angularSelected) { applyAngularCornersToPreview(lightSelected) }
				else { applyRoundedCornersToPreview(lightSelected) }
			}
		}

	}

	/**
	 * HELPER-FUNS to style the preview
	 */
	private fun applyLightDesignToPreview(angularIsChecked: Boolean = widget_configure_corners_radiobutton_angular.isChecked) {
		previewTitle.setTextColor(primaryColorLightStyle)
		previewBig.setTextColor(secondaryColorLightStyle)
		previewRoom.setTextColor(primaryColorLightStyle)
		previewTime.setTextColor(primaryColorLightStyle)
		previewDetails.setTextColor(primaryColorLightStyle)
		if(angularIsChecked) {
			previewHeader.setBackgroundResource(backgroundHeaderLightStyleSharp)
			previewBody.setBackgroundResource(backgroundBodyLightStyleSharp)
		} else {
			previewHeader.setBackgroundResource(backgroundHeaderLightStyleRound)
			previewBody.setBackgroundResource(backgroundBodyLightStyleRound)
		}
	}

	private fun applyDarkDesignToPreview(angularIsChecked: Boolean = widget_configure_corners_radiobutton_angular.isChecked) {
		previewTitle.setTextColor(primaryColorDarkStyle)
		previewBig.setTextColor(secondaryColorDarkStyle)
		previewRoom.setTextColor(primaryColorDarkStyle)
		previewTime.setTextColor(primaryColorDarkStyle)
		previewDetails.setTextColor(primaryColorDarkStyle)
		if(angularIsChecked) {
			previewHeader.setBackgroundResource(backgroundHeaderDarkStyleSharp)
			previewBody.setBackgroundResource(backgroundBodyDarkStyleSharp)
		} else {
			previewHeader.setBackgroundResource(backgroundHeaderDarkStyleRound)
			previewBody.setBackgroundResource(backgroundBodyDarkStyleRound)
		}
	}

	private fun applyRoundedCornersToPreview(lightDesignIsSelected: Boolean = widget_configure_design_radiobutton_light.isChecked)
		= if (lightDesignIsSelected) {
			previewHeader.setBackgroundResource(backgroundHeaderLightStyleRound)
			previewBody.setBackgroundResource(backgroundBodyLightStyleRound)
		} else {
			previewHeader.setBackgroundResource(backgroundHeaderDarkStyleRound)
			previewBody.setBackgroundResource(backgroundBodyDarkStyleRound)
		}


	private fun applyAngularCornersToPreview(lightDesignIsSelected: Boolean = widget_configure_design_radiobutton_light.isChecked)
		= if (lightDesignIsSelected) {
			previewHeader.setBackgroundResource(backgroundHeaderLightStyleSharp)
			previewBody.setBackgroundResource(backgroundBodyLightStyleSharp)
		} else {
			previewHeader.setBackgroundResource(backgroundHeaderDarkStyleSharp)
			previewBody.setBackgroundResource(backgroundBodyDarkStyleSharp)
		}

	/**
	 * BASE-IMPLEMENTATION of the [RadioGroup.OnCheckedChangeListener]
	 */
	private inner class ColorTextChangingOnCheckChangeListener(
        private val buttons: Array<RadioButton>,
        private val onPostChange: (Int) -> (Unit)
	): RadioGroup.OnCheckedChangeListener {

		init { changeTextColorForRadioButtons() }

		//changes the text-color for the radiobuttons
		private fun changeTextColorForRadioButtons() = buttons.forEach {
			if (it.isChecked) it.setTextColor(secondaryColorDarkStyle)
			else it.setTextColor(primaryColorLightStyle)
		}

		//changes the text-color and calls the Lambda after it
		override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
			changeTextColorForRadioButtons()
			return onPostChange(checkedId)
		}
	}
}
