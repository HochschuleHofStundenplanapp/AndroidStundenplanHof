package de.hof.university.app.chat;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

class MessageSingleton extends Observable {

    private static final MessageSingleton ourInstance = new MessageSingleton();
    private ArrayList<ChatMessage> chatMessageArrayList;

    static MessageSingleton getInstance() {
        return ourInstance;
    }

    private MessageSingleton() {
        chatMessageArrayList = new ArrayList<ChatMessage>();
    }

    public void appendMessage(ChatMessage message){
        chatMessageArrayList.add(message);
        setChanged();
        notifyObservers();
    }
    public ArrayList<ChatMessage> getMessages(){
        return chatMessageArrayList;
    }
}
