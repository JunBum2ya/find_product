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

#include "packet/body_400.h"

#if 0

{
    "extrSysId": "extrSysId",
    "devColecDataVOs": [
        {
            "m2mSvcNo": 0,
            "deviceId": "devId",
            "colecRowVOs": [
                {
                    "snsnDataInfoVOs": [
                        {
                            "dataTypeCd": "aaa",
                            "snsnVal": 100
                        }
                    ]
                }
            ]
        }
    ]
}

------------------------------------------------------

{
    "extrSysId": "extrSysId",
    "devColecDataVOs": [
        {
            "m2mSvcNo": 0,
            "deviceId": "devId",
            "colecRowVOs": [
                {
					"occDt": "2014-02-28 17:59:17.517",
                    "snsnDataInfoVOs": [
                        {
                            "dataTypeCd": "aaa",
                            "snsnVal": 100
                        },
                        {
                            "dataTypeCd": "bbb",
                            "snsnVal": 100
                        },
                        {
                            "dataTypeCd": "ccc",
                            "snsnVal": 100
                        }
                    ],
                    "strDataInfoVOs": [
                        {
                            "snsnTagCd": "aaa",
                            "strVal": "xxx"
                        },
                        {
                            "snsnTagCd": "bbb",
                            "strVal": "yyy"
                        },
                        {
                            "snsnTagCd": "ccc",
                            "strVal": "zzz"
                        }
                    ]
                }
            ]
        }
    ]
}

#endif

static JSON_Value* jsonwrap_new_value_if411(char *extrSysId)
{
	JSON_Value *json_val = json_value_init_object();
    JSON_Object *json_obj = json_value_get_object(json_val);

	json_object_set_string(json_obj, EXTR_IF_BODY_COMMON_extrSysId, extrSysId);
	json_object_set_value(json_obj, EXTR_IF_BODY_DATA_devColecDataVOs, json_value_init_array());
	return json_val;
}
static JSON_Status jsonwrap_if411_append_value_devColecDataVOs_item(JSON_Value *json_pval, JSON_Value *json_cval)
{
	return json_array_append_value(
		json_object_get_array(json_value_get_object(json_pval),EXTR_IF_BODY_DATA_devColecDataVOs), 
		json_cval);
}

static JSON_Value* jsonwrap_new_value_devColecDataVOs_item(int m2mSvcNo, char *devId)
{
	JSON_Value *json_val = json_value_init_object();
    JSON_Object *json_obj = json_value_get_object(json_val);
	
	json_object_set_number(json_obj, EXTR_IF_BODY_DATA_m2mSvcNo, m2mSvcNo);
	json_object_set_string(json_obj, EXTR_IF_BODY_COMMON_deviceId, devId);
	json_object_set_value(json_obj, EXTR_IF_BODY_DATA_colecRowVOs, json_value_init_array());
	return json_val;
}

static JSON_Status jsonwrap_devColecDataVOs_append_colecRowVOs_item(JSON_Value *json_pval, JSON_Value *json_cval)
{
	return json_array_append_value(
		json_object_get_array(json_value_get_object(json_pval),EXTR_IF_BODY_DATA_colecRowVOs), 
		json_cval);
}


// 0 - number type
static JSON_Status jsonwrap_colecRowVOs_append_number_data_item(JSON_Value *json_pval, JSON_Value *json_cval)
{
	return json_array_append_value(
		json_object_get_array(json_value_get_object(json_pval),EXTR_IF_BODY_DATA_snsnDataInfoVOs), 
		json_cval);
}

// 1 - string type
static JSON_Status jsonwrap_colecRowVOs_append_string_data_item(JSON_Value *json_pval, JSON_Value *json_cval)
{
	return json_array_append_value(
		json_object_get_array(json_value_get_object(json_pval),EXTR_IF_BODY_DATA_strDataInfoVO), 
		json_cval);
}


static JSON_Value* jsonwrap_new_value_snsnDataInfoVOs_item_num(char *dataTypeCd, double snsnVal)
{
	JSON_Value *json_val = json_value_init_object();
    JSON_Object *json_obj = json_value_get_object(json_val);
	
	json_object_set_string(json_obj, EXTR_IF_BODY_DATA_dataTypeCd, dataTypeCd);
	json_object_set_number(json_obj, EXTR_IF_BODY_DATA_snsnVal, snsnVal);
	return json_val;
}
static JSON_Value* jsonwrap_new_value_strDataInfoVO_item_string(char *dataTypeCd, char* snsnVal)
{
	JSON_Value *json_val = json_value_init_object();
    JSON_Object *json_obj = json_value_get_object(json_val);
	
	json_object_set_string(json_obj, EXTR_IF_BODY_DATA_snsnTagCd, dataTypeCd);
	json_object_set_string(json_obj, EXTR_IF_BODY_DATA_strVal, snsnVal);
	return json_val;
}


static JSON_Value* jsonwrap_if411_get_colecRowVOs_item(JSON_Value *json_val)
{
	JSON_Array  *devColecDataVOs = json_object_get_array(json_value_get_object(json_val), EXTR_IF_BODY_DATA_devColecDataVOs);
	JSON_Value *devColecDataVOs_0 = json_array_get_value(devColecDataVOs, 0);

	JSON_Array  *colecRowVOs = json_object_dotget_array(json_value_get_object(devColecDataVOs_0), EXTR_IF_BODY_DATA_colecRowVOs);
	JSON_Value *colecRowVOs_0 = json_array_get_value(colecRowVOs, 0);
	
	return colecRowVOs_0;
}

