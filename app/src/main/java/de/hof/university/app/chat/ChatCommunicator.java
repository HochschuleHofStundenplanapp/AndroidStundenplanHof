package de.hof.university.app.chat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
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
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import de.hof.university.app.chat.Helper.StringTrimmer;

public class ChatCommunicator extends AsyncTask<String, String, String> implements MessageListener {


    private XMPPBOSHConnection conn1;
    private String nickname = "testuser2";
    private String password = "TestUser2";
    private Boolean newUser = false;
    private MultiUserChat multiChat;

    private MultiUserChatManager multiChatManager;
    private String roomname;
    private EntityBareJid jid;
    private String subject;



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    @Override
    protected String doInBackground(String... strings) {

        BOSHConfiguration config;
        if(newUser){
            try {
                config = BOSHConfiguration.builder()
                        .setUseHttps(true)
                        .setXmppDomain("sl-app01.hof-university.de")
                        .setFile("/http-bind/")
                        .setHost("app.hof-university.de")
                        .setPort(443)
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
                        .setXmppDomain("sl-app01.hof-university.de")
                        .setFile("/http-bind/")
                        .setHost("app.hof-university.de")
                        .setPort(443)
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
        StringTrimmer trimmer = new StringTrimmer();
        String sender = trimmer.trimmStartingAt(message.getFrom().toString(),'/');
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

    private void joinOrCreate(EntityBareJid jid) throws XmppStringprepException, InterruptedException, SmackException.NoResponseException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, XMPPException.XMPPErrorException, MultiUserChatException.NotAMucServiceException {
        Resourcepart nickname = Resourcepart.from(this.nickname);
        MucEnterConfiguration.Builder mec = multiChat.getEnterConfigurationBuilder(nickname);
        mec.requestHistorySince(600000);
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
            multiChat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
