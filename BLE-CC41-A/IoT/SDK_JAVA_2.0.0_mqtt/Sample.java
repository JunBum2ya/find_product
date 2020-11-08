import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.kt.smcp.gw.ca.comm.exception.SdkException;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.mqtt.BaseInfo;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.mqtt.IMMqttConnector;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.mqtt.LogIf;
import com.kt.smcp.gw.ca.util.IMUtil;

public class Sample {
	public static void main(String[] args) {
		Integer timeout = 3000;
		LogIf logIf = new LogIf();
		IMMqttConnector imMqttConnector = new IMMqttConnector();
		BaseInfo baseInfo = null;
		Long transID;
		try {
			// 초기화
			baseInfo = IMUtil.getBaseInfo("IoTSDK.properties");
			imMqttConnector.init(logIf, baseInfo);
			// 연결
			imMqttConnector.connect(timeout);
			// 인증
			imMqttConnector.authenticate((long)timeout);
			// 숫자형 단건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			imMqttConnector.requestNumColecData("temperature", 22.1, new Date(), transID);
			// 문자형 단건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			imMqttConnector.requestStrColecData("LED", "ON", new Date(), transID);
			// 숫자형 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			Map<String, Double> numberRows = new HashMap<String, Double>();
			numberRows.put("latitude", 20.2);
			numberRows.put("longitude", 23.8);
			imMqttConnector.requestNumColecDatas(numberRows, new Date(), transID);
			// 문자형 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			Map<String, String> stringRows = new HashMap<String, String>();
			stringRows.put("power", "on");
			stringRows.put("speed", "high");
			imMqttConnector.requestStrColecDatas(stringRows, new Date(), transID);
			// 복합형(문자+숫자) 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			imMqttConnector.requestColecDatas(numberRows, stringRows, new Date(), transID);
			// 연결해제
			imMqttConnector.disconnect();
			// 종료
			imMqttConnector.destroy();
		} catch (SdkException e) {
			System.out.println("Code :" + e.getCode() + " Message :" + e.getMessage());
		}
	}
}


