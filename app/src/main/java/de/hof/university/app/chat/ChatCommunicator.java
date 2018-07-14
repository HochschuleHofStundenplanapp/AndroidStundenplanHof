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

package de.hof.university.app.chat;

import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.bosh.BOSHConfiguration;
import org.jivesoftware.smack.bosh.XMPPBOSHConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import de.hof.university.app.util.Define;

public class ChatCommunicator extends AsyncTask<String, String, String> implements MessageListener {
    
    private XMPPBOSHConnection conn1;
    private String nickname = ""; //"testuser2";
    private String password = ""; //"TestUser2";
    private Boolean newUser = false;
    private MultiUserChat multiChat;

    private MultiUserChatManager multiChatManager;
    private String roomname;
    private EntityBareJid jid;
    private String subject;



    final public String getPassword() { return password; }

    final public void setPassword(String password) {
        this.password = password;
    }
    
    final public String getSubject() { return subject; }
    
    final public void setSubject(String subject) {
        this.subject = subject;
    }
    
    final public String getNickname() { return nickname; }
    
    final public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    final public Boolean getNewUser() { return newUser; }
    
    final public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }
    
    final public String getRoomname() { return roomname; }
    
    final public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    @Override
    protected String doInBackground(final String... strings) {

        BOSHConfiguration config;
        if(newUser){
            try {
                config = BOSHConfiguration.builder()
                        .setUseHttps(true)
                        .setXmppDomain(Define.SL_APP01_HOF_UNIVERSITY_DE)
                        .setFile(Define.CHAT_HTTP_BIND)
                        .setHost(Define.CHAT_SERVER_APP_HOF_UNIVERSITY_DE)
                        .setPort(Define.CHAT_SERVER_BOSH_PORT)
                        .build();
                conn1 = new XMPPBOSHConnection(config);
                conn1.connect();
                createUser(this.nickname,this.password);
                newUser = false;
            } catch (XmppStringprepException e) {
                e.printStackTrace();
                return "";
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
        try {
            config = BOSHConfiguration.builder()
                        .setUseHttps(true)
                        .setUsernameAndPassword(this.nickname, this.password)
                        .setXmppDomain(Define.SL_APP01_HOF_UNIVERSITY_DE)
                        .setFile(Define.CHAT_HTTP_BIND)
                        .setHost(Define.CHAT_SERVER_APP_HOF_UNIVERSITY_DE)
                        .setPort(Define.CHAT_SERVER_BOSH_PORT)
                        .build();
        }
        catch (XmppStringprepException e) {
                e.printStackTrace();
                return "";
        }


        conn1 = new XMPPBOSHConnection(config);

        try {
            conn1.connect();
            conn1.login();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        multiChatManager = MultiUserChatManager.getInstanceFor(conn1);
        try {
            jid = JidCreate.entityBareFrom(roomname + "@" + "chat.sl-app01.hof-university.de");


            multiChat = multiChatManager.getMultiUserChat(jid);
            try {
                joinOrCreate(jid);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (MultiUserChatException.MucAlreadyJoinedException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (MultiUserChatException.NotAMucServiceException e) {
                e.printStackTrace();
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void processMessage(Message message) {
  
        final int index = message.getFrom().toString().indexOf('/');
        final String sender = message.getFrom().toString().substring(index + 1);
        Log.d("Achtung!",message.getBody());
        MessageSingleton.getInstance().appendMessage(new ChatMessage(sender,message.getBody()));
    }

    public void createUser(String username, String password){
        AccountManager accMan = AccountManager.getInstance(conn1);
        accMan.sensitiveOperationOverInsecureConnection(true);
        try {
            Localpart localpart = Localpart.from(username);
            accMan.createAccount(localpart,password);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
    }

    private void joinOrCreate(final EntityBareJid jid) throws XmppStringprepException, InterruptedException, SmackException.NoResponseException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException {
        Resourcepart nickname = Resourcepart.from(this.nickname);
        MucEnterConfiguration.Builder mec = multiChat.getEnterConfigurationBuilder(nickname);
        mec.requestHistorySince(Define.CHAT_HISTORY_LENGTH);
        MucEnterConfiguration mucEnterConf = mec.build();
        try {
            multiChat.createOrJoin(mucEnterConf).makeInstant();
            Form form = multiChat.getConfigurationForm();
            Form answerForm = form.createAnswerForm();
            answerForm.setAnswer("muc#roomconfig_roomdesc", subject);
            multiChat.sendConfigurationForm(answerForm);
        }catch (NullPointerException e){
            multiChat.join(nickname);

        }
        if(multiChat.isJoined()){
            multiChat.addMessageListener(this);
        }
    }

    public void sendMessage(String message){
        try {
            if (conn1.isConnected()){
                multiChat.sendMessage(message);
            }
            else {
                conn1.connect();
                multiChat.join(Resourcepart.from(this.nickname));
                multiChat.sendMessage(message);
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.MucAlreadyJoinedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        if (multiChat != null){
            multiChat.removeMessageListener(this);
            conn1.disconnect();
        }

        MessageSingleton.getInstance().clear();
    }










}
