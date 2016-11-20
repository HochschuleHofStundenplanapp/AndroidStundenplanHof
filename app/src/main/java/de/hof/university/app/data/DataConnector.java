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

package de.hof.university.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by Lukas on 14.06.2016.
 */
public class DataConnector {

    public static final String TIME_APPEND = "_url_cache_time";

    private Context context;

    public final String getStringFromUrl(Context context, final String strUrl, final int cacheTime){
        this.context = context;
        if (cacheStillValid(strUrl+TIME_APPEND, cacheTime)){
            return loadFromSharedPreferences(strUrl);
        } else {
            final String result = readStringFromUrl(strUrl);
            if (result == null) {
                if (cacheTime != -1) {
                    return loadFromSharedPreferences(strUrl);
                } else {
                    return "";
                }
            } else if (!result.isEmpty()) {
                saveToSharedPreferences(strUrl, result);
            }
            return result;
        }
    }

    private void saveToSharedPreferences(String strUrl, String result) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .putLong(strUrl + TIME_APPEND, new Date().getTime())
                .putString(strUrl, result)
                .apply();
    }

    private String loadFromSharedPreferences(String strUrl) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = sharedPreferences.getString(strUrl, "");
        return result;
    }

    public final void cleanCache(final Context context, final int maxAge){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for(Map.Entry<String, ?> entry : allEntries.entrySet()){
            final String key = entry.getKey();
            if(key.endsWith(TIME_APPEND)){
                if(!cacheStillValid(key, maxAge)){
                    //Delete the old Keys
                    sharedPreferences.edit().remove(key).remove(key.substring(0, key.length()-TIME_APPEND.length())).apply();
                }
            }
        }
    }

    private boolean cacheStillValid(final String urlKey, final int cacheTime){
        final Date today = new Date();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        Date lastCached = new Date(sharedPreferences.getLong(urlKey, 0L));

        Calendar cal = Calendar.getInstance();
        cal.setTime(lastCached);
        cal.add(Calendar.MINUTE, cacheTime);
        lastCached = cal.getTime();

        return lastCached.after(today);
    }


    static final int timeoutInSeconds = 1000;

    private static String readStringFromUrl(final String strUrl){
        InputStream inputStream;

        HttpURLConnection urlConnection;

        URL url;
        try {
            url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();

            //Für die Schnittstelle der Hochschule wird Authentifizerung benötigt
            if(strUrl.contains("www.hof-university.de/soap/client.php")) {
                // user
                String username = "p"; //soapuser
                username = 'a' +username;
                username += "u";
                username = 'o' +username;
                username += "s";
                username = 's' +username;
                username += "er";

                //password                  F%98z&12
                String password = "8";
                password += "z";
                password = '9' +password;
                password += "&";
                password = '%' +password;
                password += "1";
                password = 'F' +password;
                password += "2";
                final String userPassword = username + ':' + password;
                final String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
                urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
            } else if (strUrl.contains("http://sh-web02.hof-university.de/soap/client.php")) {
                // Testserver
                String username = "test";
                String password = "test";

                final String userPassword = username + ':' + password;
                final String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
                urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
            }

            urlConnection.setConnectTimeout(timeoutInSeconds);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (final MalformedURLException | UnsupportedEncodingException ignored ) {

        } catch (final IOException ignored ) {
            //System.out.println(ignored);
        }

        return null;
    }
}
