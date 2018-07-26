package dasilver.jeong.chatpracticeandroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

public class ConnectDialog extends Dialog {

    private ImageButton cancelImageButton;
    private View.OnClickListener mCancelClickListener;

    public ConnectDialog(@NonNull Context context, View.OnClickListener cancelClickListener) {
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
        setContentView(R.layout.dialog_connect);

        cancelImageButton = (ImageButton) findViewById(R.id.btn_dialog_connect_cancel);

        if (mCancelClickListener != null) {
            cancelImageButton.setOnClickListener(mCancelClickListener);
        } else {

        }

    }
}