/*
obj: set/get
arr: append/get_idx
*/

static int im_if411_init_body(IMPacketBodyPtr body, char* extrSysId, char* devId)
{
	char timebuff[64];

	body->root = jsonwrap_new_value_if411(extrSysId);
	JSON_Value *jsonval_devColecDataVOs_item = jsonwrap_new_value_devColecDataVOs_item(0, devId);
	JSON_Value *jsonval_colecRowVOs_item = json_value_init_object();

	im_util_strftime_now(timebuff, "%Y-%m-%d %H:%M:%S.");
	sprintf(timebuff+strlen(timebuff), "%d", im_util_gettime_now_msec());
	json_object_set_string(json_value_get_object(jsonval_colecRowVOs_item),
		EXTR_IF_BODY_DATA_occDt, timebuff);


	json_object_set_value(json_value_get_object(jsonval_colecRowVOs_item),
		EXTR_IF_BODY_DATA_snsnDataInfoVOs, json_value_init_array());
	
	json_object_set_value(json_value_get_object(jsonval_colecRowVOs_item),
		EXTR_IF_BODY_DATA_strDataInfoVO, json_value_init_array());

	jsonwrap_devColecDataVOs_append_colecRowVOs_item(jsonval_devColecDataVOs_item, jsonval_colecRowVOs_item);
	jsonwrap_if411_append_value_devColecDataVOs_item(body->root, jsonval_devColecDataVOs_item);

	return 0;
}

/* =====================================
if411 - simple
====================================== */
int im_if411_build_numdata_body (IMPacketBodyPtr body, char* extrSysId, char* devId, char* dataTypeCd, double snsnVal)
{
	int rc = 0;
	im_if411_init_body(body, extrSysId, devId);

	JSON_Value *jsonval_colecRowVOs_item_0 = jsonwrap_if411_get_colecRowVOs_item(body->root);
	if ( jsonval_colecRowVOs_item_0 == NULL )	{
		return -1;
	}

	JSON_Value *jsonval_number_data_item = jsonwrap_new_value_snsnDataInfoVOs_item_num(dataTypeCd, snsnVal);
	jsonwrap_colecRowVOs_append_number_data_item(jsonval_colecRowVOs_item_0, jsonval_number_data_item);
	return 0;
}

int im_if411_build_strdata_body (IMPacketBodyPtr body, char* extrSysId, char* devId, char* dataTypeCd, char* snsnVal)
{
	int rc = 0;
	im_if411_init_body(body, extrSysId, devId);

	JSON_Value *jsonval_colecRowVOs_item_0 = jsonwrap_if411_get_colecRowVOs_item(body->root);
	if ( jsonval_colecRowVOs_item_0 == NULL )	{
		return -1;
	}

	JSON_Value *jsonval_string_data_item = jsonwrap_new_value_strDataInfoVO_item_string(dataTypeCd, snsnVal);
	jsonwrap_colecRowVOs_append_string_data_item(jsonval_colecRowVOs_item_0, jsonval_string_data_item);
	return rc;
}



/* =====================================
if411 - complex
====================================== */
int im_if411_complexdata_init (IMPacketBodyPtr body, char* extrSysId, char* devId)
{
	int rc = 0;
	im_pktBody_init(body);

	im_if411_init_body(body, extrSysId, devId);
	return rc;
}
void im_if411_complexdata_release(IMPacketBodyPtr body)
{
	im_pktBody_release(body);
}

int im_if411_complexdata_add_numdata(IMPacketBodyPtr body, char* dataTypeCd, double snsnVal)
{
	int rc = 0;

	if ( body->root == NULL ){
		return -1;
	}

	JSON_Value *jsonval_colecRowVOs_item_0 = jsonwrap_if411_get_colecRowVOs_item(body->root);
	if ( jsonval_colecRowVOs_item_0 == NULL )	{
		return -1;
	}

	JSON_Value *jsonval_number_data_item = jsonwrap_new_value_snsnDataInfoVOs_item_num(dataTypeCd, snsnVal);
	jsonwrap_colecRowVOs_append_number_data_item(jsonval_colecRowVOs_item_0, jsonval_number_data_item);
	
	return rc;
}

int im_if411_complexdata_add_strdata(IMPacketBodyPtr body, char* dataTypeCd, char* snsnVal)
{
	int rc = 0;

	if ( body->root == NULL ){
		return -1;
	}

	JSON_Value *jsonval_colecRowVOs_item_0 = jsonwrap_if411_get_colecRowVOs_item(body->root);
	if ( jsonval_colecRowVOs_item_0 == NULL )	{
		return -1;
	}

	JSON_Value *jsonval_string_data_item = jsonwrap_new_value_strDataInfoVO_item_string(dataTypeCd, snsnVal);
	jsonwrap_colecRowVOs_append_string_data_item(jsonval_colecRowVOs_item_0, jsonval_string_data_item);
	
	return rc;
}
