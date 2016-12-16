package de.hof.university.app.model.schedule;

import java.util.HashSet;
import java.util.Set;

import de.hof.university.app.Util.Define;

/**
 * Created by danie on 30.11.2016.
 */

public class MySchedule extends Schedule {
	private static final long serialVersionUID = Define.serialVersionUID;
	Set<String> ids;

	public MySchedule() {
		super();
		this.ids = new HashSet<>();
	}

	public Set<String> getIds() {
		return ids;
	}

	public void setIds(Set<String> ids) {
		this.ids = ids;
	}
}
