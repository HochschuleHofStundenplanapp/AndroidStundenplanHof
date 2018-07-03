package de.hof.university.app.data;

import java.util.HashMap;

/**
 * Created by patrickniepel on 20.01.18.
 */

public interface TaskComplete {
    void onTaskComplete(HashMap<String, CharSequence[]> data);
}
