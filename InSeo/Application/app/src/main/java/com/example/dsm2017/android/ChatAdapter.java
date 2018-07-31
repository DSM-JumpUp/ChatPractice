package com.example.dsm2017.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    public static final int TYPE_MINE = 0;
    public static final int TYPE_PEER = 1;
    private ArrayList<ChatData> mMessageList;

    public ChatAdapter(ArrayList<ChatData> mMessageList){
        this.mMessageList = mMessageList;
    }



    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = -1;

        switch(viewType) {
            case TYPE_MINE:
                layout = R.layout.activity_sender_message;
                break;
            case TYPE_PEER:
                layout = R.layout.activity_peer_message;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        ChatData message = mMessageList.get(position);
        holder.mMessage.setText(message.getMessage());
        holder.mTime.setText(message.getTime());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getType();
    }

    class ChatHolder extends RecyclerView.ViewHolder {
        TextView mMessage;
        TextView mTime;
        public ChatHolder(View itemView) {
            super(itemView);
            mMessage = (TextView) itemView.findViewById(R.id.text_massage);
            mTime = (TextView) itemView.findViewById(R.id.text_time);
        }
    }
}
