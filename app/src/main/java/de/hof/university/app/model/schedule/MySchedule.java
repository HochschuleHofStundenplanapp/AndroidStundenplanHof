package de.hof.university.app.model.schedule;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by danie on 30.11.2016.
 */

public class MySchedule extends Schedule {
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
