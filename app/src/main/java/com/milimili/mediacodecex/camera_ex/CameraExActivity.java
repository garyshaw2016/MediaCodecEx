package com.milimili.mediacodecex.camera_ex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.milimili.mediacodecex.R;

/**
 * 此Activity实验了如下功能：
 * 1.相机预览
 * 2.自动对焦(前后置）
 * 3.屏幕旋转，相机自动旋转
 * 4.SurfaceView置顶(setZOrderOnTop)的含义及其效果
 * 5.手动对焦
 * 6.拍照功能
 * 7.相机录像功能
 */
public class CameraExActivity extends AppCompatActivity{

    private static final String TAG = "CameraExActivity";

    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_ex);

        cameraView = (CameraView) findViewById(R.id.cameraView);

        Button switchCamera = (Button) findViewById(R.id.btn_switch_camera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.switchCamera();
            }
        });

        Button switchLight = (Button) findViewById(R.id.btn_switch_flash_mode);
        switchLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFlashLight();
            }
        });

        Button takePhoto = (Button) findViewById(R.id.btn_take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePhoto();
            }
        });

        final Button recordVideo = (Button) findViewById(R.id.btn_record);
        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isRecording()) {
                    //停止录制
                    cameraView.stopRecord();
                    recordVideo.setText("录制");
                }else {
                    cameraView.startRecord();
                    recordVideo.setText("停止录制");
                }
            }
        });



    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraView.isRecording()) {
            cameraView.stopRecord();
        }
    }
}
