/*
 * Copyright (c) 2016 Michael Stepping, Hof University, Jonas Beetz
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

package de.hof.university.app.communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.util.Define;

/*
If you wish to do any message handling beyond receiving notifications on apps
in the background, create a new Service ( File > New > Service > Service )
that extends FirebaseMessagingService . This service is necessary to receive
notifications in foregrounded apps, to receive data payload, to send
upstream messages, and so on.
*/

public final class FcmMessagingService extends FirebaseMessagingService {
	private final static String TAG = "FcmMessagingService";

	public FcmMessagingService() {
		super();
	}

	/*
	In this service create an onMessageReceived method to handle incoming messages.
	 */
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// If the application is in the foreground handle both data and notification messages here.
		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.

		final String title = remoteMessage.getNotification().getTitle();
		final String message = remoteMessage.getNotification().getBody();

		showNotification(title, message);

		super.onMessageReceived(remoteMessage);

		Log.d(TAG, "From: " + remoteMessage.getFrom());
		Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
	}

	//Zeigt die Notification, falls der Nutzer gerade in der App ist wenn die Notification eintrifft
	private void showNotification(String title, String message) {
		Intent intent = new Intent(this, MainActivity.class);

		// Activity wird über die aktuelle Activity gelegt, damit der zurück weg normal ist.
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		// damit später geprüft werden kann welcher intent gestartet wurde
		intent.putExtra(Define.NOTIFICATION_TYPE, Define.NOTIFICATION_TYPE_CHANGE);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this, Define.NOTIFICATION_CHANNEL_SCHEDULE_CHANGES_ID);
		notificationBuilder.setContentTitle(title);
		notificationBuilder.setContentText(message);
		notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
		notificationBuilder.setAutoCancel(true);
		notificationBuilder.setContentIntent(pendingIntent);
		// damit bei der Benachrichtigung den Standard Sound wiedergegeben wird
		notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		org.junit.Assert.assertTrue(notificationManager != null);
		notificationManager.notify(0, notificationBuilder.build());
	}


	/*
	On initial startup of your app, the FCM SDK generates a registration token for
	the client app instance. If you want to target single devices, or create
	device groups, you'll need to access this token.

	You can access the token's value by creating a new class which extends FirebaseInstanceIdService.
	In that class, call getToken within onTokenRefresh , and log the value as shown:

	 The onTokenRefresh callback fires whenever a new token is generated,
	 so calling getToken in its context ensures that you are accessing a current,
	 available registration token. FirebaseInstanceID.getToken() returns null
	 if the token has not yet been generated.
	*/
	@Override
	public void onNewToken(String sNewToken) {
		super.onNewToken(sNewToken);

		// Get updated InstanceID token.
		Log.d("New Token: ", sNewToken);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// Für alten Token deregistrieren
		new RegisterLectures().deRegisterLectures();

		//SharedPreferences sharedPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Log.d(TAG, "New TOKEN: " + sNewToken);
		editor.putString( Define.FCM_TOKEN, sNewToken);
		editor.apply();

		//erneut Registrieren falls es einen neuen Token gibt
		DataManager.getInstance().registerFCMServer(getApplicationContext());
	}

	// After you have obtained the token, you can send it to your app server.
	// See the Instance ID API reference for full detail on the API.
	//  Implement this method to send any registration to your app's servers.
	//private void sendRegistrationToServer(final String refreshedToken) {
	// Jonas :-)
	//}
}
