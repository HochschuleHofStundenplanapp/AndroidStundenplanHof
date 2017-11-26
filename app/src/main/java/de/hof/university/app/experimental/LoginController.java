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

package de.hof.university.app.experimental;

/*
 * Created by Lukas on 05.07.2016.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import de.hof.university.app.R;


final public class LoginController {

	public final static String TAG = "LoginController";

	//singleton
	private static LoginController controller = null;

	// --Commented out by Inspection (17.07.2016 20:11):private final Context context;
	private final SharedPreferences sharedPref;

	private final AlertDialog loginAlertDialog;
	private final AlertDialog passwordAlertDialog;

	private String password;

	public static LoginController getInstance(Context context) {
		if ( null == controller ) {
			controller = new LoginController(context);
		}
		return controller;
	}

	private LoginController(final Context context) {
		super();

		password = "";
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				throw new RuntimeException();
			}
		};
		// LOGIN DIALOG
		View loginDialogView = View.inflate(context, R.layout.dialog_login, null);
		final EditText editTextUsernameLogin = (EditText) loginDialogView.findViewById(R.id.username);
		final EditText editTextPasswordLogin = (EditText) loginDialogView.findViewById(R.id.password);
		editTextUsernameLogin.setText(getUsername());


		final CheckBox checkBoxShowPasswordLogin = (CheckBox) loginDialogView.findViewById(R.id.show_password);
		checkBoxShowPasswordLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					editTextPasswordLogin.setTransformationMethod(null);
				} else {
					editTextPasswordLogin.setTransformationMethod(new PasswordTransformationMethod());
				}
			}
		});
		final CheckBox checkBoxSavePasswordLogin = (CheckBox) loginDialogView.findViewById(R.id.save_password);
		checkBoxSavePasswordLogin.setChecked(sharedPref.getBoolean("save_password", true));

		AlertDialog.Builder builderLogin = new AlertDialog.Builder(context);
		loginAlertDialog = builderLogin.setTitle(context.getString(R.string.LOGIN_DIALOG)).setView(loginDialogView)
				.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sharedPref.edit().putBoolean("save_password", checkBoxSavePasswordLogin.isChecked()).apply();
						sharedPref.edit().putString("username", editTextUsernameLogin.getText().toString()).apply();

						if (checkBoxSavePasswordLogin.isChecked()) {
							sharedPref.edit().putString("password", editTextPasswordLogin.getText().toString()).apply();
						} else {
							password = editTextPasswordLogin.getText().toString();
						}
						editTextPasswordLogin.setText("");
						checkBoxShowPasswordLogin.setChecked(false);
						handler.sendMessage(handler.obtainMessage());
					}
				})
				.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						editTextPasswordLogin.setText("");
						checkBoxShowPasswordLogin.setChecked(false);
						handler.sendMessage(handler.obtainMessage());
						dialog.dismiss();
					}
				}).setCancelable(false)
				.create();

		// PASSWORD DIALOG
		View passwordDialogView = View.inflate(context, R.layout.dialog_password, null);
		final EditText editTextPasswordPassword = (EditText) passwordDialogView.findViewById(R.id.password);
		final CheckBox checkBoxShowPasswordPassword = (CheckBox) passwordDialogView.findViewById(R.id.show_password);
		checkBoxShowPasswordPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					editTextPasswordPassword.setTransformationMethod(null);
				} else {
					editTextPasswordPassword.setTransformationMethod(new PasswordTransformationMethod());
				}
			}
		});

		AlertDialog.Builder builderPassword = new AlertDialog.Builder(context);
		passwordAlertDialog = builderPassword.setTitle(context.getString(R.string.LOGIN_DIALOG)).setView(passwordDialogView)
				.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						password = editTextPasswordPassword.getText().toString();
						editTextPasswordPassword.setText("");
						checkBoxShowPasswordPassword.setChecked(false);
						handler.sendMessage(handler.obtainMessage());
					}
				})
				.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						editTextPasswordPassword.setText("");
						checkBoxShowPasswordPassword.setChecked(false);
						handler.sendMessage(handler.obtainMessage());
						dialog.dismiss();
					}
				}).setCancelable(false).create();
	}

	public final boolean showDialog() {
		if ( getUsername().isEmpty() ) {
			loginAlertDialog.show();
			try {
				Looper.loop();
			} catch ( RuntimeException ignored ) {
			}
		} else if ( getPassword().isEmpty() ) {
			passwordAlertDialog.show();
			try {
				Looper.loop();
			} catch ( RuntimeException ignored ) {
			}
		}
		return (!passwordIsEmpty() && !getUsername().isEmpty());
	}

	private boolean passwordIsEmpty() {
		boolean result;
		if ( sharedPref.getBoolean("save_password", false) ) {
			result = sharedPref.getString("password", "").isEmpty();
		} else {
			result = password.isEmpty();
		}
		return result;
	}

	public final void showLoginDialog() {
		try {
			loginAlertDialog.show();
		} catch (WindowManager.BadTokenException e) {
			Log.e(TAG, "loginAlertDialog.show: BadTokenException einmal abgefangen. Nochmal versuchen", e);
			try {
				loginAlertDialog.show();
			} catch (WindowManager.BadTokenException e2) {
				Log.e(TAG, "loginAlertDialog.show: BadTokenException zweimal abgefangen. Aufhören");
			}
		}

		try {
			Looper.loop();
		} catch ( RuntimeException ignored ) {
		}
	}

	public final String getUsername() {
		return sharedPref.getString("username", "");
	}

	public final String getPassword() {
		String result;
		if ( sharedPref.getBoolean("save_password", false) ) {
			result = sharedPref.getString("password", "");
		} else {
			result = password;
			password = "";
		}
		return result;
	}
}
