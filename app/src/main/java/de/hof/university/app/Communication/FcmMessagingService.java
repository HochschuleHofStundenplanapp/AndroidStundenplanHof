/*
 * Copyright (c) 2016 Michael Stepping, Hof University
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
public final class FcmMessagingService extends FirebaseMessagingService {

	public final static String TAG = "FcmMessagingService";


	public FcmMessagingService() {
	}


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
