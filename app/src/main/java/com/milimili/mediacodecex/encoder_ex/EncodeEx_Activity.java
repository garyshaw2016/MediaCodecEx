package com.milimili.mediacodecex.encoder_ex;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.milimili.mediacodecex.R;

public class EncodeEx_Activity extends AppCompatActivity
        implements SurfaceHolder.Callback{

    private static final String TAG = "EncodeEx_Activity";
    private SurfaceView surfaceView;
    private AvcEncoder avcEncoder;
    private Handler handler;
    private CameraThread cameraThread;

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
        cameraThread = new CameraThread("camera");
        cameraThread.start();
        handler = new Handler(cameraThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cameraThread.startCamera(holder,previewWidth,previewHeight);
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        avcEncoder = new AvcEncoder();
        avcEncoder.startEncoderThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        avcEncoder.stopEncoderThread();
        cameraThread.stopCamera();

    }
}
