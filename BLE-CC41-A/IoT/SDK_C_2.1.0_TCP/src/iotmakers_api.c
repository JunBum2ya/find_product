/*
 * GiGA IoTMakers version 1.0
 *
 *  Copyright (c) 2016 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file except in
 *  compliance with license agreement with kt corp. Any redistribution or use of this
 *  software, with or without modification shall be strictly prohibited without prior written
 *  approval of kt corp, and the copyright notice above does not evidence any actual or
 *  intended publication of such software.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <sys/time.h>
#include <pthread.h>
#include <unistd.h>

#include "im_common.h"
#include "base/base.h"
#include "util/log.h"
#include "util/util.h"
#include "packet/packet.h"
#include "packet/body_400.h"
#include "action/action.h"

#if  defined (_IM_C_SOCK_) 
#include "netio/sock.h"
#endif

static	IMPacketBody	g_data_body;
static	pthread_t		g_thread_id;

void im_error_handler(int errcode)
{
	IMCbErrorHndl cb_error_handler = im_base_get_cb_error_hndler();
	if ( cb_error_handler != NULL )	{
		cb_error_handler(errcode);
	}
}

/*
 * im_init
 */
int im_init(char *ip, int port, char *deviceId, char *athnRqtNo, char *extrSysId, char *log_file_name)
{
	int rc = 0;
	
	rc = im_base_init(ip, port, extrSysId, deviceId, athnRqtNo, log_file_name);
	if ( rc < 0 )	{
		return -1;
	}

	if ( im_log_init((char*)im_base_get_log_file_name()) < 0 ) {
		printf("fail im_log_init(); plz check the path[%s]\n", (char*)im_base_get_log_file_name());
		return 0;
	}

	im_log_set_level(LOG_LEVEL_ERROR);
	g_thread_id = 0;
	
	return 0;
}

int im_init_with_config_file(char *fname)
{
	char			ip[IOTMAKERS_STR_64_BYTE_LEN + 1];
	unsigned short	port;
	char			extrSysId[IOTMAKERS_STR_30_BYTE_LEN + 1];
	char			deviceId[IOTMAKERS_STR_64_BYTE_LEN + 1];
	char			athnRqtNo[IOTMAKERS_STR_64_BYTE_LEN + 1];
	char			log_file_name[IOTMAKERS_STR_128_BYTE_LEN + 1];

	int rc = im_util_read_config(fname,
			ip, &port,
			extrSysId,
			deviceId,
			athnRqtNo, log_file_name);

	printf("ip: %s\n", ip);
	printf("port: %d\n", port);
	printf("extrSysId: %s\n", extrSysId);
	printf("deviceId: %s\n", deviceId);
	printf("athnRqtNo: %s\n", athnRqtNo);
	printf("log_file_name: %s\n", log_file_name);

	if (rc < 0)	{
		return -1;
	}

	return im_init(ip, port, deviceId, athnRqtNo, extrSysId, log_file_name);
}

void im_release() 
{
	im_base_release();
	im_log_close();
	return;
}

int im_get_LastErrCode()
{
	return im_base_get_lastErrorCode();
}

void im_set_loglevel(int loglevel)
{
	if ( loglevel <= LOG_LEVEL_ERROR )	{
		im_log_set_level(LOG_LEVEL_ERROR);
	} else if ( loglevel >= LOG_LEVEL_DEBUG )	{
		im_log_set_level(LOG_LEVEL_DEBUG);
	} else {
		im_log_set_level(loglevel);
	}
}

#if  defined (_IM_C_SOCK_) 
void im_set_socktimeout_sec(int sec)
{
	im_base_set_sockTimeoutSec(sec);
}
#endif


/*
 * im_settings callback - user data
 */
void im_set_numdata_handler(IMCbTagidNumDataHndl cb_proc)
{
	im_base_set_cb_numdata_handler(cb_proc);
}
void im_set_strdata_handler(IMCbTagidStrDataHndl cb_proc)
{
	im_base_set_cb_strdata_handler(cb_proc);
}
void im_set_dataresp_handler(IMCbDataRespHndl cb_proc)
{
	im_base_set_cb_dataresp_handler(cb_proc);
}
void im_set_error_handler(IMCbErrorHndl cb_proc)
{
	im_base_set_cb_error_hndler(cb_proc);
}


