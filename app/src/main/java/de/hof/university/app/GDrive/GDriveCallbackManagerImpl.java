/*
 * Copyright (c) 2018 Hochschule Hof
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.hof.university.app.GDrive;

import android.content.Intent;
import android.util.Log;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Basti on 11.06.18.
 */

public class GDriveCallbackManagerImpl implements GDriveCallbackManager{

    private GDriveCallback gDriveCallback;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("GDriveCallbackManagerIm", "Result ok?: "+(resultCode==RESULT_OK));
        if(gDriveCallback != null){
            if(resultCode==RESULT_OK)
                gDriveCallback.onResult(GDriveCallback.ResultType.success);
            else
                gDriveCallback.onResult(GDriveCallback.ResultType.failure);
        }

    }

    void registerCallback(GDriveCallback callback){
        gDriveCallback = callback;
    }

    void unregisterCallback(){
        gDriveCallback = null;
    }
}
