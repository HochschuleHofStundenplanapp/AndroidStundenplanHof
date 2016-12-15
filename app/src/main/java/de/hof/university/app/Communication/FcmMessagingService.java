package de.hof.university.app.Communication;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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
		Log.d(TAG, "From: " + remoteMessage.getFrom());
		Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
	}

}
