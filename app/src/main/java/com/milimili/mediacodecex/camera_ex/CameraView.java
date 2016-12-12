package com.milimili.mediacodecex.camera_ex;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luxiansheng on 16/12/11.
 * 相机View
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback, ICameraOperation{
    private static final String TAG = "CameraView";
    private Camera camera;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private FlashLightsStatus flashLightsStatus;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        getHolder().addCallback(this);
    }

    private void init() {
        flashLightsStatus = FlashLightsStatus.LIGHT_OFF;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setKeepScreenOn(true);
        openCameraOnHolder(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setCameraDisplayOrientation();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
        if (holder!=null) {
            if (Build.VERSION.SDK_INT >=-14) {
                holder.getSurface().release();
            }
        }
    }

    /**
     * 打开相机
     * @param holder surfaceHolder
     */
    public void openCameraOnHolder(SurfaceHolder holder) {
        //初始化相机
        //获取相机实例
        camera = Camera.open(cameraId);
        try {
            //设置相机显示的目的surface
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCamera();
    }

    /**
     * 初始化相机和设置其参数
     */
    private void initCamera() {
        //相机参数设置
        Camera.Parameters parameters = camera.getParameters();
        //1.设置最优预览尺寸,宽高必须是摄像头支持的数值
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        Camera.Size bestSize = sizeList.get(0);
        for (int i = 0; i < sizeList.size(); i++) {
            Log.d(TAG, "supported width:" + sizeList.get(i).width +
                    ",height:" + sizeList.get(i).height);
            if (sizeList.get(i).width * sizeList.get(i).height > bestSize.width * bestSize.height) {
                bestSize = sizeList.get(i);
            }
        }
        Log.d(TAG, "setPreviewSize:width:" + bestSize.width + ",height:" + bestSize.height);
        parameters.setPreviewSize(bestSize.width, bestSize.height);
        //2.设置相机预览方向
        setCameraDisplayOrientation();
        //3.设置相机拍照后的照片方向
//        parameters.setRotation(90);
        //4.自动对焦(视频）
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        for (int i=0;i<supportedFocusModes.size();i++) {
            String s = supportedFocusModes.get(i);
            Log.d(TAG,"supportFocusMode:"+ s);
            if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(s)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }else if (Camera.Parameters.FOCUS_MODE_FIXED.equals(s)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            }
        }
        //5.获取相机（不）支持的闪光灯模式
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes!=null) {
            if (!supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                flashLightsStatus.getFlashLightNotSupportList().add(FlashLightsStatus.LIGHT_AUTO);
            }
            for (int i=0;i<supportedFlashModes.size();i++) {
                Log.i(TAG,supportedFlashModes.get(i));
            }

        }

        //预览的回调
        camera.setPreviewCallback(this);

        camera.setParameters(parameters);
        //开始预览
        camera.startPreview();
    }

    /**
     * 根据屏幕方向设置相机的展示方向
     */
    public void setCameraDisplayOrientation() {
        //如果屏幕方向发生改变，则改变相机方向和参数
        if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(0);//横屏
        }else if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
            camera.setDisplayOrientation(90);//竖屏
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        } else {
            Log.w(TAG, "camera==null");
        }
    }

    @Override
    public void switchCamera() {
        cameraId = 1 - cameraId;
        releaseCamera();
        openCameraOnHolder(getHolder());
    }

    @Override
    public void toggleFlashLight() {
        if (camera!=null) {
            List<String> supportedFlashModes = camera.getParameters().getSupportedFlashModes();
            if (supportedFlashModes.size()>0) {

            }
        }
        turnFlashLight(flashLightsStatus.next());
    }

    private void turnFlashLight(FlashLightsStatus next) {
        flashLightsStatus = next;
        if (camera!=null) {
            Camera.Parameters parameters = camera.getParameters();
            switch (next) {
                case LIGHT_AUTO:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
                case LIGHT_ON:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    break;
                case LIGHT_OFF:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
                default:
                    break;
            }
            camera.setParameters(parameters);
        }

    }

    @Override
    public void takePhoto() {

    }

    @Override
    public void recordVideo() {

    }

    /**
     * Camera 预览回调 可以拿到每一帧的流数据
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                Point point = new Point((int)event.getX(),(int)event.getY());
                onFocus(point,autoFocusCallback);
                break;
        }
        return true;
    }

    /**
     * 手动聚焦
     *
     * @param point 触屏坐标
     */
    protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = camera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if(Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

            //定点对焦
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                camera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }


        return focus(callback);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            camera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {

        }
    };

    private enum FlashLightsStatus {
        LIGHT_AUTO,LIGHT_ON,LIGHT_OFF;

        /**
         * 不受摄像头支持的闪光灯模式
         */
        private List<FlashLightsStatus> flashLightNotSupportList;
        FlashLightsStatus() {
            flashLightNotSupportList = new ArrayList<>();
        }

        public List<FlashLightsStatus> getFlashLightNotSupportList() {
            return flashLightNotSupportList;
        }

        //不断循环的枚举
        public FlashLightsStatus next() {
            int index = ordinal();
            int len = values().length;
            FlashLightsStatus status = values()[(index+1) % len];
            if (!flashLightNotSupportList.contains(status)) {
                return status;
            }else {
                return next();
            }
        }

        public static FlashLightsStatus valueOf(int index) {
            return values()[index];
        }
    }

}
