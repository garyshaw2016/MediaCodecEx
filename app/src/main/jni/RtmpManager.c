//
//  RtmpManager.c
//  RTMPDemo
//
//  Created by cxria on 15/11/17.
//  Copyright © 2015年 cxria. All rights reserved.
//

//#include "RtmpManager.h"



#include <stdio.h>
#include <stdlib.h>
#include "librtmp/rtmp.h"
#include "librtmp/amf.h"
#include <string.h>
#include <unistd.h>


//RTMP_MAX_HEADER_SIZE=18
#define RTMP_HEAD_SIZE   (sizeof(RTMPPacket)+RTMP_MAX_HEADER_SIZE)

#define BUFFER_SIZE 32768



typedef struct _RTMPMetadata
{
	// video, must be h264 type
	unsigned int    nWidth;
	unsigned int    nHeight;
	unsigned int    nFrameRate;
	unsigned int    nSpsLen;
	unsigned char   *Sps;
	unsigned int    nPpsLen;
	unsigned char   *Pps;
} RTMPMetadata,*LPRTMPMetadata;



RTMP* m_pRtmp;
RTMPMetadata metaData;
unsigned int startTime;
FILE *fp;
int setfp(FILE *p){
    fp = p;
    return 1;
}
//连接rtmp， url为推流地址
int RtmpManager_Connect(const char* url)
{
	m_pRtmp = RTMP_Alloc();//分配空间
	RTMP_Init(m_pRtmp);//初始化

    printf("====url is ==%s\n",url);
    //----设置url----
	if (RTMP_SetupURL(m_pRtmp,(char*)url) == FALSE)
	{
        printf("RTMP_SetupURL failed\n");
		RTMP_Free(m_pRtmp);
		return 0;
	}
	//----表示推流， 需在rtmpconnect之前调用-
	RTMP_EnableWrite(m_pRtmp);
	//连接

	if (RTMP_Connect(m_pRtmp, NULL) == FALSE)
	{
        printf("RTMP_Connect failed\n");
		RTMP_Free(m_pRtmp);
		return 0;
	}

	//连接

	if (RTMP_ConnectStream(m_pRtmp,0) == FALSE)
	{
        printf("RTMP_ConnectStream failed\n");
		RTMP_Close(m_pRtmp);
		RTMP_Free(m_pRtmp);
		return 0;
	}

    startTime = RTMP_GetTime();
    //连接成功
    printf("RTMP Connect success\n");
	return 1;
}


unsigned int RtmpManager_GetStartTime(){
    return startTime;
}

/**
 * 关闭流
 *
 */
void RtmpManager_Close()
{
	if(m_pRtmp)
	{   //清理rtmp
		RTMP_Close(m_pRtmp);
		RTMP_Free(m_pRtmp);
		m_pRtmp = NULL;
        //如果保存了sps, pps,释放他们
        if(metaData.Sps){
            free(metaData.Sps);
            metaData.Sps = NULL;
        }
        if(metaData.Pps){
            free(metaData.Pps);
            metaData.Pps = NULL;
        }
	}
}