/*
 * im_connect
 */
void im_disconnect()
{
#if  defined (_IM_C_MQTT_) 
	im_mqtt_disconnect(im_base_get_mqttc());
#elif defined (_IM_C_SOCK_) 
	im_sock_disconnect(im_base_get_sock());
	im_base_set_sock(-1);
#endif
}


static void __imcb_recv_data_handler(char *data, int data_len)
{
	im_action_recv_data_handler(data, data_len);
}

int im_connect()
{
	int conn_timeout_sec = (3);

#if  defined (_IM_C_MQTT_) 
	int rc;
	rc = im_mqtt_connect_timeout(im_base_get_mqttc(), im_base_get_ip(), im_base_get_port(), 
		im_base_get_deviceId(), conn_timeout_sec);
	if ( rc < 0 )	{
		ERROR_LOG("fail im_mqtt_connect()");
		im_base_set_lastErrorCode(IM_ErrCode_SOCK_CONNECTION_FAIL);
		im_error_handler(IM_ErrCode_SOCK_CONNECTION_FAIL);
		return -1;
	}
	im_mqtt_set_cb_recv_handler(__imcb_recv_data_handler);

#elif defined (_IM_C_SOCK_) 
	int sock = -1;

	if ( im_base_get_sock() > 0 )	{
		im_disconnect();
	}

	sock = im_sock_connect_timeout(im_base_get_ip(), im_base_get_port(), conn_timeout_sec);
	if ( sock <= 0 )	{
		im_base_set_lastErrorCode(IM_ErrCode_SOCK_CONNECTION_FAIL);
		im_error_handler(IM_ErrCode_SOCK_CONNECTION_FAIL);
		return -1;
	}
	im_base_set_sock(sock);
	im_sock_set_timeout(sock, im_base_get_sockTimeoutSec());
	im_sock_set_cb_recv_handler((void*)__imcb_recv_data_handler);
#endif
	return 0;
}



/*
 * im_auth_device
 */
int im_auth_device()
{
	im_base_set_lastErrorCode(IM_ErrCode_SUCCESS);
	
	int rc = im_action_authDevChannel_send_req();
	if ( rc < 0 )	{
		return -1;
	}

	int authWaitSec = 3;
	while ( im_base_is_ChAthnSuccess() != (1) )	{
		if ( authWaitSec-- <= 0 )	{
			ERROR_LOG("fail im_base_is_ChAthnSuccess()");
			return -1;
		}
		INFO_LOG("wating the device authenticated.");
		sleep(1);
	}
	INFO_LOG("device authenticated.");

	return 0;
}


/*
 * im_send_keepalive
 */
#if defined (_IM_C_SOCK_) 
int im_send_keepalive()
{
	int pktCountRecvPrev = im_base_get_pktCountRecv();
	int pktCountRecvNow = pktCountRecvPrev;

	int rc = im_action_authKeepalive_send_req();
	if ( rc < 0 )	{
		im_error_handler(IM_ErrCode_SOCK_SEND_FAIL);
		return -1;
	}

	pktCountRecvNow = im_base_get_pktCountRecv();
	if ( pktCountRecvPrev == pktCountRecvNow )	{
		sleep(1);
		pktCountRecvNow = im_base_get_pktCountRecv();
	}

	if ( pktCountRecvPrev == pktCountRecvNow ) 	{
		sleep(1);
		pktCountRecvNow = im_base_get_pktCountRecv();
	}

	if ( pktCountRecvPrev == pktCountRecvNow )	{
		im_error_handler(IM_ErrCode_SOCK_SEND_FAIL);
	}

	return 0;
}

static void __timer_handler(int arg)
{
	im_send_keepalive();
}

