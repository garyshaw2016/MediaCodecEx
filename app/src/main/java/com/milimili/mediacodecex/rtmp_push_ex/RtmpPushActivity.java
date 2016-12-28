package com.milimili.mediacodecex.rtmp_push_ex;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.milimili.mediacodecex.R;
import com.milimili.mediacodecex.encoder_ex.AvcEncoder;
import com.milimili.mediacodecex.encoder_ex.CameraThread;

public class RtmpPushActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private Handler handler;
    private CameraThreadA cameraThreadA;
    private AvcEncoderA avcEncoder;

    private int previewWidth = 1280;
    private int previewHeight = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_push);
        surfaceView = (SurfaceView) findViewById(R.id.previewSurfaceView);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        holder.setKeepScreenOn(true);
        cameraThreadA = new CameraThreadA("camera");
        cameraThreadA.start();
        handler = new Handler(cameraThreadA.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cameraThreadA.startCamera(holder,previewWidth,previewHeight);
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        avcEncoder = new AvcEncoderA(previewWidth,previewHeight);
        avcEncoder.startEncoderThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraThreadA.stopCamera();
        avcEncoder.stopEncoderThread();
    }
}
