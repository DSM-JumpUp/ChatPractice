package com.example.dsm2017.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ChatData {


    private String mMessage;
    private int mType;
    private String mTime;

    public int getType() {
        return mType;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getTime() {
        return mTime;
    }

    public static class Builder {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        public ChatData Build(int mType, String mMessage) {
            ChatData message = new ChatData();
            message.mType = mType;
            message.mMessage = mMessage;
            message.mTime = dateFormat.format(cal.getTime());
            return message;
        }
    }
}