int im_settimer_keepalived() 
{
#ifdef _LINUX_
	struct itimerval itv;
	itv.it_interval.tv_sec = im_base_get_keepAliveExpireTimeSec();
	itv.it_interval.tv_usec = 0;

	itv.it_value.tv_sec = im_base_get_keepAliveExpireTimeSec();
	itv.it_value.tv_usec = 0;

	setitimer(ITIMER_REAL, &itv, (struct itimerval *) 0);

	if ( sigset (SIGALRM, __timer_handler) < 0 )	{
		return -1;
	}

	INFO_LOG("The KeepAlived timer has been registered.");
#endif
	return 0;
}

void im_unsettimer_keepalived()
{
#ifdef _LINUX_
	signal(SIGALRM, SIG_IGN);
	INFO_LOG("The KeepAlived timer has been unregistered.");
#endif
}

int im_recv_control_packet(int timeout_sec)
{
	int rc;
	
	if ( im_base_is_ServiceMode() == (1) )	{
		INFO_LOG("Not allowed in ServiceMode.");
		return -1;
	}

	if ( timeout_sec < 0 ){
		timeout_sec = 0;
	}

	im_sock_set_timeout(im_base_get_sock(), timeout_sec);

	while ( (rc = im_action_recv_packet()) == 0 )	{
		;
	}
	// no data to recv

	return rc;
}

#endif




/*
 * loop 
 */
static void *__th_recv_loop(void *arg)
{
	int rc = 0;
	unsigned long pktCountRecv = 0;
	unsigned long pktCountSent = 0;

#if defined (_IM_C_SOCK_)
	int sock = im_base_get_sock();
	// sock in blocking_mode
	im_sock_set_read_timeout(sock, 1);
#endif

	im_base_set_recvLoopStart();
	im_base_set_ServiceModeStart();

	while ( im_base_get_isRecvLoopStop() != (1) )
	{
#if defined (_IM_C_SOCK_)
		sock = im_base_get_sock();
		rc = im_sock_recv_data(sock);
		if ( rc < 0 )	{
			IM_ErrCode errCode =  (IM_ErrCode)im_base_get_lastErrorCode();
			if ( errCode != IM_ErrCode_SUCCESS )	{
				if (  errCode == IM_ErrCode_SOCK_CONNECTION_LOSS )	{
					INFO_LOG("about to im_sock_disconnect(), sock=[%d]", sock);
					im_sock_disconnect(sock);
					im_base_set_sock(-1);
					break;
				}
				im_error_handler(errCode);
			}
		}
#elif defined (_IM_C_MQTT_) 
		// avoid busy_wait
		sleep(1);
#endif
		pktCountRecv = im_base_get_pktCountRecv();
		pktCountSent = im_base_get_pktCountSent();
		DEBUG_LOG("thd[%d], rc=[%d]; sent=[%ld]pkts, recv=[%ld]pkts", g_thread_id, rc, pktCountSent, pktCountRecv);
	}

	im_base_set_ServiceModeStop();
	INFO_LOG("done recv_loop.");
}

static int __im_start_recv_loop_thread()
{
	int rc;
    int thr_id;

    thr_id = pthread_create(&g_thread_id, NULL, __th_recv_loop, (void *)NULL);
    if (thr_id < 0)   {
        ERROR_LOG("fail pthread_create()");
		return -1;
    }

	pthread_detach(g_thread_id);	

	return 0;
}

static void __im_stop_recv_loop()
{
    int status;

	im_base_set_recvLoopStop();
	im_base_set_ServiceModeStop();

	if ( g_thread_id != 0 ){
        INFO_LOG("waiting for thread_recv_loop[%d] to be done", g_thread_id);
	    pthread_join(g_thread_id, (void **)&status);
		g_thread_id = 0;
        INFO_LOG("thread_recv_loop done");
	}
}

