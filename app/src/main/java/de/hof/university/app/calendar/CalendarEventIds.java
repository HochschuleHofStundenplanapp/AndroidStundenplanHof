package de.hof.university.app.calendar;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.HofObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarEventIds extends HofObject {
    private static final long serialVersionUID = Define.serialVersionUIDv1;

    private HashMap<String, ArrayList<Long>> lecturesEventIDs;
    private HashMap<String, ArrayList<Long>> changesEventIDs;

    public CalendarEventIds() {
        lecturesEventIDs = new HashMap<>();
        changesEventIDs = new HashMap<>();
    }

    public HashMap<String, ArrayList<Long>> getLecturesEventIDs() {
        return lecturesEventIDs;
    }

    public HashMap<String, ArrayList<Long>> getChangesEventIDs() {
        return changesEventIDs;
    }

    public void setLecturesEventIDs(HashMap<String, ArrayList<Long>> lecturesEventIDs) {
        this.lecturesEventIDs = lecturesEventIDs;
    }

    public void setChangesEventIDs(HashMap<String, ArrayList<Long>> changesEventIDs) {
        this.changesEventIDs = changesEventIDs;
    }
}
