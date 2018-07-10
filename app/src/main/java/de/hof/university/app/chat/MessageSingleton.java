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
