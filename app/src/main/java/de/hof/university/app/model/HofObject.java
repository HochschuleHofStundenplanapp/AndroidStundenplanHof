/*
 * Copyright (c) 2017 Daniel Glaser
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

package de.hof.university.app.model;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;

/**
 * Created by Daniel on 01.12.2016.
 */

public class HofObject implements Serializable {
	public final static String TAG = "SaveObject";

	private final static String filename = "SaveObject";
	private static final long serialVersionUID = Define.serialVersionUID;

	private Date lastSaved;

	public HofObject() {
		this.lastSaved = null;
	}

	public Date getLastSaved() {
		return lastSaved;
	}

	public void setLastSaved(final Date lastSaved) {
		this.lastSaved = lastSaved;
	}

	// this is the general method to serialize an object
	//
	private void saveObject(final Context context) {
		try {
			final File file = new File(context.getFilesDir(), this.filename);
			final FileOutputStream fos = new FileOutputStream(file);
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(this);
			os.close();
			fos.close();
		} catch ( final IOException e ) {
			Log.e( TAG, "Fehler beim Speichern des Objektes", e );
		}
	}

	// this is the general method to serialize an object
	private Object readObject(final Context context) {
		Object result = null;
		try {
			final File file = new File(context.getFilesDir(), this.filename);
			if ( file.exists() ) {
				final FileInputStream fis = new FileInputStream(file);
				final ObjectInputStream is = new ObjectInputStream(fis);
				result = is.readObject();
				is.close();
				fis.close();
			}
		} catch ( final Exception e ) {
			Log.e( TAG, "Fehler beim Einlesen", e );
		}
		return result;
	}
}
