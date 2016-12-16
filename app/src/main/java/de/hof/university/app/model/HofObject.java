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
	public static String TAG = "SaveObject";
	private String filename = "SaveObect";

	private static final long serialVersionUID = Define.serialVersionUID;
	private Date lastSaved;

	public HofObject() {
		this.lastSaved = null;
	}

	public Date getLastSaved() {
		return lastSaved;
	}

	public void setLastSaved(Date lastSaved) {
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
		} catch ( IOException e ) {
			Log.e( TAG, "Fehler beim Speichern des Objektes", e );
		}

		// TODO Fehlerwert zurückgeben?
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
		} catch ( Exception e ) {
			Log.e( TAG, "Fehler beim Einlesen", e );
		}
		return result;
	}
}
