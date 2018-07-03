package de.hof.university.app.GDrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureItem;

public class AbstractDriveFragment extends PreferenceFragment {


    public static AbstractDriveFragment newInstance() {
        return new AbstractDriveFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if(isStateSaved())
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_KEY);

        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        Log.d("#########", "on create called");
        if (!isSignedIn()) {
            signIn();
        }
        else// ##################### Save and Load ########################
           //loadMyScheduleFromDrive();
            saveMySchedule();
        // TODO: Use the ViewModel
    }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Log.i(TAG, "Sign-in request code.");
            // Called after user is signed in.

            if (resultCode == getActivity().RESULT_OK) {
                Log.i(TAG, "Signed in successfully.");
                // Create Drive clients now that account has been authorized access.

                createDriveClients(GoogleSignIn.getLastSignedInAccount(getContext()));
            } else {
                Log.w(TAG, String.format("Unable to sign in, result code %d", resultCode));
            }
        }
    }





    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACCOUNT_NAME_KEY, mAccountName);
    }


    public boolean isSignedIn() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
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
                        //loadMyScheduleFromDrive();
                        saveMySchedule();
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
        return GoogleSignIn.getClient(getContext(), signInOptions);
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
    }

    public DriveClient getDriveClient() {
        return mDriveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }


    protected void saveMySchedule() {
        Log.i(TAG, "save my schedule called");
        final DataManager dataManager = DataManager.getInstance();
        final ArrayList<LectureItem> mySchedule = dataManager.getMySchedule(getContext(), getString(R.string.language), false);

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
        }).addOnSuccessListener(new OnSuccessListener<Task<DriveFile>>() {
            @Override
            public void onSuccess(Task<DriveFile> driveFileTask) {

                Toast.makeText(getActivity().getApplicationContext(), "Drive File saved successfully", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "Drive File NOT saved", Toast.LENGTH_LONG).show();
            }
        });


    }
}