//发送rtmp  packet
int SendPacket(unsigned int nPacketType,unsigned char *data,unsigned int size,unsigned int nTimestamp)
{
    RTMPPacket* packet;
    if (nPacketType == RTMP_PACKET_TYPE_VIDEO) {
        //创建空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE + 数据大小，
        packet = (RTMPPacket *)malloc(RTMP_HEAD_SIZE+size);
        memset(packet,0,RTMP_HEAD_SIZE);
        //设置rtmppacket的数据指针
        packet->m_body = (char *)packet + RTMP_HEAD_SIZE;
        packet->m_nBodySize = size;//设置rtmppacket的数据大小
        memcpy(packet->m_body, data, size);//拷贝数据到数据区
        packet->m_hasAbsTimestamp = 1;//
        packet->m_packetType = nPacketType; //包的类型：音频？ 视频？
        if(m_pRtmp == NULL){
            free(packet);
            return 0;
        }
        packet->m_nInfoField2 = m_pRtmp->m_stream_id;//rtmp 的消息流id
        packet->m_nChannel = 0x04;  //rtmp 的块流id
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;//rtmp 块类型
        packet->m_nTimeStamp = nTimestamp;//rtmp 时间戳
    }else if(nPacketType == RTMP_PACKET_TYPE_AUDIO){
        //创建空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE + 数据大小，
        packet = (RTMPPacket *)malloc(RTMP_HEAD_SIZE+size);
        memset(packet,0,RTMP_HEAD_SIZE);
        //设置rtmppacket的数据指针
        packet->m_body = (char *)packet + RTMP_HEAD_SIZE;
        packet->m_nBodySize = size;//设置rtmppacket的数据大小
        memcpy(packet->m_body, data, size);//拷贝数据到数据区
        packet->m_hasAbsTimestamp = 1;//
        packet->m_packetType = nPacketType; //包的类型：音频？ 视频？
        if(m_pRtmp == NULL){
            free(packet);
            return 0;
        }
        packet->m_nInfoField2 = m_pRtmp->m_stream_id;//rtmp 的消息流id
        packet->m_nChannel = 0x05;  //rtmp 的块流id
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;//rtmp 块类型
        packet->m_nTimeStamp = nTimestamp;//rtmp 时间戳
    }else{
        return 0;
    }
	//发送
	int nRet =0;
    if(m_pRtmp == NULL){
        free(packet);
        return 0;
    }
	if (RTMP_IsConnected(m_pRtmp))
	{
		nRet = RTMP_SendPacket(m_pRtmp,packet,TRUE);//把构造好的rtmppakage 发送出去
    }
	//清理空间
	free(packet);
	return nRet;
}

//在发送视频数据前，需要，先将sps,pps信息，发送出去，sps,pps获取可查看编码器的相关文档
int SendVideoSpsPps(unsigned char *pps,int pps_len,unsigned char * sps,int sps_len)
{
	RTMPPacket * packet=NULL;
	unsigned char * body=NULL;
	int i;
    //分配空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE + 1024，1024是随便给的，足够保存sps,pps的数据
	packet = (RTMPPacket *)malloc(RTMP_HEAD_SIZE+1024);
	memset(packet,0,RTMP_HEAD_SIZE+1024);
	packet->m_body = (char *)packet + RTMP_HEAD_SIZE;//设置rtmppacket数据区指针
	body = (unsigned char *)packet->m_body;//定义一个临时指针，准备为数据区赋值
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
	body[i++]   = 0xe1;// numofsequeueceParameterSets -- sps个数 = numofsequeueceParameterSets & 0x1F
    body[i++] = (sps_len >> 8) & 0xff;//此字节和下一个字节一起，表示sps长度
	body[i++] = sps_len & 0xff;
	memcpy(&body[i],sps,sps_len);//sps内容
	i +=  sps_len;

	/*pps*/
	body[i++]   = 0x01;//pps 个数
    body[i++] = (pps_len >> 8) & 0xff;//此字节和下一个字节一起，表示pps长度
	body[i++] = (pps_len) & 0xff;
	memcpy(&body[i],pps,pps_len);//pps内容
	i +=  pps_len;
    //到此，已构造好数据区的内容， i表示了数据区的长度， 接下来构造rtmppacket 结构体， 然后发送出去就行
	packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;//消息类型
	packet->m_nBodySize = i;//数据区长度 （m_body的长度）
	packet->m_nChannel = 0x04;//块流id
	packet->m_nTimeStamp = 0;//时间戳
	packet->m_hasAbsTimestamp = 1;//时间戳是相对时间，还是绝对时间
	packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;//块类型
	packet->m_nInfoField2 = m_pRtmp->m_stream_id;//消息流id
    if(m_pRtmp == NULL){//使用前，检查是否为空
        free(packet);
        return 0;
    }
	//发送
	int nRet = RTMP_SendPacket(m_pRtmp,packet,TRUE);
	free(packet);
	return nRet;
}

