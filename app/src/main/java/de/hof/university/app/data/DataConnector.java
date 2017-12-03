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

import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.hof.university.app.util.Define;

/**
 * Created by Lukas on 14.06.2016.
 */
public class DataConnector {

    private static final String TAG = "DataConnector";

    final String readStringFromUrl(final String strUrl) {
        InputStream inputStream;

        HttpURLConnection urlConnection;

        final URL url;
        try {
            url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();

            //Für die Schnittstelle der Hochschule wird Authentifizerung benötigt
            final String userPassword = Define.sAuthSoapUserName + ':' + Define.sAuthSoapPassword;
            final String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
            urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

            urlConnection.setConnectTimeout(Define.connectTimeout);
            urlConnection.setReadTimeout(Define.readTimeout);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());

            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            final StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();

        } catch (final MalformedURLException | UnsupportedEncodingException exception) {
            Log.e(TAG, "readStringFromUrl: MalformedURLException | UnsupportedEncodingException: ", exception);
        } catch (final IOException exception) {
            Log.e(TAG, "readStringFromUrl: IOExcepton: ", exception);
        }

        return "";

    }
}
