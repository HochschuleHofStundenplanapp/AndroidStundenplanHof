package de.hof.university.app.Communication;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

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
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;

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

        for (int i = 0; i < data.length; i++) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("vorlesung_id", data[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            json.put(jo);
        }
        System.out.println(json.toString());

        return json.toString();
    }

    public class MyAcyncTask extends AsyncTask<Set<String>, String, String> {

        @Override
        protected String doInBackground(Set<String>... params) {
            Log.d("FCMService", "Beginn doInBackground");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.contextOfApplication);
            final String token = sharedPref.getString(MainActivity.contextOfApplication.getString(R.string.FCM_TOKEN), "Token ist leer");

            // Vorlesungen setzen
            // Test ID's
            //String[] lectures = {"1","2", "3"};
            // Richtige ID's
            String[] lectures = params[0].toArray(new String[params[0].size()]);

            URL url = null;
            try {
                url = new URL(Define.URL_REGISTER_PUSH_NOTIFICATIONS_HOF + "?debug=1"); // TODO Debug ausschalten
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

                // Authentifizierung
                // TODO noch neue Version nutzen
                final String username = "soapuser";
                final String password = "F%98z&12";
                final String userPassword = username + ':' + password;
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
                        sb.append(line + "\n");
                    }

                    text = sb.toString();

                    Log.d(TAG, "SERVER RESPONSE: " + text);
                } else {
                    Log.d(TAG, "Der ResponseCode war: " + client.getResponseCode());
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

