package com.example.dsm2017.android;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogAgainActivity extends Dialog {
    private Button reconnectBtn;
    private ImageButton cancelBtn;
    private int cannotConnectText, findAgaintext;
    private TextView conText, conText2;
    private View.OnClickListener againClick;
    private View.OnClickListener againSearch;

    public DialogAgainActivity(@NonNull Context context, int cannotConnectText, int findAgaintext, View.OnClickListener againCancelClickListener, View.OnClickListener againFindPeople) {
        super(context);
        this.cannotConnectText = cannotConnectText;
        this.findAgaintext = findAgaintext;
        this.againClick = againCancelClickListener;
        this.againSearch = againFindPeople;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.activity_dialog_again);

        reconnectBtn = findViewById(R.id.dialog_con_Btn);
        cancelBtn = findViewById(R.id.dialog_con_imgBtn);
        conText = findViewById(R.id.dialog_con_text);
        conText2 = findViewById(R.id.dialog_con_text2);

        conText.setText(cannotConnectText + " m 이내에서 사람을 찾지 못했습니다.");
        conText2.setText(findAgaintext + "m로 범위를 넓혀볼까요 ?");

        cancelBtn.setOnClickListener(againClick);
        reconnectBtn.setOnClickListener(againSearch);
    }
}
