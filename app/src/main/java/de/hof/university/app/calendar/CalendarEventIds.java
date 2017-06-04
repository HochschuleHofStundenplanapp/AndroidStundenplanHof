package de.hof.university.app.calendar;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.HofObject;

import java.io.InterruptedIOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarEventIds extends HofObject implements Serializable {
    private static final long serialVersionUID = Define.serialVersionUIDv1;

    private HashMap<String, String> lectureEventIDs;
    private HashMap<String, String> changesEventIDs;

    public CalendarEventIds() {
        lectureEventIDs = new HashMap<>();
        changesEventIDs = new HashMap<>();
    }

    public HashMap<String, String> getLectureEventIDs() {
        return lectureEventIDs;
    }

    public HashMap<String, String> getChangesEventIDs() {
        return changesEventIDs;
    }

    public void setLectureEventIDs(HashMap<String, String> lectureEventIDs) {
        this.lectureEventIDs = lectureEventIDs;
    }

    public void setChangesEventIDs(HashMap<String, String> changesEventIDs) {
        this.changesEventIDs = changesEventIDs;
    }
}
