package com.milimili.mediacodecex.decoder_ex;

import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by luxiansheng on 16/12/12.
 * 简易的视频播放器（解码器）
 */

public class VideoPlayerThread extends Thread {
    private static final String TAG = "VideoPlayerThread";
    private static  String sampleVideoPath;

    private MediaCodec videoDecoder;
    private MediaExtractor mediaExtractor;
    private Surface surface;
    private AudioTrack audioTrack;

    public VideoPlayerThread(Surface surface, String localVideoPath) {
        this.surface = surface;
        sampleVideoPath = localVideoPath;
    }

    @Override
    public void run() {
        mediaExtractor = new MediaExtractor();

        try {
            mediaExtractor.setDataSource(sampleVideoPath);
            //如果有多个文件，取第一个视频类型的文件
            for (int i=0;i<mediaExtractor.getTrackCount();i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mine.startsWith("video/")) {
                    mediaExtractor.selectTrack(i);
                    videoDecoder = MediaCodec.createDecoderByType(mine);
                    videoDecoder.configure(trackFormat,surface,null,0);
                    break;
                }
            }

            if (videoDecoder ==null) {
                Log.e(TAG,"videoDecoder==null,make sure the video exists");
                return;
            }

            //开始解码
            videoDecoder.start();

            //获取输入输出Buffers
            ByteBuffer[] inputBuffers = videoDecoder.getInputBuffers();
            ByteBuffer[] outputBuffers = videoDecoder.getOutputBuffers();

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();

            boolean isEos = false;
            while (!Thread.interrupted()) {
                if (!isEos) {
                    int inIndex = videoDecoder.dequeueInputBuffer(10000);
                    if (inIndex>=0) {
                        ByteBuffer inputBuffer = inputBuffers[inIndex];
                        int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                        if (sampleSize<0) {
                            videoDecoder.queueInputBuffer(inIndex,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEos = true;
                        }else {
                            videoDecoder.queueInputBuffer(inIndex,0,sampleSize,mediaExtractor.getSampleTime(),0);
                            mediaExtractor.advance();
                        }
                    }
                }

                int outIndex = videoDecoder.dequeueOutputBuffer(bufferInfo,10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = videoDecoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "New format " + videoDecoder.getOutputFormat());
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;
                    default:
                        ByteBuffer buffer = outputBuffers[outIndex];
                        Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);
                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast
                        while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        videoDecoder.releaseOutputBuffer(outIndex,true);
                        break;

                }

                //停止播放
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }

            }

            videoDecoder.stop();
            videoDecoder.release();
            mediaExtractor.release();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
