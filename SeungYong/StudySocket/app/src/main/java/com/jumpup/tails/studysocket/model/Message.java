package com.jumpup.tails.studysocket.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Message {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;

    private int mType;
    private String mMessage;
    private String mTime;

    public Message(){ }

    public int getType(){
        return this.mType;
    }

    public String getMessage(){
        return this.mMessage;
    }

    public String getTime(){
        return this.mTime;
    }

    public static class Builder{
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        public Message Build(int mType, String mMessage) {
            Message message = new Message();
            message.mType = mType;
            message.mMessage = mMessage;
            message.mTime = dateFormat.format(cal.getTime());
            return message;
        }
    }
}
