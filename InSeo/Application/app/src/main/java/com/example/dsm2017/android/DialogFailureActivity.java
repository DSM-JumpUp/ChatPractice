package com.example.dsm2017.android;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

public class DialogFailureActivity extends Dialog {

    public View.OnClickListener mCancelClick;
    private ImageButton cancelBtn;
    private Button OKBTN;


    public DialogFailureActivity(@NonNull Context context, View.OnClickListener failCancelClickListener) {
        super(context);
        this.mCancelClick = failCancelClickListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.activity_dialog_fail);

        cancelBtn = findViewById(R.id.dialog_con_imgBtn);
        OKBTN = findViewById(R.id.dialog_con_Btn);

        cancelBtn.setOnClickListener(mCancelClick);
        OKBTN.setOnClickListener(mCancelClick);
    }
}
