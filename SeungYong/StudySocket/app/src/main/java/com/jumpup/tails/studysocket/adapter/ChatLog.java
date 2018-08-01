package com.jumpup.tails.studysocket.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jumpup.tails.studysocket.R;
import com.jumpup.tails.studysocket.model.Message;

import java.util.ArrayList;

import static com.jumpup.tails.studysocket.model.Message.TYPE_ACTION;
import static com.jumpup.tails.studysocket.model.Message.TYPE_LOG;
import static com.jumpup.tails.studysocket.model.Message.TYPE_MESSAGE;

public class ChatLog extends RecyclerView.Adapter<ChatLog.ViewHolder> {

    private ArrayList<Message> mMessageList;

    public ChatLog(ArrayList<Message> mMessageList){
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = -1;

        switch (viewType){
            case TYPE_MESSAGE: // 내 채팅
                layout = R.layout.activity_message; break;
            case TYPE_LOG: // 상대방 채팅
                layout = R.layout.activity_log; break;
            case TYPE_ACTION: // 상대방에 채팅을 치고 있을 때
                layout = R.layout.activity_action; break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        holder.tvMessage.setText(message.getMessage());
        holder.tvTime.setText(message.getTime());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvMessage;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.text_message);
            tvTime = itemView.findViewById(R.id.text_time);
        }
    }
}
