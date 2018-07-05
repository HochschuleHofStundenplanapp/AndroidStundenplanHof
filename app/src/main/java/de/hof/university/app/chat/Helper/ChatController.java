package de.hof.university.app.chat.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import de.hof.university.app.chat.ChatCommunicator;

public class ChatController {

    private String splusname;
    private final String PREFERENCES_CHAT = "chatPrefs";
    private final String CHAT_KEY = "alreadyCreated";
    private final String USERNAME_KEY = "username";
    private UserGenerator userGen;
    private Context context;
    private ChatCommunicator chatCommunicator;
    private SharedPreferences prefs;
    private StringTrimmer trimmer;


    public ChatController(Context context, String splusname){
        userGen = new UserGenerator();
        this.splusname = splusname;
        this.context = context;
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
        chatCommunicator.setRoomname(trimmer.trimmTill(splusname,'%'));
        chatCommunicator.execute();

    }

    private boolean checkIfUserExist(){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_CHAT, Context.MODE_PRIVATE);
        Log.d("Achtung",prefs.getString(USERNAME_KEY,"Nichts"));
        return prefs.getBoolean(CHAT_KEY,false);
    }

    private void createUser(){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_CHAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putBoolean(CHAT_KEY, true);
        editor.putString(USERNAME_KEY,userGen.generateUser());
        editor.commit();
    }

    public void sendMessage(String text){
        chatCommunicator.sendMessage(text);
    }

    public void stopChat(){
        chatCommunicator.disconnect();
    }




}
