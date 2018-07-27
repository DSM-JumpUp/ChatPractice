package dasilver.jeong.chatpracticeandroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConnectAgainDialog extends Dialog {

    private Button connectAgainButton;
    private ImageButton connectAgainCancelButton;
    private TextView cannotConnectTextView, findAgainTextView;
    private View.OnClickListener mAgainCancelClickListener;
    private String cannotConnectText, findAgaintext;
    private ProgressBar connectAgainProgressBar;

    public ConnectAgainDialog(@NonNull Context context, String cannotConnectText, String findAgaintext, View.OnClickListener againCancelClickListener) {
        super(context);
        this.cannotConnectText = cannotConnectText;
        this.findAgaintext = findAgaintext;
        this.mAgainCancelClickListener = againCancelClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.dialog_connect_again);

        connectAgainButton = (Button)findViewById(R.id.btn_connect_again);
        cannotConnectTextView = (TextView)findViewById(R.id.text_connect_again_cannot_connect);
        findAgainTextView = (TextView)findViewById(R.id.text_connect_again_find_again);
        connectAgainProgressBar = (ProgressBar)findViewById(R.id.progress_connect_again);
        connectAgainCancelButton = (ImageButton) findViewById(R.id.btn_dialog_connect_again_cancel);


        cannotConnectTextView.setText(cannotConnectText+"m이내 사람들을 찾지 못하였습니다.");
        findAgainTextView.setText(findAgaintext+"m이내로 다시 탐색하겠습니까?");

        if (mAgainCancelClickListener != null) {
            connectAgainCancelButton.setOnClickListener(mAgainCancelClickListener);
        } else {

        }

        connectAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectAgainProgressBar.setVisibility(View.VISIBLE);
            }
        });

    }
}
