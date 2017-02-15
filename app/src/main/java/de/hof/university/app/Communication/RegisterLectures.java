package de.hof.university.app.Communication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import java.util.Set;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;

/**
 * Created by jonasbeetz on 22.12.16.
 */

public class RegisterLectures {
    public void registerLectures(Set<String> ids){
        ParamsClass params = new ParamsClass(ids, "https://app.hof-university.de/fcmtest/fcm_register_user.php");

        new MyAcyncTask().execute(params);
    }

    public String makeJSONString(String data[]){
        JSONArray json = new JSONArray();

        for(int i = 0; i<data.length; i++){
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
    public class MyAcyncTask extends AsyncTask<ParamsClass, String, String> {

        @Override
        protected String doInBackground(ParamsClass... params) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.contextOfApplication);
            final String token = sharedPref.getString(MainActivity.contextOfApplication.getString(R.string.FCM_TOKEN),"Token ist leer");

            //String[] lectures = {"1","2", "3"};
            String[] lectures = params[1].ids.toArray(new String[params[1].ids.size()]);

            URL url = null;
            try {
                url = new URL(params[0].url);
            } catch (MalformedURLException e) {
                Log.d("FCMService", "Register Token fehlgeschlagen");
                e.printStackTrace();
            }
            HttpURLConnection client = null;
            try {
                client = (HttpURLConnection) url.openConnection();

                String data = URLEncoder.encode("fcm_token", "UTF-8")
                        + "=" + URLEncoder.encode(token, "UTF-8");

                data += "&" + URLEncoder.encode("vorlesung_id", "UTF-8") + "="
                        + URLEncoder.encode(makeJSONString(lectures), "UTF-8");

                client.setRequestMethod("POST");
                client.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(client.getOutputStream());
                wr.write( data );
                wr.flush();
                wr.close();

                String text = "";
                BufferedReader reader=null;

                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }

                text = sb.toString();

                Log.d("SERVER RESPONSE: ", text);
            }
            catch(MalformedURLException error) {
                Log.d("TAG", "MalformedURLException error");
                //Handles an incorrectly entered URL
            }
            catch(SocketTimeoutException error) {
                Log.d("TAG", "SocketTimeoutException");
                //Handles URL access timeout.
            }
            catch (IOException error) {
                Log.d("TAG", "IOException");
                //Handles input and output errors
            }
            finally {
                if(client != null) // Make sure the connection is not null.
                    client.disconnect();
                Log.d("TAG", "Disconnected");
                System.out.println("token ist: " + token);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    public class ParamsClass {
        public Set<String> ids;
        public String url;

        public ParamsClass(Set<String> ids, String url) {
            this.ids = ids;
            this.url = url;
        }
    }
}

