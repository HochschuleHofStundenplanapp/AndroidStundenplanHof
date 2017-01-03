package de.hof.university.app.Communication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Log;

	/*
	If you wish to do any message handling beyond receiving notifications on apps
	in the background, create a new Service ( File > New > Service > Service )
	that extends FirebaseMessagingService . This service is necessary to receive
	notifications in foregrounded apps, to receive data payload, to send
	upstream messages, and so on.
	*/
public class FcmMessagingService extends FirebaseMessagingService {
	public FcmMessagingService() {
	}

	public final static String TAG = "FcmMessagingService";

	/*
	In this service create an onMessageReceived method to handle incoming messages.
	 */
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// TODO(developer): Handle FCM messages here.
		// If the application is in the foreground handle both data and notification messages here.
		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.

		String title = remoteMessage.getNotification().getTitle();
		String message = remoteMessage.getNotification().getBody();

		showNotification(title, message);

		super.onMessageReceived(remoteMessage);

		Log.d(TAG, "From: " + remoteMessage.getFrom());
		Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
	}

	//Zeigt die Notification
	private void showNotification(String title, String message) {

		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(title);
		notificationBuilder.setContentText(message);
		notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
		notificationBuilder.setAutoCancel(true);
		notificationBuilder.setContentIntent(pendingIntent);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0,notificationBuilder.build());
	}
}
