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

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.DriveStatusCodes;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.model.schedule.MySchedule;

public class GoogleDriveController {

    private final Context context;
    private static GoogleDriveController controller;

    public final String TAG = "GoogleDriveController";

    protected static final String ACCOUNT_NAME_KEY = "account_name";
    public boolean restoreActive = false;


    /**
     * Sign-in request code.
     */
    private static final int REQUEST_CODE_SIGN_IN = 0;

    /**
     * Next available request code for child classes.
     */
    protected static final int NEXT_AVAILABLE_REQUEST_CODE = 1;

    /**
     * Google sign-in client.
     */
    protected GoogleSignInClient mGoogleSignInClient;

    /**
     * Google Drive client.
     */
    protected DriveClient mDriveClient;

    /**
     * Google Drive resource client.
     */
    protected DriveResourceClient mDriveResourceClient;

    /**
     * Selected account name to authorize the app for and authenticate the client with.
     */
    protected String mAccountName;

    private Function<Void, Void> onSignInSuccesful;


    private GoogleDriveController(Context context) {
        this.context = context;
    }

    public static GoogleDriveController getInstance(Context context){
        if(null == controller){
            controller = new GoogleDriveController(context);
        }
        return controller;
    }


    public void signInIfNeeded(@Nullable Function<Void, Void> callback){
        this.onSignInSuccesful = callback;
        if (!isSignedIn() || null==getDriveResourceClient()) {
            signIn();
        }else{
            if(null != onSignInSuccesful)
                this.onSignInSuccesful.apply(null);
        }
    }


