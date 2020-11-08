
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
#include "packet/body_500.h"

static JSON_Value* jsonwrap_new_value_if525_resp_body(char* respCd, char* respMsg)
{
	JSON_Value *json_val = json_value_init_object();
    JSON_Object *json_obj = json_value_get_object(json_val);

	json_object_set_string(json_obj, EXTR_IF_BODY_COMMON_respCd, respCd);
	json_object_set_string(json_obj, EXTR_IF_BODY_COMMON_respMsg, respMsg);
	return json_val;
}

int im_if525_build_resp_body (IMPacketBodyPtr body, char* respCd, char* respMsg)
{
	body->root = jsonwrap_new_value_if525_resp_body(respCd, respMsg);
	return (body->root != NULL)?0:-1;
}

#if 0
control data - serialized_string:
{
    "mapHeaderExtension": {},
    "devCnvyDataVOs": [
        {
            "devId": "river4D1450836201263",
            "cnvyRowVOs": [
                {
                    "snsnDataInfoVOs": [
                        {
                            "dataTypeCd": "controll01",
                            "snsnVal": 6
                        }
                    ],
                    "sttusDataInfoVOs": [],
                    "contlDataInfoVOs": [],
                    "cmdDataInfoVOs": [],
                    "binDataInfoVOs": [],
                    "strDataInfoVOs": [],
                    "dtDataInfoVOs": [],
                    "genlSetupDataInfoVOs": [],
                    "sclgSetupDataInfoVOs": [],
                    "binSetupDataInfoVOs": [],
                    "mapRowExtension": {}
                }
            ]
        }
    ],
    "msgHeadVO": {
        "mapHeaderExtension": {}
    }
}
#endif

static JSON_Value* get_cnvyRowVOs_val(JSON_Value *rootval)
{
    JSON_Object *root_object = json_value_get_object(rootval);

	JSON_Array  *devCnvyDataVOs_arr = json_object_get_array(root_object, EXTR_IF_BODY_CTRL_devCnvyDataVOs);
	JSON_Value  *devCnvyDataVOs_val = json_array_get_value(devCnvyDataVOs_arr, 0);
    JSON_Object *devCnvyDataVOs_obj = json_value_get_object(devCnvyDataVOs_val);

	JSON_Array  *cnvyRowVOs_arr = json_object_get_array(devCnvyDataVOs_obj, EXTR_IF_BODY_CTRL_cnvyRowVOs);
	JSON_Value  *cnvyRowVOs_val = json_array_get_value(cnvyRowVOs_arr, 0);
	return cnvyRowVOs_val;
}

static JSON_Value* get_snsnDataInfoVOs_val_with_idx(JSON_Value *rootval, int idx)
{
	JSON_Value  *cnvyRowVOs_val = get_cnvyRowVOs_val(rootval);
    JSON_Object *cnvyRowVOs_obj = json_value_get_object(cnvyRowVOs_val);

	JSON_Array  *snsnDataInfoVOs_arr = json_object_get_array(cnvyRowVOs_obj, EXTR_IF_BODY_DATA_snsnDataInfoVOs);
	JSON_Value  *snsnDataInfoVOs_val = json_array_get_value(snsnDataInfoVOs_arr, idx);

	return snsnDataInfoVOs_val;
}
static JSON_Value* get_strDataInfoVOs_val_val_with_idx(JSON_Value *rootval, int idx)
{
	JSON_Value  *cnvyRowVOs_val = get_cnvyRowVOs_val(rootval);
    JSON_Object *cnvyRowVOs_obj = json_value_get_object(cnvyRowVOs_val);

	JSON_Array  *strDataInfoVOs_arr = json_object_get_array(cnvyRowVOs_obj, EXTR_IF_BODY_DATA_strDataInfoVO);
	JSON_Value  *strDataInfoVOs_val = json_array_get_value(strDataInfoVOs_arr, idx);

	return strDataInfoVOs_val;
}



int im_if525_body_get_numdata_with_index (IMPacketBodyPtr body, int idx, char **o_tagid, double *o_val)
{
    JSON_Object *j_obj;
	JSON_Value  *snsnDataInfoVOs_val = get_snsnDataInfoVOs_val_with_idx(body->root, idx);
	if ( snsnDataInfoVOs_val == NULL )	{
		return -1;
	}

    j_obj = json_value_get_object(snsnDataInfoVOs_val);
	*o_tagid = (char*)json_object_get_string(j_obj, EXTR_IF_BODY_DATA_dataTypeCd);
	*o_val = json_object_get_number(j_obj, EXTR_IF_BODY_DATA_snsnVal);

	return 0;
}
int im_if525_body_get_strdata_with_index (IMPacketBodyPtr body, int idx, char **o_tagid, char **o_val)
{
    JSON_Object *j_obj;
	JSON_Value  *strDataInfoVOs_val = get_strDataInfoVOs_val_val_with_idx(body->root, idx);
	if ( strDataInfoVOs_val == NULL )	{
		return -1;
	}

    j_obj = json_value_get_object(strDataInfoVOs_val);
	*o_tagid = (char*)json_object_get_string(j_obj, EXTR_IF_BODY_DATA_snsnTagCd);
	*o_val = (char*)json_object_get_string(j_obj, EXTR_IF_BODY_DATA_strVal);

	return 0;
}
