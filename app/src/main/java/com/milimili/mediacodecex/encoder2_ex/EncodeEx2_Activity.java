package com.milimili.mediacodecex.encoder2_ex;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.milimili.mediacodecex.R;

public class EncodeEx2_Activity extends AppCompatActivity
        implements SurfaceHolder.Callback{

    private static final String TAG = "EncodeEx_Activity";
    private SurfaceView surfaceView;
    private AvcEncoder2 avcEncoder2;
    private Handler handler;
    private CameraThread2 cameraThread2;

    private int previewWidth = 1280;
    private int previewHeight = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode_ex_);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        cameraThread2 = new CameraThread2("camera");
        cameraThread2.start();
        handler = new Handler(cameraThread2.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cameraThread2.startCamera(holder,previewWidth,previewHeight);
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        avcEncoder2 = new AvcEncoder2();
        avcEncoder2.startEncoderThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        avcEncoder2.stopEncoderThread();
        cameraThread2.stopCamera();

    }
}
