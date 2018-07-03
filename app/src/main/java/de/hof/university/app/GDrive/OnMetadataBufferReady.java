package de.hof.university.app.GDrive;

import com.google.android.gms.drive.MetadataBuffer;

public interface OnMetadataBufferReady {
    void handleMetadataBuffer(MetadataBuffer metadataBuffer);
}
