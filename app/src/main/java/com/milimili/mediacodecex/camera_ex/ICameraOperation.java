package com.milimili.mediacodecex.camera_ex;

/**
 * Created by luxiansheng on 16/12/12.
 * 对相机的相关操作
 */

public interface ICameraOperation {
    //切换前后摄像头
    void switchCamera();
    //开关闪光灯
    void toggleFlashLight();
    //拍照
    void takePhoto();
    //录制视频
    void recordVideo();
}
