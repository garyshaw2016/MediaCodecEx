package com.milimili.mediacodecex.mediamuxer_ex;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by luxiansheng on 16/12/16.
 * 解码一个视频文件，分离出视频和音频，丢弃音频后将视频文件再次封装
 * 为视频文件
 */

public class ExtractMedia {
    private static final String TAG = "ExtractMedia";
    private static final String old = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/0.mp4";
    private static final String new_ = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/new.mp4";
    public static final String AUDIO = "audio/";
    public static final String VIDIO = "video/";

    private MediaExtractor extractor;
    private MediaMuxer muxer;

    public void runTest() {
        Thread th = new Thread(new RunnableTest());
        th.start();
    }

    private class RunnableTest implements Runnable {

        @Override
        public void run() {
            extractor = new MediaExtractor();
            try {
                extractor.setDataSource(old);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int frameRate = 24;
            //选择视频轨道
            int trackIndex = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mine.startsWith(AUDIO)) {//分离出音频
                    extractor.selectTrack(i);
                    if (trackFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        //设置frameRate为trackformat中的值
                        frameRate = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                    //创建编解码器和muxer
                    try {
                        muxer = new MediaMuxer(new_, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        trackIndex = muxer.addTrack(trackFormat);
                        muxer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                int sampleSize = extractor.readSampleData(byteBuffer, 0);
                if (sampleSize == -1) {//读完
                    break;
                }
                extractor.advance();
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                bufferInfo.size = sampleSize;
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;
                muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);

            }
            Log.i(TAG, "分离完成");
            //结束后释放
            extractor.release();
            muxer.stop();
            muxer.release();

        }
    }
}