    /**
     * Builds a Google sign-in client.
     */
    private GoogleSignInClient getGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER )
                        .build();
        return GoogleSignIn.getClient(getActivity(), signInOptions);
    }

    /**
     * Builds the Drive clients after successful sign-in.
     *
     * @param googleSignInAccount The account which was signed in to.
     */
    private void createDriveClients(GoogleSignInAccount googleSignInAccount) {
        //Log.i(TAG, "Update view with sign-in account.");
        // Build a drive client.
        mDriveClient = Drive.getDriveClient(getActivity().getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        mDriveResourceClient =
                Drive.getDriveResourceClient(getActivity().getApplicationContext(), googleSignInAccount);

        if(null != onSignInSuccesful)
            GoogleDriveController.this.onSignInSuccesful.apply(null);

    }

    public void createDriveClients(){
        this.createDriveClients(GoogleSignIn.getLastSignedInAccount(context));
    }

    public DriveResourceClient getDriveResourceClient() {

        return mDriveResourceClient;
    }

    public DriveClient getDriveClient() {
        return mDriveClient;
    }

    private Activity getActivity(){
        if(context instanceof Activity){
            return (Activity)context;
        }
        throw new RuntimeException("Unable to get Activity from Context");
    }


    public boolean isSignedIn() {
        final GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        return mGoogleSignInClient != null
                && (signInAccount != null
                && signInAccount.getGrantedScopes().contains(Drive.SCOPE_FILE));
    }

    /**
     * Attempts silent sign-in. On failure, start a sign-in {@link Intent}.
     */
    private void signIn() {
        Log.i(TAG, "Start sign-in.");

        mGoogleSignInClient = getGoogleSignInClient();
        // Attempt silent sign-in
        mGoogleSignInClient.silentSignIn()
                .addOnSuccessListener(googleSignInAccount -> {

                    createDriveClients(googleSignInAccount);
                    Log.i(TAG, "Silent sign in succesfully");

                }).addOnFailureListener(e -> {
                    // Silent sign-in failed, display account selection prompt
                    Log.i(TAG, "Silent sign in failed");

                    getActivity().startActivityForResult(
                            mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                });
    }


    public void registerCallback(GDriveCallbackManager manager, GDriveCallback callback) {
        if(!(manager instanceof GDriveCallbackManagerImpl)){
            throw new RuntimeException(context.getString(R.string.gdrive_unexpected_callback_manager));
        }

        ((GDriveCallbackManagerImpl)manager).registerCallback(callback);
    }


    public void unregisterCallback(final GDriveCallbackManager manager){
        if(!(manager instanceof GDriveCallbackManagerImpl)){
            throw new RuntimeException(context.getString(R.string.gdrive_unexpected_callback_manager));
        }

        ((GDriveCallbackManagerImpl)manager).unregisterCallback();
    }


    private void saveInDrive(String filename, Object fileToSave, Date timestamp){
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

        Tasks.whenAll(appFolderTask, createContentsTask).continueWith(task -> {
            DriveFolder parent = appFolderTask.getResult();
            DriveContents contents = createContentsTask.getResult();

            OutputStream outputStream = contents.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(fileToSave);
            objectOutputStream.close();
            outputStream.close();

            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setTitle(filename)
                    .setDescription(timestamp.getTime()+"")
                    .build();

            return getDriveResourceClient().createFile(parent, metadataChangeSet, contents);
        }).addOnSuccessListener(driveFileTask -> {
            Log.i(TAG, filename + " saved successfully");
            //Toast.makeText(context, filename + " saved successfully", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            setGDrivePreference(false);
            Toast.makeText(context, filename + " not saved", Toast.LENGTH_LONG).show();
            });
    }

    public Metadata getMetadataNamed(String name, MetadataBuffer metadataBuffer){
        Metadata defaultMetadata = metadataBuffer.get(0);
        for (Metadata metadata: metadataBuffer){
            if(metadata.getTitle().equals(name)){
                defaultMetadata = metadata;
            }
        }

        return defaultMetadata;
    }


    public void saveMySchedule() {
        Log.i(TAG, "save my schedule called");
        final DataManager dataManager = DataManager.getInstance();
        final MySchedule mySchedule = dataManager.getMySchedule(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Date myScheduleDate = new Date();
        //Save current Date of MySchedule in SharedPrefs for later update checks
        this.restoreActive = true;
        prefs.edit().putLong(context.getString(R.string.PREF_KEY_MYSCHEDULE_DATE), myScheduleDate.getTime()).commit();
        this.restoreActive = false;


        for(LectureItem item: mySchedule.getLectures()){
            Log.i(TAG, item.toString());
        }

        this.saveInDrive(context.getString(R.string.myschedule), mySchedule, myScheduleDate);


    }

    public void saveSharedPreferences(){
        Log.i(TAG, "Save shared prefs called");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.saveInDrive(context.getString(R.string.preferences_file), new HashMap<>(prefs.getAll()), new Date());

    }

    public void getAppFolderFileList(final OnMetadataBufferReady completion){
        final Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(SearchableField.TITLE, context.getString(R.string.myschedule)),
                        Filters.eq(SearchableField.TITLE, context.getString(R.string.preferences_file))))
                .build();

        if(getDriveResourceClient() == null){
            setGDrivePreference(false);
            return;
        }
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();

        Tasks.whenAll(appFolderTask).continueWith(task -> {
            final Task<MetadataBuffer> metadataBufferTask = getDriveResourceClient().queryChildren(appFolderTask.getResult(), query);
            return metadataBufferTask;
        }).addOnSuccessListener(metadataBufferTask -> Tasks.whenAll(metadataBufferTask).continueWith(task -> metadataBufferTask.getResult()).addOnFailureListener(e -> Log.i(TAG, "Failed to read MetadataBuffer")).addOnSuccessListener(metadata -> completion.handleMetadataBuffer(metadata)));


    }


    private void updateInDrive(String filename, Object fileToUpdate, Date updatedAt){
        this.signInIfNeeded((Void)->{
            getAppFolderFileList(metadataBuffer -> {
                Metadata scheduleMD = getMetadataNamed(filename, metadataBuffer);
                Task<DriveContents> openFileTask =
                        getDriveResourceClient().openFile(scheduleMD.getDriveId().asDriveFile(), DriveFile.MODE_WRITE_ONLY);

                openFileTask.continueWithTask(task -> {
                    DriveContents contents = task.getResult();

                    OutputStream outputStream = contents.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(fileToUpdate);
                    objectOutputStream.close();
                    outputStream.close();

                    metadataBuffer.release();
                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setTitle(filename)
                            .setDescription(updatedAt.getTime()+"")
                            .build();


                    //We have to either commit or discard Changes Made to to Drive File, we only read from it so we can discard Changes. (There were none)
                    return getDriveResourceClient().commitContents(contents, metadataChangeSet);
                }).addOnSuccessListener(aVoid -> {
                    //Toast.makeText(context, "Updated " + filename, Toast.LENGTH_LONG).show();
                    getDriveClient().requestSync();

                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    //Toast.makeText(context, "Update " + filename + " failed", Toast.LENGTH_LONG).show();
                });
            });
            return null;
        });

    }

    public void updateMyScheduleFromDrive(){
        Log.i(TAG, "Update my schedule called");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Save current Date of MySchedule in SharedPrefs for later update checks
        final Date myScheduleDate = new Date();
        this.restoreActive = true;
        prefs.edit().putLong(context.getString(R.string.PREF_KEY_MYSCHEDULE_DATE), myScheduleDate.getTime()).commit();
        this.restoreActive = false;
        MySchedule lectures = DataManager.getInstance().getMySchedule(context);
        this.updateInDrive(context.getString(R.string.myschedule), lectures, myScheduleDate);
    }

    public void updateSharedPreferences(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.updateInDrive(context.getString(R.string.preferences_file), prefs.getAll(), new Date());
    }

    private <T> void loadFromDrive(String filename, OnGDriveRestore<T> callback){
        getAppFolderFileList(metadataBuffer -> {
            Metadata prefsMD = getMetadataNamed(filename, metadataBuffer);
            Task<DriveContents> openFileTask =
                    getDriveResourceClient().openFile(prefsMD.getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);

            openFileTask.continueWithTask(task -> {

                DriveContents contents = task.getResult();
                InputStream in = contents.getInputStream();
                ObjectInputStream os = new ObjectInputStream(in);
                final T objFromDrive =(T) os.readObject();
                this.restoreActive = true;

                callback.onResult(objFromDrive);

                this.restoreActive = false;
                in.close();
                os.close();
                metadataBuffer.release();

                //We have to either commit or discard Changes Made to to Drive File, we only read from it so we can discard Changes. (There were none)
                return getDriveResourceClient().discardContents(contents);
            }).addOnFailureListener(e -> e.printStackTrace());
        });
    }




    public void loadMyScheduleFromDrive(@Nullable OnGDriveRestore<MySchedule> callback){
        Log.i(TAG, "Load my schedule called");

        this.loadFromDrive(context.getString(R.string.myschedule), (OnGDriveRestore<MySchedule>) mySchedule -> {
            DataManager.getInstance().deleteAllFromMySchedule(context);

            DataManager.getInstance().getMySchedule(context).setIds(mySchedule.getIds());

                for(LectureItem item: mySchedule.getLectures()){
                    DataManager.getInstance().addToMySchedule(context, item);
                }

                if(callback != null) callback.onResult(mySchedule);
        });
    }



    public void loadSharedPreferences(){
        Log.i(TAG, "Load shared Preferences called");

        this.loadFromDrive(context.getString(R.string.preferences_file), (OnGDriveRestore<HashMap<String, ?>>) prefs -> {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            for (String key : prefs.keySet()) {
                    //Log.i(TAG, "key will be set: " + key);
                    Object value = prefs.get(key);
                    if (value instanceof Boolean) {
                        editor.putBoolean(key, (Boolean) value);
                    } else if (value instanceof Double) {
                        editor.putFloat(key, Float.valueOf(value + ""));
                    } else if (value instanceof Integer) {
                        editor.putInt(key, (Integer) value);
                    } else if (value instanceof Long) {
                        editor.putLong(key, (Long) value);
                    } else if (value instanceof String) {
                        editor.putString(key, (String) value);
                    }
                }
                editor.commit();
        });
    }

    public void deleteMyScheduleDriveFile() {
        getAppFolderFileList(metadataBuffer -> {
            for(Metadata metadata: metadataBuffer){
                getDriveResourceClient().delete(metadata.getDriveId().asDriveFile());
                Log.i(TAG, "Drive file deleted" + metadata.getTitle());
            }
            Snackbar.make(getActivity().getCurrentFocus(), R.string.schedule_deleted, Snackbar.LENGTH_SHORT).show();
        });
    }


    public void sync(boolean isChecked){
        this.signInIfNeeded(input -> {
            //Request a Sync for Google Drive because this is a known bug to ensure App Folder content is synced before attempting to restore
            //https://stackoverflow.com/questions/23755346/android-google-drive-app-data-folder-not-listing-all-childrens
            this.getInstance(getActivity()).getDriveClient().requestSync().addOnSuccessListener(aVoid -> {
                //sync(isChecked);
                performSync(isChecked);
            }).addOnFailureListener((e) -> {
                ApiException apiExcepition = (ApiException) e;
                if (DriveStatusCodes.DRIVE_RATE_LIMIT_EXCEEDED == apiExcepition.getStatusCode()) {
                    Log.i(TAG, "Drive Limit exceeded, performing sync");
                    performSync(isChecked);
                }
            });


            return null;
        });
    }

    private void performSync(boolean isChecked){
        if (isChecked) {

            this.getAppFolderFileList(metadataBuffer -> {
                Log.i(TAG, metadataBuffer.getCount() + "Items in Drive");
                if (metadataBuffer.getCount() == 0) {
                    this.saveMySchedule();
                    this.saveSharedPreferences();
                    Snackbar.make(getActivity().getCurrentFocus(), R.string.schedule_will_be_synchronized, Snackbar.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(context).setTitle(context.getString(R.string.gdrive_files_online))
                            .setMessage(context.getString(R.string.gdrive_restore_question))
                            .setCancelable(false)
                            .setPositiveButton(context.getString(R.string.gdrive_use_remote), (dialog, which) -> {
                                this.loadMyScheduleFromDrive(null);
                                this.loadSharedPreferences();
                            })
                            .setNegativeButton(context.getString(R.string.gdrive_use_local), (dialog, which) ->
                            {
                                this.updateMyScheduleFromDrive();
                                this.updateSharedPreferences();
                                Snackbar.make(getActivity().getCurrentFocus(), R.string.cloudOverride, Snackbar.LENGTH_SHORT).show();
                            })
                            .setNeutralButton(context.getString(R.string.cancel), (dialog, which) -> {
                                setGDrivePreference(false);
                            })
                            .show();

                }
                metadataBuffer.release();
            });


        } else {
            new AlertDialog.Builder(context).setTitle(context.getString(R.string.gdrive_delete_drive_files))
                    .setMessage(context.getString(R.string.gdrive_delete_drive_files_question))
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, (dialog, which) ->
                            this.deleteMyScheduleDriveFile()).setNegativeButton(R.string.no, (dialog, which) -> {

            }).setIcon(android.R.drawable.ic_dialog_alert).show();
        }
    }

    private void setGDrivePreference(boolean value){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(context.getString(R.string.gdrive_sync), value).commit();
    }

}



