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

#include <string.h>

#include "base/base.h"
#include "util/log.h"
#include "packet/packet.h"
#include "action/action.h"
#include "packet/body_500.h"


/* =====================================
EXTR-IF-525
====================================== */
static void im_action_control_request_body_handler(IMPacketBodyPtr body)
{
	char *tagid;
	char *strval;
	double numval;
	
	int idx = 0;
	int rc;

	IMCbTagidNumDataHndl numdata_handler = (IMCbTagidNumDataHndl)im_base_get_cb_numdata_handler();
	IMCbTagidStrDataHndl strdata_handler = (IMCbTagidStrDataHndl)im_base_get_cb_strdata_handler();

	if ( numdata_handler != NULL )	{
		for (idx=0; idx<IOTMAKERS_MAX_CONTROL_DATA; idx++)	{
			rc = im_if525_body_get_numdata_with_index(body, idx, &tagid, &numval);
			if ( rc < 0 )	{
				break;
			}
			numdata_handler(tagid, numval);	
		}
	} else {
		INFO_LOG("No numdata_handler set");
	}

	if (strdata_handler != NULL) {
		for (idx=0; idx<IOTMAKERS_MAX_CONTROL_DATA; idx++)		{
			rc = im_if525_body_get_strdata_with_index(body, idx, &tagid, &strval);
			if ( rc < 0 )	{
				break;
			}
			strdata_handler(tagid, strval);	
		}
	} else {
		INFO_LOG("No strdata_handler set");
	}
}


int im_action_control_send_rsp(IMPacketPtr pktRqst, char *respCd, char *respMsg)
{
	int rc = 0;
	IMPacket pkt;
	IMPacketPtr ppkt = &pkt;
	IMPacketHeadPtr phead = im_packet_get_head(ppkt);
	IMPacketBodyPtr pbody = im_packet_get_body(ppkt);

	im_packet_init_for_response(ppkt);
	im_packet_set_head_methodType_as_CONTL_ITGCNVY_DATA(ppkt);

	im_pktHead_copy_trmTransacId(&(pkt.head), &(pktRqst->head));

	im_if525_build_resp_body(pbody, respCd, respMsg);	
	
	rc = im_action_send_packet(ppkt);

	im_packet_release(ppkt);
	return rc;
}

int im_action_control_req_handler(IMPacketPtr reqPkt)
{
	IMPacketHeadPtr head = im_packet_get_head(reqPkt);
	IMPacketBodyPtr body = im_packet_get_body(reqPkt);

	im_action_control_send_rsp(reqPkt, "100", "SUCCESS");

	im_action_control_request_body_handler(body);

	return 0;
}




