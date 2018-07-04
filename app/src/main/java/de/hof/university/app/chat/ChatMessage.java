package de.hof.university.app.chat;

import java.time.LocalDate;

/**
 * Created and © by Christian G. Pfeiffer on 04.07.18.
 */
public class ChatMessage {
    private String sender,message;

    private ChatMessage(){
    }

    public ChatMessage(String mySender, String myMessage){
        this.sender = mySender;
        this.message = myMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
