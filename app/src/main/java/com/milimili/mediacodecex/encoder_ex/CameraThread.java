package com.milimili.mediacodecex.encoder_ex;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by luxiansheng on 16/12/14.
 * 获取Camera回调的线程
 */

public class CameraThread extends HandlerThread implements Camera.PreviewCallback {
    private static final String TAG = "CameraThread";
    private Camera camera;
    private long timeStamp;
    int frameNumber;

    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<>(10);

    public CameraThread(String name) {
        super(name);
    }

    public void startCamera(final SurfaceHolder holder, int previewWidth, int previewHeight) {
        camera = Camera.open();
        //简单配置
        camera.setDisplayOrientation(90);//竖屏

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);//needed
        parameters.setPreviewSize(previewWidth, previewHeight);
        int dataBufferSize = (int) (previewWidth * previewHeight *
                (ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat()) / 8.0));
        camera.addCallbackBuffer(new byte[dataBufferSize]);
        camera.setPreviewCallbackWithBuffer(this);
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCamera() {
        camera.setPreviewCallbackWithBuffer(null);
        timeStamp = System.currentTimeMillis();
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, "Get Frame:" + (frameNumber++) + ",time:" + (System.currentTimeMillis() - timeStamp));
        timeStamp = System.currentTimeMillis();
        fillData(data);
        camera.addCallbackBuffer(data);
    }


    /**
     * 往队列中填充数据,填满十个就送出
     *
     * @param data
     */
    private void fillData(byte[] data) {
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
        }
        YUVQueue.add(data);
    }
}
