/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil */

#ifndef _Included_com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
#define _Included_com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
 * Method:    getStringFromNative
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_milimili_mediacodecex_rtmp_1push_1ex_RtmpUtil_getStringFromNative
  (JNIEnv *, jclass);

/*
 * Class:     com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
 * Method:    connect
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_milimili_mediacodecex_rtmp_1push_1ex_RtmpUtil_connect
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
 * Method:    sendVideoSpsPps
 * Signature: ([BI[BI)I
 */
JNIEXPORT jint JNICALL Java_com_milimili_mediacodecex_rtmp_1push_1ex_RtmpUtil_sendVideoSpsPps
  (JNIEnv *, jclass, jbyteArray, jint, jbyteArray, jint);

/*
 * Class:     com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
 * Method:    sendVideoData
 * Signature: ([BIZI)I
 */
JNIEXPORT jint JNICALL Java_com_milimili_mediacodecex_rtmp_1push_1ex_RtmpUtil_sendVideoData
  (JNIEnv *, jclass, jbyteArray, jint, jboolean, jint);

/*
 * Class:     com_milimili_mediacodecex_rtmp_push_ex_RtmpUtil
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_milimili_mediacodecex_rtmp_1push_1ex_RtmpUtil_close
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
