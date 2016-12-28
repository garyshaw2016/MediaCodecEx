package com.milimili.mediacodecex.rtmp_push_ex;

/**
 * Created by luxiansheng on 16/12/20.
 * JniUtil
 */
public class RtmpUtil {

    public static final String pushUrl = "http://www.b.cxria.com/live/666666";

    static {
        System.loadLibrary("rtmppush");
    }

    public static native String getStringFromNative();

    public static native int connect(String url);

    public static native int sendVideoSpsPps(byte[] pps, int ppsLen, byte[] sps, int spsLen);

    public static native int sendVideoData(byte[] videoDatas, int dataLen, boolean isKeyFrame,
                                           int timeStamp);
    public static native void close();

}
