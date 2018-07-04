package de.hof.university.app.chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;

/**
 * Created and © by Christian G. Pfeiffer on 04.07.18.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{
    private ArrayList<ChatMessage> chatList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
       public TextView sender,textmessage;


        public MyViewHolder(View itemView) {
            super(itemView);

            sender = itemView.findViewById(R.id.senderTextView);
            textmessage = itemView.findViewById(R.id.messageTextView);
        }
    }

    public ChatAdapter(ArrayList<ChatMessage> chatList) {
        this.chatList = chatList;
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
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
