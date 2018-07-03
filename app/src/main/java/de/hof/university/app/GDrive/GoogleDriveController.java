package de.hof.university.app.GDrive;

import android.app.Activity;
import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
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

    public static GoogleDriveController getInstance(Activity activity){
        if(null == controller){
            controller = new GoogleDriveController(activity);
        }
        return controller;
    }


    public void signInIfNeeded(@Nullable Function<Void, Void> callback){
        this.onSignInSuccesful = callback;
        if (!isSignedIn()) {
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
        Log.i(TAG, "Update view with sign-in account.");
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
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
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
            throw new RuntimeException("Unexpected CallbackManager, please use the provided CallbackManager Factory");
        }

        ((GDriveCallbackManagerImpl)manager).registerCallback(callback);
    }


    public void unregisterCallback(final GDriveCallbackManager manager){
        if(!(manager instanceof GDriveCallbackManagerImpl)){
            throw new RuntimeException("Unexpected CallbackManager, please use the provided CallbackManager Factory");
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

            Log.d("#########", "called");

            return getDriveResourceClient().createFile(parent, metadataChangeSet, contents);
        }).addOnSuccessListener(driveFileTask -> {
            Toast.makeText(context, filename + " saved successfully", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> Toast.makeText(context, filename + " NOT saved", Toast.LENGTH_LONG).show());
    }

    public Metadata getMetadataNamed(String name, MetadataBuffer metadataBuffer){
        Metadata defaultMetadata = metadataBuffer.get(0);
        Log.i(TAG, "Search " + name);
        for (Metadata metadata: metadataBuffer){
            Log.i(TAG, "Current File: " + metadata.getTitle());
            if(metadata.getTitle().equals(name)){
                defaultMetadata = metadata;
            }
        }
        Log.i(TAG, "Found " + defaultMetadata.getTitle());

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

        this.saveInDrive(context.getString(R.string.myschedule), mySchedule.getLectures(), myScheduleDate);


    }

    public void saveSharedPreferences(){
        Log.i(TAG, "Save shared prefs called");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        this.saveInDrive("Preferences", new HashMap<>(prefs.getAll()), new Date());

    }

    public void getAppFolderFileList(final OnMetadataBufferReady completion){
        final Query query = new Query.Builder()
                .addFilter(Filters.or(Filters.eq(SearchableField.TITLE, context.getString(R.string.myschedule)),
                        Filters.eq(SearchableField.TITLE, "Preferences")))
                .build();

        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();

        Tasks.whenAll(appFolderTask).continueWith(task -> {
            final Task<MetadataBuffer> metadataBufferTask = getDriveResourceClient().queryChildren(appFolderTask.getResult(), query);
            return metadataBufferTask;
        }).addOnSuccessListener(metadataBufferTask -> Tasks.whenAll(metadataBufferTask).continueWith(task -> metadataBufferTask.getResult()).addOnFailureListener(e -> Log.i("####", "Failed to read MetadataBuffer")).addOnSuccessListener(metadata -> completion.handleMetadataBuffer(metadata)));


    }


    private void updateInDrive(String filename, Object fileToUpdate, Date updatedAt){
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
                Log.i(TAG, updatedAt.getTime() + " in lAST vIEWED BY ME");
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle(filename)
                        .setDescription(updatedAt.getTime()+"")
                        .build();


                //We have to either commit or discard Changes Made to to Drive File, we only read from it so we can discard Changes. (There were none)
                return getDriveResourceClient().commitContents(contents, metadataChangeSet);
            }).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Updated " + filename, Toast.LENGTH_LONG).show();
                getDriveClient().requestSync();

            }).addOnFailureListener(e -> {
                e.printStackTrace();
                Toast.makeText(context, "Update " + filename + " failed", Toast.LENGTH_LONG).show();
            });
        });
    }

    public void updateMyScheduleFromDrive(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Save current Date of MySchedule in SharedPrefs for later update checks
        final Date myScheduleDate = new Date();
        this.restoreActive = true;
        prefs.edit().putLong(context.getString(R.string.PREF_KEY_MYSCHEDULE_DATE), myScheduleDate.getTime()).commit();
        Log.i(TAG, myScheduleDate.getTime() + " in Shared Prefs");
        this.restoreActive = false;

        Log.i(TAG, "Update my schedule called");
        ArrayList<LectureItem> lectures = DataManager.getInstance().getMySchedule(context).getLectures();
        this.updateInDrive(context.getString(R.string.myschedule), lectures, myScheduleDate);
    }

    public void updateSharedPreferences(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.updateInDrive("Preferences", prefs.getAll(), new Date());
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

                //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
                Log.i(TAG, "Shared Preferences restore is active");
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




    public void loadMyScheduleFromDrive(@Nullable OnGDriveRestore<List<LectureItem>> callback){
        Log.i(TAG, "Load my schedule called");

        this.loadFromDrive(context.getString(R.string.myschedule), (OnGDriveRestore<ArrayList<LectureItem>>) mySchedule -> {
            DataManager.getInstance().deleteAllFromMySchedule(context);
                for(LectureItem item: mySchedule){

                    DataManager.getInstance().addToMySchedule(context, item);


                    Log.i("LectureItem: ", item.toString());
                }

                if(callback != null) callback.onResult(mySchedule);
        });
    }



    public void loadSharedPreferences(){
        Log.i(TAG, "load shared Preferences called");

        this.loadFromDrive("Preferences", (OnGDriveRestore<HashMap<String, ?>>) prefs -> {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            for (String key : prefs.keySet()) {
                    Log.i(TAG, "key will be set: " + key);
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
                Toast.makeText(MainActivity.getAppContext(), "Deleted Drive File", Toast.LENGTH_LONG).show();
            }
        });
    }


}



