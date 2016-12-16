package de.hof.university.app.model;

import java.io.Serializable;
import java.util.Date;

import de.hof.university.app.Util.Define;

/**
 * Created by danie on 01.12.2016.
 */

public class SaveObject implements Serializable {
	private static final long serialVersionUID = Define.serialVersionUID;
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
