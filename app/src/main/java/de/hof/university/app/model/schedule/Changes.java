package de.hof.university.app.model.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import de.hof.university.app.model.SaveObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Changes extends SaveObject {
	private ArrayList<Object> changes;

	public Changes() {
		super();
		this.changes = new ArrayList<>();
	}

	public ArrayList<Object> getChanges() {
		return changes;
	}

	public void setChanges(ArrayList<Object> changes) {
		this.changes = changes;
	}

}
