package de.hof.university.app.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by danie on 01.12.2016.
 */

public class SaveObject implements Serializable {
	Date lastSaved;

	public SaveObject() {
		this.lastSaved = null;
	}

	public Date getLastSaved() {
		return lastSaved;
	}

	public void setLastSaved(Date lastSaved) {
		this.lastSaved = lastSaved;
	}
}