//发送视频数据包
int RtmpManager_SendVideoPacket(unsigned char *data,unsigned int size,int bIsKeyFrame,unsigned int nTimeStamp)
{
	if(data == NULL ){//&& size<11
		return 0;
	}

	unsigned char *body = (unsigned char*)malloc(size+9);
	memset(body,0,size+9);

	int i = 0;
	if(bIsKeyFrame){
		body[i++] = 0x17;// 1:Iframe  7:AVC
		body[i++] = 0x01;// AVC NALU
		body[i++] = 0x00;//avc时，全0，无意义
		body[i++] = 0x00;
		body[i++] = 0x00;


		// NALU size
		body[i++] = size>>24 &0xff;
		body[i++] = size>>16 &0xff;
		body[i++] = size>>8 &0xff;
		body[i++] = size&0xff;
		// NALU data
		memcpy(&body[i],data,size);
		//SendVideoSpsPps(metaData.Pps,metaData.nPpsLen,metaData.Sps,metaData.nSpsLen);
	}else{
		body[i++] = 0x27;// 2:Pframe  7:AVC
		body[i++] = 0x01;// AVC NALU
		body[i++] = 0x00;//avc时，全0，无意义
		body[i++] = 0x00;
		body[i++] = 0x00;


		// NALU size
		body[i++] = size>>24 &0xff;
		body[i++] = size>>16 &0xff;
		body[i++] = size>>8 &0xff;
		body[i++] = size&0xff;
		// NALU data
		memcpy(&body[i],data,size);
	}


	int bRet = SendPacket(RTMP_PACKET_TYPE_VIDEO, body, i+size, nTimeStamp);

	free(body);

	return bRet;
   // return 1;
}

int RtmpManager_SetSpsPps(unsigned char *sps,unsigned int sps_len,unsigned char * pps,unsigned int pps_len){
        metaData.nSpsLen = sps_len;
        if(metaData.Sps != NULL){
            free(metaData.Sps);
        }
    	metaData.Sps=NULL;
    	metaData.Sps=(unsigned char*)malloc(sps_len);
        if(!metaData.Sps){
            return 0;
        }
    	memcpy(metaData.Sps, sps, sps_len);

    	metaData.nPpsLen = pps_len;
        if(metaData.Pps != NULL){
            free(metaData.Sps);
        }
    	metaData.Pps=NULL;
    	metaData.Pps=(unsigned char*)malloc(pps_len);
        if(!metaData.Pps){
            return 0;
        }
    	memcpy(metaData.Pps, pps, pps_len);
        return  1;
}


int RtmpManager_SendAudioPacket(unsigned char *data,unsigned int size,unsigned int nTimeStamp){
    //由于本项目中的faacmanager中已在数据部分前加入了0xaf 0x00两个字节，因此此处不再构造数据区数据
    int ret = SendPacket(RTMP_PACKET_TYPE_AUDIO, data, size, nTimeStamp);
    //fwrite(data, size, 1, fp);

    return ret;
}

int RtmpManager_SendAudioSpecInfo(unsigned char *data,unsigned int size){
    RTMPPacket * packet=NULL;
    //分配空间， 大小是rtmppacket结构体大小 + rtmp_MAX_HEAD_SIZE + 1024，1024是随便给的，足够保存sps,pps的数据
     packet = (RTMPPacket *)malloc(RTMP_HEAD_SIZE+1024);
     memset(packet,0,RTMP_HEAD_SIZE+1024);
     packet->m_body = (char *)packet + RTMP_HEAD_SIZE;//设置rtmppacket数据区指针
     memcpy((char *)packet + RTMP_HEAD_SIZE, data, size);//为数据区赋值

    //到此，已构造好数据区的内容， i表示了数据区的长度， 接下来构造rtmppacket 结构体， 然后发送出去就行
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;//消息类型
    packet->m_nBodySize = size;//数据区长度 （m_body的长度）
    packet->m_nChannel = 0x05;//块流id
    packet->m_nTimeStamp = 0;//时间戳
    packet->m_hasAbsTimestamp = 1;//时间戳是相对时间，还是绝对时间
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;//块类型
    if(m_pRtmp == NULL){//使用前，检查是否为空
        free(packet);
        return 0;
    }
    packet->m_nInfoField2 = m_pRtmp->m_stream_id;//消息流id

    //发送
    if(m_pRtmp == NULL){//使用前，检查是否为空
        free(packet);
        return 0;
    }
    int nRet = RTMP_SendPacket(m_pRtmp,packet,TRUE);
    free(packet);
    return nRet;
}



