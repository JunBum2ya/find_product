
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
#include "packet/body_200.h"

/*
 * ********************************************************************************
 * [1. MSG] DevCommChAhtnRqtVO를 생성하기 위한 함수
 * ********************************************************************************
 */

static JSON_Value* jsonwrap_new_value_if224(char* extrSysId, char* devId, char* athnRqtNo)
{
	JSON_Value *json_val = json_value_init_object();
    JSON_Object *json_obj = json_value_get_object(json_val);

	json_object_set_string(json_obj, EXTR_IF_BODY_COMMON_extrSysId, extrSysId);
	json_object_set_string(json_obj, EXTR_IF_BODY_COMMON_deviceId, devId);
	json_object_set_string(json_obj, EXTR_IF_BODY_ATHN_athnRqtNo, athnRqtNo);
	return json_val;
}

// ATHN_COMMCHATHN_DEV_TCP
int im_if224_build_body (IMPacketBodyPtr body, char* extrSysId, char* devId, char* athnRqtNo)
{
	body->root = jsonwrap_new_value_if224(extrSysId, devId, athnRqtNo);

	return (body->root != NULL)?0:-1;
}