int im_start_service()
{
	int rc;

	g_thread_id = 0;

	// 1. connect
	rc = im_connect();
	if ( rc < 0  )	{
        ERROR_LOG("fail im_connect()");
		return -1;
	}

#if  defined (_IM_C_MQTT_) 
	im_base_set_mqttTopic4Sub(im_base_get_deviceId());
	im_base_set_mqttTopic4Pub(im_base_get_deviceId());
	rc = im_mqtt_subscribe_topic(im_base_get_mqttc(), im_base_get_mqttTopic4Sub());
	if ( rc < 0)	{
		ERROR_LOG("fail im_mqtt_subscribe_topic(), topic=[%d]", im_base_get_mqttTopic4Sub());
		goto exit_on_error;
	}
#endif
 
	// 2. do looper
	im_base_set_ServiceModeStart();
	rc = __im_start_recv_loop_thread();
	if ( rc < 0 )	{
        ERROR_LOG("fail __im_start_recv_loop_thread()");
		goto exit_on_error;
	}

	// 3. do im_auth_device
	rc = im_auth_device();
	if ( rc < 0 )	{
        ERROR_LOG("fail im_auth_device()");
		goto exit_on_error;
	}

#if  defined (_IM_C_MQTT_) 
	im_mqtt_unsubscribe_topic(im_base_get_mqttc(), im_base_get_mqttTopic4Sub());
	im_base_set_mqttTopic4Sub(im_base_get_commChAthnNo());
	im_base_set_mqttTopic4Pub(im_base_get_commChAthnNo());
	rc = im_mqtt_subscribe_topic(im_base_get_mqttc(), im_base_get_mqttTopic4Sub());
	if ( rc < 0)	{
		ERROR_LOG("fail im_mqtt_subscribe_topic(), topic=[%d]", im_base_get_commChAthnNo());
		goto exit_on_error;
	}
#endif	
	return 0;

exit_on_error:
	__im_stop_recv_loop();	
	im_disconnect();
	return -1;
}

void im_stop_service()
{
#if defined (_IM_C_SOCK_) 
	im_unsettimer_keepalived();
#endif
	__im_stop_recv_loop();
	im_disconnect();
}



/*
 * im_send_data
 */
long long im_get_new_trxid()
{
	return (long long)im_util_get_unique_number();
}

static int im_send_numdata_with_trxid(char *tagid, double val, long long trxid)
{
	im_base_set_lastErrorCode(IM_ErrCode_SUCCESS);

	int rc = im_action_collectionNumData_send_req_with_trxid(tagid, val, trxid);
	if ( rc < 0 )	{
		im_error_handler(im_base_get_lastErrorCode());
		return -1;
	}

	return 0;
}

static int im_send_strdata_with_trxid(char *tagid, char *val, long long trxid)
{
	im_base_set_lastErrorCode(IM_ErrCode_SUCCESS);

	int rc = im_action_collectionStrData_send_req_with_trxid(tagid, val, trxid);
	if ( rc < 0 )	{
		im_error_handler(im_base_get_lastErrorCode());
		return -1;
	}

	return 0;
}


/*
 * im_send_complexdata
 */
int im_init_complexdata()
{
	im_pktBody_release(&g_data_body);

	im_if411_complexdata_init(&g_data_body, im_base_get_extrSysId(), im_base_get_deviceId());
	return 0;
}
int im_add_numdata_to_complexdata(char *tagid, double val)
{
	im_if411_complexdata_add_numdata(&g_data_body, tagid, val);
	return 0;
}
int im_add_strdata_to_complexdata(char *tagid, char *val)
{
	im_if411_complexdata_add_strdata(&g_data_body, tagid, val);
	return 0;
}
static int im_send_complexdata_with_trxid(long long trxid)
{
	im_base_set_lastErrorCode(IM_ErrCode_SUCCESS);

	int rc = im_action_collectionComplexData_send_req_with_trxid(&g_data_body, trxid);
	im_if411_complexdata_release(&g_data_body);

	if ( rc < 0 )	{
		im_error_handler(im_base_get_lastErrorCode());
		return -1;
	}
	return 0;
}
int im_send_complexdata(long long trxid)
{
	return im_send_complexdata_with_trxid(trxid);
}


int im_send_numdata(char *tagid, double val, long long trxid)
{
	return im_send_numdata_with_trxid(tagid, val, trxid);
}
int im_send_strdata(char *tagid, char *val, long long trxid)
{
	return im_send_strdata_with_trxid(tagid, val, trxid);
}

