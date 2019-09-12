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

import android.content.Context;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;

/**
 * Created and © by Christian G. Pfeiffer on 04.07.18.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{
    private final ArrayList<ChatMessage> chatList;
    public String thisUser  = "";

    public class MyViewHolder extends RecyclerView.ViewHolder {
       public final TextView sender;
        public final TextView textmessage;


        public MyViewHolder(View itemView) {
            super(itemView);

            sender = itemView.findViewById(R.id.senderTextView);
            textmessage = itemView.findViewById(R.id.messageTextView);
        }

    }

    public ChatAdapter(ArrayList<ChatMessage> chatList) {
        this.chatList = chatList;

        final String PREFERENCES_CHAT = "chatPrefs";
        final String USERNAME_KEY = "username";

        final SharedPreferences prefs = MainActivity.getAppContext().getSharedPreferences(PREFERENCES_CHAT, Context.MODE_PRIVATE);
        thisUser =  prefs.getString(USERNAME_KEY,"lul");

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);
        holder.sender.setText(message.getSender());
        holder.textmessage.setText(message.getMessage());

        if ((thisUser).equals(message.getSender())){
            holder.sender.setTextColor(holder.sender.getResources().getColor(R.color.Accent_complement));
        }
        else {
            holder.sender.setTextColor(holder.sender.getResources().getColor(R.color.Primary_backup));
        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
