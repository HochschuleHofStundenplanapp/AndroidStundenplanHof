/*
 * Copyright (c) 2018 Hof University
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

package de.hof.university.app.chat.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import de.hof.university.app.chat.ChatCommunicator;
import de.hof.university.app.util.Define;

public class ChatController {

    private String splusname;
	
	private String subject;
    private final UserGenerator userGen;
    private final ConnectionMannager conManager;
    private Context context;
    private ChatCommunicator chatCommunicator;
    private SharedPreferences prefs;

/*
    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }
*/
    public ChatController(Context context, String splusname, String subject){

        userGen = new UserGenerator();
        conManager = new ConnectionMannager(context);
        this.splusname = splusname;
        this.context = context;
        this.subject = subject;
        chatCommunicator = new ChatCommunicator();
        prefs = context.getSharedPreferences(Define.PREFERENCES_CHAT, Context.MODE_PRIVATE);

    }

    public void login(){
        if (checkIfUserExist()){
            chatCommunicator.setNewUser(false);
        }
        else {
            createUser();
            chatCommunicator.setNewUser(true);
        }
        chatCommunicator.setNickname(prefs.getString(Define.USERNAME_KEY,"DefaultUser"));
        chatCommunicator.setPassword(prefs.getString(Define.PASSWORD_KEY,"DefaultPassword"));

        // cut everything in the splusname after the % sign
		final int index = splusname.indexOf('%');
		final String roomName = splusname.substring(0, index);
        chatCommunicator.setRoomname(roomName);
	
		chatCommunicator.setSubject(subject);
        chatCommunicator.execute();
    }

    private boolean checkIfUserExist(){
        SharedPreferences prefs = context.getSharedPreferences(Define.PREFERENCES_CHAT, Context.MODE_PRIVATE);
        Log.d("Obacht! ",prefs.getString(Define.USERNAME_KEY,"Nichts"));
        return prefs.getBoolean(Define.CHAT_KEY,false);
    }

    private void createUser(){
        SharedPreferences prefs = context.getSharedPreferences(Define.PREFERENCES_CHAT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putBoolean(Define.CHAT_KEY, true);
        editor.putString(Define.USERNAME_KEY,userGen.generateUser());
        editor.putString(Define.PASSWORD_KEY,userGen.generatePassword());
        editor.apply();
    }

    public void sendMessage(String text){
        chatCommunicator.sendMessage(text);
    }

    public void stopChat(){
        chatCommunicator.disconnect();
    }


}
