package de.hof.university.app.GDrive;

import android.content.Intent;

/**
 * Created by Basti on 11.06.18.
 */

public interface GDriveCallbackManager {

     void onActivityResult(int requestCode, int resultCode, Intent data);
     class Factory{
        public static GDriveCallbackManager create(){
            return new GDriveCallbackManagerImpl();
        }
    }
}
