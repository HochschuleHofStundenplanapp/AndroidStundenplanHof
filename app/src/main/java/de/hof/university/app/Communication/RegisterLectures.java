/*
 * Copyright (c) 2017 Jonas Beetz
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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import de.hof.university.app.MainActivity;
import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;

/**
 * Created by jonasbeetz on 22.12.16.
 */

public class RegisterLectures {

    public static final String TAG = "FCMService";

    // für übergebene Vorlesungen registrieren
    public void registerLectures(Set<String> ids) {
        new MyAcyncTask().execute(ids);
    }

    // von Push-Notifications abmelden,
    // oder sich für keine Vorlesung registrieren
    public void deRegisterLectures() {
        new MyAcyncTask().execute(new HashSet<String>());
    }

    public String makeJSONString(String data[]) {
        JSONArray json = new JSONArray();

	    for ( final String aData : data ) {
		    JSONObject jo = new JSONObject();
		    try {
			    jo.put("vorlesung_id", aData);
		    } catch ( JSONException e ) {
			    e.printStackTrace();
		    }
		    json.put(jo);
	    }
        Log.d(TAG, json.toString());

        return json.toString();
    }

    public class MyAcyncTask extends AsyncTask<Set<String>, String, String> {

        @SafeVarargs
        @Override
        protected final String doInBackground(Set<String>... params) {
            Log.d("FCMService", "Beginn doInBackground");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.contextOfApplication);
            final String token = sharedPref.getString(Define.FCM_TOKEN, "Token ist leer");

            // Vorlesungen setzen
            // Test ID's
            //String[] lectures = {"1","2", "3"};
            // Richtige ID's
            String[] lectures = params[0].toArray(new String[params[0].size()]);

            URL url = null;
            try {
                url = new URL(Define.URL_REGISTER_PUSH_NOTIFICATIONS_HOF); // Debug: ( + "?debug=1")
            } catch (MalformedURLException e) {
                Log.d(TAG, "URL ist nicht URL-konform: " + url);
                e.printStackTrace();
            }
            HttpURLConnection client = null;
            try {
                client = (HttpURLConnection) url.openConnection();

                String data = URLEncoder.encode("fcm_token", "UTF-8")
                        + "=" + URLEncoder.encode(token, "UTF-8");

                data += "&" + URLEncoder.encode("vorlesung_id", "UTF-8") + "="
                        + URLEncoder.encode(makeJSONString(lectures), "UTF-8");

                //Für die Schnittstelle der Hochschule wird Authentifizerung benötigt
                final String userPassword = Define.sAuthSoapUserName + ':' + Define.sAuthSoapPassword;
                final String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
                client.setRequestProperty("Authorization", "Basic " + encoding);

                client.setRequestMethod("POST");
                client.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(client.getOutputStream());
                wr.write(data);
                wr.flush();
                wr.close();

                if (client.getResponseCode() == 200) {
                    String text = "";
                    BufferedReader reader = null;

                    reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        // Append server response in string
                        sb.append(line).append("\n");
                    }

                    text = sb.toString();

                    Log.d(TAG, "SERVER RESPONSE: " + text);
                } else {
                    Log.d(TAG, "Der ResponseCode war: " + client.getResponseCode());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        // Append server response in string
                        sb.append(line).append("\n");
                    }

                    Log.d(TAG, "SERVER ERROR RESPONSE: " + sb.toString());
                }
            } catch (MalformedURLException error) {
                Log.d(TAG, "MalformedURLException error");
                //Handles an incorrectly entered URL
            } catch (SocketTimeoutException error) {
                Log.d(TAG, "SocketTimeoutException");
                //Handles URL access timeout.
            } catch (IOException error) {
                Log.d(TAG, "IOException: " + error.toString());
                //Handles input and output errors
            } finally {
                if (client != null) // Make sure the connection is not null.
                    client.disconnect();
                Log.d(TAG, "Disconnected");
                Log.d(TAG, "Der Token ist: " + token);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}

