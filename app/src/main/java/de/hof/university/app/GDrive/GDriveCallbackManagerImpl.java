package de.hof.university.app.GDrive;

import android.content.Intent;

/**
 * Created by Basti on 11.06.18.
 */

public class GDriveCallbackManagerImpl implements GDriveCallbackManager{

    private GDriveCallback gDriveCallback;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(gDriveCallback != null){
            gDriveCallback.onSuccess();
        }

    }

    void registerCallback(GDriveCallback callback){
        gDriveCallback = callback;
    }

    void unregisterCallback(){
        gDriveCallback = null;
    }
}
