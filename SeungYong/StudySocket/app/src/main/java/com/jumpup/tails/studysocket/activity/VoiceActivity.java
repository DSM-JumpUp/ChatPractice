package com.jumpup.tails.studysocket.activity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jumpup.tails.studysocket.R;
import com.jumpup.tails.studysocket.socket.SocketApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class VoiceActivity extends AppCompatActivity { // 미완성

    private boolean isRecoding = false;
    private Thread mRecordThread = null;
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44800;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    public AudioRecord mAudioRecord = null;
    public AudioTrack mAudioTrack = null;

    private Socket mSocket;
    byte[] readData = new byte[mBufferSize];
    Button btn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        SocketApplication application = (SocketApplication) getApplication();
        mSocket = application.getSocket();
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM); // AudioTrack 생성
        mAudioTrack.play();

        btn = findViewById(R.id.btn_voice);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecoding) {
                    isRecoding = true;
                    if (mAudioRecord == null) {
                        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
                        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                            mAudioRecord.startRecording();
                        }
                    }
                    mRecordThread.start();
                } else {
                    isRecoding = false;
                }
            }
        });

        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //byte[] readData = new byte[mBufferSize];
                readData = new byte[mBufferSize];
                while (isRecoding) {
                    mAudioRecord.read(readData, 0, mBufferSize);
                    StringBuilder sb = new StringBuilder();
                    for(final byte b: readData)
                        sb.append(String.format("%02x ", b&0xff));
                    Log.d("Input data", sb.toString());
                    mSocket.emit("send voice", readData);
                }
            }
        });
        mSocket.on("listen", onListen);
        mSocket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("listen", onListen);
        mSocket.disconnect();
    }

    private Emitter.Listener onListen = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (isRecoding) {

                mAudioTrack.write(readData, 0, readData.length);
            }
        }
    };
}
