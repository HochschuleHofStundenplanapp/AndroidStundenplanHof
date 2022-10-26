/*
 * Copyright (c) 2018-2019 Hof University
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
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.form.Form;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import de.hof.university.app.util.Define;

public class ChatCommunicator extends AsyncTask<String, String, String> implements MessageListener {

    private final static String TAG = "ChatCommunicator";

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
                Log.e( TAG, "BOSH connect error 1", e);
                return "";
            } catch (InterruptedException e) {
                Log.e( TAG, "BOSH connect error 2", e);
                //e.printStackTrace();
            } catch (IOException e) {
                Log.e( TAG, "BOSH connect error 3", e);
                //e.printStackTrace();
            } catch (SmackException e) {
                Log.e( TAG, "BOSH connect error 4", e);
                //e.printStackTrace();
            } catch (XMPPException e) {
                Log.e( TAG, "BOSH connect error 5", e);
                //e.printStackTrace();
            } catch ( final IllegalStateException e) {
                Log.e( TAG, "BOSH connect error 50", e );

            } catch ( final ClassCastException e ) {
                // in case of error, the SSL Library wants to Log something. Since SDK28 onwards there is problem:
                // The Logfactory changed somehow internal
                Log.e( TAG, "BOSH connect error 51",e);
                return "";
            }
            catch ( final Exception e ) {
                Log.e( TAG, "BOSH connect error 52: general purpose handler", e );
                return "";
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
        } catch (XmppStringprepException e) {
            Log.e( TAG, "BOSH connect error 6", e);
            //e.printStackTrace();
            return "";
        } catch ( final ClassCastException e ) {
            // in case of error, the SSL Library wants to Log something. Since SDK28 onwards there is problem:
            // The Logfactory changed somehow internal
            Log.e( TAG, "BOSH connect error 51",e);
            return "";
        }
        catch ( final Exception e ) {
            Log.e( TAG, "BOSH connect error 60: general purpose handler", e );
            return "";
        }


        conn1 = new XMPPBOSHConnection(config);

        try {
            conn1.connect();
            conn1.login();
        } catch (XMPPException e) {
            Log.e( TAG, "BOSH connect error 7", e);
            //e.printStackTrace();
            disconnect();
            return "";
        } catch (SmackException e) {
            Log.e( TAG, "BOSH connect error 8", e);
            //e.printStackTrace();
        } catch (IOException e) {
            Log.e( TAG, "BOSH connect error 9", e);
            //e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e( TAG, "BOSH connect error 10", e);
            //e.printStackTrace();
        } catch (NullPointerException e) {
            /* TODO: --> https://app.hof-university.de:433/ tells me it works ? and then blocks my ip ? :D
             * Here's my original error:
             * Connection XMPPBOSHConnection[not-authenticated] (0) closed with error org.igniterealtime.jbosh.BOSHException: Could not parse body:
                <!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
                <html><head>
                <title>503 Service Unavailable</title>
                </head><body>
                <h1>Service Unavailable</h1>
                <p>The server is temporarily unable to service your request due to maintenance downtime or capacity problems. Please try again later.</p>
                <hr>
                <address>Apache/2.4.18 (Ubuntu) Server at app.hof-university.de Port 443</address>
                </body></html>
             */
            disconnect();
            Log.e("ChatCommunicator", "Connection XMPPBOSHConnection[not-authenticated] closed with error", e);
            return "";
        } catch ( final ClassCastException e ) {
            // in case of error, the SSL Library wants to Log something. Since SDK28 onwards there is problem:
            // The Logfactory changed somehow internal
            Log.e( TAG, "BOSH connect error 100",e);
            return "";
        } catch ( final Exception e ) {
            Log.e( TAG, "BOSH connect error 101: general purpose handler", e );
            return "";
        }


        multiChatManager = MultiUserChatManager.getInstanceFor(conn1);
        try {
            jid = JidCreate.entityBareFrom(roomname + "@" + "chat.sl-app01.hof-university.de");
        } catch (XmppStringprepException e) {
            Log.e(TAG, "BOSH connect error 17", e);
            //e.printStackTrace();
        }

        multiChat = multiChatManager.getMultiUserChat(jid);
        try {
            joinOrCreate(jid);
        } catch (InterruptedException e) {
            Log.e( TAG, "BOSH connect error 11", e);
            //e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            Log.e( TAG, "BOSH connect error 12", e);
            //e.printStackTrace();
        } catch (MultiUserChatException.MucAlreadyJoinedException e) {
            Log.e( TAG, "BOSH connect error 13", e);
            //e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            Log.e( TAG, "BOSH connect error 14", e);
            //e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            Log.e( TAG, "BOSH connect error 15", e);
            //e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            Log.e( TAG, "BOSH connect error 16", e);
            //e.printStackTrace();
        } catch (final XmppStringprepException e) {
            Log.e( TAG, "BOSH connect error 161", e);
        } catch (final IllegalArgumentException e ) {
            Log.e( TAG, "BOSH connect error 162", e);
        } catch (final Exception e ) {
            Log.e( TAG, "BOSH connect error 163: general purpose handler", e);
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
        if (accMan == null )
            return;
        accMan.sensitiveOperationOverInsecureConnection(true);
        try {
            final Localpart localpart = Localpart.from(username);
            accMan.createAccount(localpart,password);
        } catch (XmppStringprepException e) {
            Log.e( TAG, "BOSH connect error 20", e);
            //e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e( TAG, "BOSH connect error 21", e);
            //e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            Log.e( TAG, "BOSH connect error 22", e);
            //e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            Log.e( TAG, "BOSH connect error 23", e);
            //e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            Log.e( TAG, "BOSH connect error 24", e);
            //e.printStackTrace();
        } catch (final NullPointerException e ) {
            Log.e( TAG, "BOSH no connection to server 241", e );
        } catch (final Exception e) {
            //general purpose catcher to avoid any thrown exception causing an app crash
            Log.e( TAG, "BOSH general purpose exception catcher 242", e );
        }


    }

    private void joinOrCreate(final EntityBareJid jid) throws XmppStringprepException, InterruptedException, SmackException.NoResponseException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException, IllegalArgumentException {
        final Resourcepart nickname = Resourcepart.from(this.nickname);
        final MucEnterConfiguration.Builder mec = multiChat.getEnterConfigurationBuilder(nickname);
        if (mec == null)
            return;
        mec.requestHistorySince(Define.CHAT_HISTORY_LENGTH);
        final MucEnterConfiguration mucEnterConf = mec.build();
        try {
            multiChat.createOrJoin(mucEnterConf).makeInstant();
            final Form form = multiChat.getConfigurationForm();
            final FillableForm answerForm = form.getFillableForm();
            answerForm.setAnswer("muc#roomconfig_roomdesc", subject);
            multiChat.sendConfigurationForm(answerForm);
        } catch (final NullPointerException e){
            multiChat.join(nickname);
        }
        catch (final Exception e){
            Log.e(TAG, "createOrJoin fails", e);
            throw ( e ) ;
        }

        if(multiChat.isJoined()){
            multiChat.addMessageListener(this);
        }
    }

    public void sendMessage(final String message){
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
            Log.e( TAG, "BOSH connect error 30", e);
            //e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e( TAG, "BOSH connect error 31", e);
            //e.printStackTrace();
        } catch (MultiUserChatException.MucAlreadyJoinedException e) {
            Log.e( TAG, "BOSH connect error 32", e);
            //e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            Log.e( TAG, "BOSH connect error 33", e);
            //e.printStackTrace();
        } catch (XmppStringprepException e) {
            Log.e( TAG, "BOSH connect error 34", e);
            //e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            Log.e( TAG, "BOSH connect error 35", e);
            //e.printStackTrace();
        } catch (MultiUserChatException.NotAMucServiceException e) {
            Log.e( TAG, "BOSH connect error 36", e);
            //e.printStackTrace();
        } catch (XMPPException e) {
            Log.e( TAG, "BOSH connect error 37", e);
            //e.printStackTrace();
        } catch (IOException e) {
            Log.e( TAG, "BOSH connect error 38", e);
            //e.printStackTrace();
        } catch (SmackException e) {
            Log.e( TAG, "BOSH connect error 39", e);
            //e.printStackTrace();
        }
        catch (final IllegalArgumentException e) {
            Log.e( TAG, "BOSH connect error 398", e);
        }
        catch (final Exception e ) {
            Log.e( TAG, "BOSH connect error 399", e);
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
