//
// Created by 卢先生 on 16/12/20.
//
#include "rtmp_publisher.h"
#include "librtmp/rtmp.h"
#include "stdlib.h"
#include <android/log.h>

#define LOG_TAG "rtmp_publisher"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define RTMP_HEAD_SIZE (sizeof(RTMPPacket)+RTMP_MAX_HEADER_SIZE)

RTMP *m_rtmp;
int startTime;
/*
 * Class:     io_github_yanbober_ndkapplication_NdkJniUtils
 * Method:    getCLanguageString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_milimili_nativeex_JniUtil_getStringFromNative
        (JNIEnv *env, jclass obj) {
    return (*env)->NewStringUTF(env, "This just a test for Android Studio NDK JNI developer2!");
}

/**
 * 连接到rtmp服务器
 */
JNIEXPORT jint JNICALL Java_com_milimili_nativeex_JniUtil_connect
        (JNIEnv *jniEnv, jclass obj, jstring url) {
    jboolean *isCopy = (jboolean *) JNI_TRUE;
    const char *cUrl = (*jniEnv)->GetStringUTFChars(jniEnv, url, isCopy);
    LOGI("===rtmp address is:%s", cUrl);

    m_rtmp = RTMP_Alloc();
    RTMP_Init(m_rtmp);
    //设置url
    if (RTMP_SetupURL(m_rtmp, (char *) cUrl) == FALSE) {
        //url设置失败
        RTMP_Free(m_rtmp);
        LOGE("setUp Rtmp Url Failed\n");
        return FALSE;
    }

    //表示推流
    RTMP_EnableWrite(m_rtmp);

    //连接
    if (RTMP_ConnectStream(m_rtmp, 0) == FALSE) {
        //连接失败
        RTMP_Free(m_rtmp);
        LOGE("connectStreamFailed\n");
        return FALSE;
    }

    //连接成功
    startTime = RTMP_GetTime();
    LOGI("connect success");
    return TRUE;
}

/**
 * 在发送视频数据前，需要，先将sps,pps信息，发送出去，sps,pps获取可查看编码器的相关文档
 */
JNIEXPORT int JNICALL Java_com_milimili_nativeex_JniUtil_sendVideoSpsPps
        (JNIEnv *jniEnv, jclass jobj, jbyteArray jpps, jint pps_len, jbyteArray jsps,
         jint sps_len) {
    jbyte *jBytepps = (*jniEnv)->GetByteArrayElements(jniEnv, jpps, (jboolean *) FALSE);
    jbyte *jBytesps = (*jniEnv)->GetByteArrayElements(jniEnv, jsps, (jboolean *) FALSE);
    unsigned char *pps = (unsigned char *) jBytepps;
    unsigned char *sps = (unsigned char *) jBytesps;
    RTMPPacket *packet = NULL;
    unsigned char *body = NULL;
    int i;
    //分配空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE +
    // 1024，1024是随便给的，足够保存sps,pps的数据
    packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + 1024);
    memset(packet, 0, sizeof(packet));
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;//设置rtmppacket数据区指针
    body = (unsigned char *) packet->m_body;//定义一个临时指针，准备为数据区赋值
    i = 0;
    body[i++] = 0x17;//表示avc
    body[i++] = 0x00;// avc sequeues heade

    body[i++] = 0x00;//avc 时，全0，无意义
    body[i++] = 0x00;
    body[i++] = 0x00;

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;// configuratuin version
    body[i++] = sps[1];// avcprofileindication
    body[i++] = sps[2];// profile_compatibility
    body[i++] = sps[3];// avclevelIndication
    body[i++] = 0xff;// lengthSizeMinusOne --nalu包长数据使用的字节数 = （lengthSizeMinusOne & 3 ）+ 1

    /*sps*/
    body[i++] = 0xe1;// numofsequeueceParameterSets -- sps个数 = numofsequeueceParameterSets & 0x1F
    body[i++] = (sps_len >> 8) & 0xff;//此字节和下一个字节一起，表示sps长度
    body[i++] = sps_len & 0xff;
    memcpy(&body[i], sps, sps_len);//sps内容
    i += sps_len;

    /*pps*/
    body[i++] = 0x01;//pps 个数
    body[i++] = (pps_len >> 8) & 0xff;//此字节和下一个字节一起，表示pps长度
    body[i++] = (pps_len) & 0xff;
    memcpy(&body[i], pps, pps_len);//pps内容
    i += pps_len;

    //构造rtmpPacket
    packet->m_nBodySize = (uint32_t) i;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_hasAbsTimestamp = 0x01;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    if (m_rtmp == NULL) {
        free(packet);
        return 0;
    }
    packet->m_nInfoField2 = m_rtmp->m_stream_id;

    //发送
    RTMP_SendPacket(m_rtmp, packet, TRUE);
}

