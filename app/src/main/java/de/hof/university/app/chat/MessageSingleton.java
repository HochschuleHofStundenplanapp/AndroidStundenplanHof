package de.hof.university.app.chat;

import java.util.ArrayList;
import java.util.Observable;

public class MessageSingleton extends Observable {

    private static final MessageSingleton ourInstance = new MessageSingleton();
    private ArrayList<ChatMessage> chatMessageArrayList;
    public enum ErrorEnum {
        working,
        networkUnavailable,
        openfireUnvailable,
        defaultError
    }
    private ErrorEnum status;

    public static MessageSingleton getInstance() {
        return ourInstance;
    }

    private MessageSingleton() {
        chatMessageArrayList = new ArrayList<ChatMessage>();
        status = ErrorEnum.working;
    }

    public void appendMessage(ChatMessage message){
        chatMessageArrayList.add(message);
        setChanged();
        notifyObservers();
    }
    public ArrayList<ChatMessage> getMessages(){
        return chatMessageArrayList;
    }

    public void clear(){
        this.chatMessageArrayList = new ArrayList<ChatMessage>();
    }

    public void changeStatus(ErrorEnum status){
        this.status = status;
        notifyObservers();
    }

    public ErrorEnum getStatus() {
        return status;
    }
}
