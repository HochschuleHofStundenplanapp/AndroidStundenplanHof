package de.hof.university.app.chat.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


import de.hof.university.app.chat.ChatCommunicator;
import de.hof.university.app.chat.MessageSingleton;

public class ChatController {

    private String splusname;
    private final String PREFERENCES_CHAT = "chatPrefs";
    private final String CHAT_KEY = "alreadyCreated";
    private final String USERNAME_KEY = "username";
    private final String PASSWORD_KEY = "password";
    private String subject;
    private final UserGenerator userGen;
    private final ConnectionMannager conManager;
    private Context context;
    private ChatCommunicator chatCommunicator;
    private SharedPreferences prefs;
    private StringTrimmer trimmer;


    public ChatController(Context context, String splusname, String subject){
        userGen = new UserGenerator();
        conManager = new ConnectionMannager(context);
        this.splusname = splusname;
        this.context = context;
        this.subject = subject;
        trimmer = new StringTrimmer();
        chatCommunicator = new ChatCommunicator();
        prefs = context.getSharedPreferences(PREFERENCES_CHAT, Context.MODE_PRIVATE);

    }

    public void login(){
        if (checkIfUserExist()){
            chatCommunicator.setNewUser(false);
        }
        else {
            createUser();
            chatCommunicator.setNewUser(true);
        }
        chatCommunicator.setNickname(prefs.getString(USERNAME_KEY,"DefaultUser"));
        chatCommunicator.setPassword(prefs.getString(PASSWORD_KEY,"DefaultPassword"));
        chatCommunicator.setRoomname(trimmer.trimmTill(splusname,'%'));
        chatCommunicator.setSubject(subject);
        chatCommunicator.execute();
    }

    private boolean checkIfUserExist(){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_CHAT, Context.MODE_PRIVATE);
        Log.d("Obacht! ",prefs.getString(USERNAME_KEY,"Nichts"));
        return prefs.getBoolean(CHAT_KEY,false);
    }

    private void createUser(){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_CHAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putBoolean(CHAT_KEY, true);
        editor.putString(USERNAME_KEY,userGen.generateUser());
        editor.putString(PASSWORD_KEY,userGen.generatePassword());
        editor.commit();
    }

    public void sendMessage(String text){
        chatCommunicator.sendMessage(text);
    }

    public void stopChat(){
        chatCommunicator.disconnect();
    }


}
