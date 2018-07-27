package dasilver.jeong.chatpracticeandroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class ConnectFailDialog extends Dialog {

    private ImageButton cancelImageButton;
    private View.OnClickListener mCancelClickListener;


    public ConnectFailDialog(@NonNull Context context, View.OnClickListener cancelClickListener) {
        super(context);
        this.mCancelClickListener = cancelClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.dialog_connect_fail);

        cancelImageButton = (ImageButton) findViewById(R.id.btn_dialog_connect_fail_cancel);


        if (mCancelClickListener != null) {
            cancelImageButton.setOnClickListener(mCancelClickListener);
        } else {

        }

    }
}
