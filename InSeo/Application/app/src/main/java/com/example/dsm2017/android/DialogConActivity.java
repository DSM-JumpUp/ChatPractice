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

public class DialogConActivity extends Dialog {

    private View.OnClickListener mCancelClick;
    private ImageButton cancelBtn;

    public DialogConActivity(@NonNull Context context, View.OnClickListener onCancel) {
        super(context);
        this.mCancelClick = onCancel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.activity_dialog_con);

        cancelBtn = findViewById(R.id.dialog_con_imgBtn);

        cancelBtn.setOnClickListener((View.OnClickListener) mCancelClick);
    }
}
