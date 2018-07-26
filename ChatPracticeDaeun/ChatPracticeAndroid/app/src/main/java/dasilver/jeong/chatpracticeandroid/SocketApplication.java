package dasilver.jeong.chatpracticeandroid;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;


public class SocketApplication extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.156.145.143:3000");
        } catch (URISyntaxException ue) {
            ue.printStackTrace();
        }
    }
    public Socket getSocket() {
        return mSocket;
    }
}

