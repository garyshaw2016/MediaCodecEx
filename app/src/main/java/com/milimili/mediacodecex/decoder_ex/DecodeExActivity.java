package com.milimili.mediacodecex.decoder_ex;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.milimili.mediacodecex.R;

/**
 * 解码的练习Activity
 */
public class DecodeExActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private VideoPlayerThread player1;
    private AudioPlayerThread player2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_ex);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        String localPath = Environment.getExternalStorageDirectory().getPath()+"/0.mp4";
        player1 = new VideoPlayerThread(holder.getSurface(),localPath);
        player2 = new AudioPlayerThread(localPath);
        player1.start();
        player2.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player1!=null) {
            player1.interrupt();
        }
        if (player2!=null) {
            player2.interrupt();
        }
    }
}
