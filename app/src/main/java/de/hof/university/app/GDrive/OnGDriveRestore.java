package de.hof.university.app.GDrive;

import java.util.List;

import de.hof.university.app.model.schedule.LectureItem;

public interface OnGDriveRestore<T> {
    void onResult(T lectures);
}