JNIEXPORT jint JNICALL Java_com_milimili_nativeex_JniUtil_sendVideoData
        (JNIEnv *jniEnv, jclass jobj, jbyteArray data, jint dataLen, jboolean isKeyFrame,
         jint timeStamp) {

    if (data == NULL) {
        return 0;
    }

    unsigned char *body = malloc((size_t) dataLen + 9);
    memset(body, 0, (size_t) dataLen + 9);

    int i = 0;
    if (isKeyFrame) {
        body[i++] = 0x17;// 1:Iframe  7:AVC
        body[i++] = 0x01;// AVC NALU
        body[i++] = 0x00;//avc时，全0，无意义
        body[i++] = 0x00;
        body[i++] = 0x00;


        // NALU size
        body[i++] = dataLen >> 24 & 0xff;
        body[i++] = dataLen >> 16 & 0xff;
        body[i++] = dataLen >> 8 & 0xff;
        body[i++] = dataLen & 0xff;
        // NALU data
        memcpy(&body[i], data, (size_t) dataLen);
    } else {
        body[i++] = 0x27;// 2:Pframe  7:AVC
        body[i++] = 0x01;// AVC NALU
        body[i++] = 0x00;//avc时，全0，无意义
        body[i++] = 0x00;
        body[i++] = 0x00;


        // NALU size
        body[i++] = dataLen >> 24 & 0xff;
        body[i++] = dataLen >> 16 & 0xff;
        body[i++] = dataLen >> 8 & 0xff;
        body[i++] = dataLen & 0xff;
        // NALU data
        memcpy(&body[i], data, dataLen);
    }

    int bRet = SendPacket(RTMP_PACKET_TYPE_VIDEO, body, (unsigned int)(i + dataLen), (unsigned int
    )timeStamp);

    free(body);

    return bRet;

}

/**
 * 关闭流
 */
JNIEXPORT void JNICALL Java_com_milimili_nativeex_JniUtil_close
        (JNIEnv *jniEnv, jclass jobj) {

}

//发送rtmp  packet
int SendPacket(unsigned int nPacketType, unsigned char *data, unsigned int size,
               unsigned int nTimestamp) {
    RTMPPacket *packet;
    if (nPacketType == RTMP_PACKET_TYPE_VIDEO) {
        //创建空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE + 数据大小，
        packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + size);
        memset(packet, 0, RTMP_HEAD_SIZE);
        //设置rtmppacket的数据指针
        packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
        packet->m_nBodySize = size;//设置rtmppacket的数据大小
        memcpy(packet->m_body, data, size);//拷贝数据到数据区
        packet->m_hasAbsTimestamp = 1;//
        packet->m_packetType = nPacketType; //包的类型：音频？ 视频？
        if (m_rtmp == NULL) {
            free(packet);
            return 0;
        }
        packet->m_nInfoField2 = m_rtmp->m_stream_id;//rtmp 的消息流id
        packet->m_nChannel = 0x04;  //rtmp 的块流id
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;//rtmp 块类型
        packet->m_nTimeStamp = nTimestamp;//rtmp 时间戳
    } else if (nPacketType == RTMP_PACKET_TYPE_AUDIO) {
        //创建空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE + 数据大小，
        packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + size);
        memset(packet, 0, RTMP_HEAD_SIZE);
        //设置rtmppacket的数据指针
        packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
        packet->m_nBodySize = size;//设置rtmppacket的数据大小
        memcpy(packet->m_body, data, size);//拷贝数据到数据区
        packet->m_hasAbsTimestamp = 1;//
        packet->m_packetType = nPacketType; //包的类型：音频？ 视频？
        if (m_rtmp == NULL) {
            free(packet);
            return 0;
        }
        packet->m_nInfoField2 = m_rtmp->m_stream_id;//rtmp 的消息流id
        packet->m_nChannel = 0x05;  //rtmp 的块流id
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;//rtmp 块类型
        packet->m_nTimeStamp = nTimestamp;//rtmp 时间戳
    } else {
        return 0;
    }
    //发送
    int nRet = 0;
    if (m_rtmp == NULL) {
        free(packet);
        return 0;
    }
    if (RTMP_IsConnected(m_rtmp)) {
        nRet = RTMP_SendPacket(m_rtmp, packet, TRUE);//把构造好的rtmppakage 发送出去
    }
    //清理空间
    free(packet);
    return nRet;
}




