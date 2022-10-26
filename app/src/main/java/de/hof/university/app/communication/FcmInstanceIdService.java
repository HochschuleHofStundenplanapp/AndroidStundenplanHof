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

/**
 * Created by stepping on 15.12.2016.
 */

// moved to FirebaseMessagingService. This class is deprecated

//
//public class FcmInstanceIdService extends FirebaseInstanceIdService {
//
//    private final static String TAG = "FcmInstanceIdService";
//
//    /*
//    On initial startup of your app, the FCM SDK generates a registration token for
//    the client app instance. If you want to target single devices, or create
//    device groups, you'll need to access this token.
//
//    You can access the token's value by creating a new class which extends FirebaseInstanceIdService.
//    In that class, call getToken within onTokenRefresh , and log the value as shown:
//
//     The onTokenRefresh callback fires whenever a new token is generated,
//     so calling getToken in its context ensures that you are accessing a current,
//     available registration token. FirebaseInstanceID.getToken() returns null
//     if the token has not yet been generated.
//    */
//    @Override
//    public void onTokenRefresh() {
//        // Get updated InstanceID token.
//        String recent_token = FirebaseInstanceId.getInstance().getToken();
//        Log.d("Recent Token: ", recent_token);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//        // Für alten Token deregistrieren
//        new RegisterLectures().deRegisterLectures();
//
//        //SharedPreferences sharedPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        Log.d(TAG, "RECENT TOKEN: " + recent_token);
//        editor.putString( Define.FCM_TOKEN, recent_token);
//        editor.apply();
//
//        //erneut Registrieren falls es einen neuen Token gibt
//        DataManager.getInstance().registerFCMServer(getApplicationContext());
//    }
//
//    // After you have obtained the token, you can send it to your app server.
//    // See the Instance ID API reference for full detail on the API.
//    //  Implement this method to send any registration to your app's servers.
//    //private void sendRegistrationToServer(final String refreshedToken) {
//        // Jonas :-)
//    //}
//}
