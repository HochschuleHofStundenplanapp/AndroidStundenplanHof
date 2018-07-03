package de.hof.university.app.GDrive;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.hof.university.app.R;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureItem;

public abstract class AbstractDriveBaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseDriveActivity";

    /**
     * Dictionary key for {@link AbstractDriveBaseActivity#mAccountName}.
     */
    protected static final String ACCOUNT_NAME_KEY = "account_name";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("#########", "on create called");
        if (!isSignedIn()) {
            signIn();
        }
        else// ##################### Save and Load ########################
            loadMyScheduleFromDrive();//saveMySchedule();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACCOUNT_NAME_KEY, mAccountName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAccountName = savedInstanceState.getString(ACCOUNT_NAME_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Log.i(TAG, "Sign-in request code.");
            // Called after user is signed in.
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Signed in successfully.");
                // Create Drive clients now that account has been authorized access.
                createDriveClients(GoogleSignIn.getLastSignedInAccount(this));
            } else {
                Log.w(TAG, String.format("Unable to sign in, result code %d", resultCode));
            }
        }
    }

    public boolean isSignedIn() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
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
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        createDriveClients(googleSignInAccount);
                        //######################## Load and Save ###########################
                        loadMyScheduleFromDrive();
                        //saveMySchedule();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Silent sign-in failed, display account selection prompt
                startActivityForResult(
                        mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            }
        });
    }

    /**
     * Builds a Google sign-in client.
     */
    private GoogleSignInClient getGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER )
                        .build();

        return GoogleSignIn.getClient(this, signInOptions);
    }

    /**
     * Builds the Drive clients after successful sign-in.
     *
     * @param googleSignInAccount The account which was signed in to.
     */
    private void createDriveClients(GoogleSignInAccount googleSignInAccount) {
        Log.i(TAG, "Update view with sign-in account.");
        // Build a drive client.
        mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
        // Build a drive resource client.
        mDriveResourceClient =
                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
    }

    public DriveClient getDriveClient() {
        return mDriveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    protected void saveMySchedule() {
        final DataManager dataManager = DataManager.getInstance();
        final ArrayList<LectureItem> mySchedule = dataManager.getMySchedule(this, getString(R.string.language), false);

        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

        Tasks.whenAll(appFolderTask, createContentsTask).continueWith(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                DriveFolder parent = appFolderTask.getResult();
                DriveContents contents = createContentsTask.getResult();

                OutputStream outputStream = contents.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(mySchedule);

                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle(getString(R.string.myschedule))
                        .build();

                Log.d("#########", "called");

                return getDriveResourceClient().createFile(parent, metadataChangeSet, contents);
            }
        }).addOnSuccessListener(this, new OnSuccessListener<Task<DriveFile>>() {
            @Override
            public void onSuccess(Task<DriveFile> driveFileTask) {

                Toast.makeText(getApplicationContext(), "Drive File saved successfully", Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Drive File NOT saved", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteMyScheduleDriveFile() {
        getAppFolderFileList(new OnMetadataBufferReady() {
            @Override
            public void handleMetadataBuffer(MetadataBuffer metadataBuffer) {
                for(Metadata metadata: metadataBuffer){
                    getDriveResourceClient().delete(metadata.getDriveId().asDriveFile());
                }
            }
        });
    }

    public void loadMyScheduleFromDrive(){
        getAppFolderFileList(new OnMetadataBufferReady() {
            @Override
            public void handleMetadataBuffer(MetadataBuffer metadataBuffer) {
                Task<DriveContents> openFileTask =
                        getDriveResourceClient().openFile(metadataBuffer.get(0).getDriveId().asDriveFile(), DriveFile.MODE_READ_ONLY);

                openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                        DriveContents contents = task.getResult();
                        InputStream in = contents.getInputStream();
                        ObjectInputStream os = new ObjectInputStream(in);
                        final ArrayList<LectureItem> mySchedule = (ArrayList<LectureItem>) os.readObject();

                        for(LectureItem item: mySchedule){
                            DataManager.getInstance().addToMySchedule(AbstractDriveBaseActivity.this, item);
                            Log.i("LectureItem: ", item.toString());
                        }
                        //We have to either commit or discard Changes Made to to Drive File, we only read from it so we can discard Changes. (There were none)
                        return getDriveResourceClient().discardContents(contents);
                    }
                });
            }
        });
    }


    //TODO: Qeury returnt bisher noch nur die Metadaten der MySchedule Files
    private void getAppFolderFileList(final OnMetadataBufferReady completion){
        final Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, getString(R.string.myschedule)))
                .build();
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();

        Tasks.whenAll(appFolderTask).continueWith(new Continuation<Void, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<Void> task) {
                final Task<MetadataBuffer> metadataBufferTask = getDriveResourceClient().queryChildren(appFolderTask.getResult(), query);
                return metadataBufferTask;
            }
        }).addOnSuccessListener(new OnSuccessListener<Task<MetadataBuffer>>() {
            @Override
            public void onSuccess(final Task<MetadataBuffer> metadataBufferTask) {

                Tasks.whenAll(metadataBufferTask).continueWith(new Continuation<Void, MetadataBuffer>() {
                    @Override
                    public MetadataBuffer then(@NonNull Task<Void> task) {
//                        Log.i("Count#####:", metadataBufferTask.getResult().getCount()+"");
//                        for (int i=0;i<metadataBufferTask.getResult().getCount(); i++){
//                            Log.i("########", metadataBufferTask.getResult().get(i).getTitle());
//                        }
//
//                        for(Metadata metadata: metadataBufferTask.getResult()){
//                            getDriveResourceClient().delete(metadata.getDriveId().asDriveFile());
//                        }
                        return metadataBufferTask.getResult();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("####", "Failed to read MetadataBuffer");
                    }
                }).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                    @Override
                    public void onSuccess(MetadataBuffer metadata) {
                        completion.handleMetadataBuffer(metadata);
                    }
                });
            }
        });


    }
}


//interface OnMetadataBufferReady {
//    void handleMetadataBuffer(MetadataBuffer metadataBuffer);
//}































