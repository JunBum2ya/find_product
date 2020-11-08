import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.kt.smcp.gw.ca.comm.exception.SdkException;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.http.BaseInfo;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.http.IMHttpConnector;
import com.kt.smcp.gw.ca.util.IMUtil;

public class Sample {
	public static void main(String[] args) {
		IMHttpConnector imHttpConnector = new IMHttpConnector();
		BaseInfo baseInfo = null;
		Integer resultCode;
		Long transID;
		try {
			// 초기화
			baseInfo = IMUtil.getBaseInfo("IoTSDK.properties");			
			imHttpConnector.init(baseInfo);
			// 인증		
			resultCode = imHttpConnector.authenticate();
			System.out.println("ResultCode : " + resultCode);			
			// 숫자형 단건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			resultCode = imHttpConnector.requestNumColecData("temperature", 22.1 , new Date(), transID);
			System.out.println("ResultCode : " + resultCode);			
			// 문자형 단건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			resultCode = imHttpConnector.requestStrColecData("LED", "ON", new Date(), transID);
			System.out.println("ResultCode : " + resultCode);			
			// 숫자형 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			Map<String, Double> numberRows = new HashMap<String, Double>();
			numberRows.put("latitude", 20.2);
			numberRows.put("longitude", 23.8);
			resultCode = imHttpConnector.requestNumColecDatas(numberRows, new Date(), transID);
			System.out.println("ResultCode : " + resultCode);			
			// 문자형 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			Map<String, String> stringRows = new HashMap<String, String>();
			stringRows.put("power", "on");
			stringRows.put("speed", "high");
			resultCode = imHttpConnector.requestStrColecDatas(stringRows, new Date(), transID);
			System.out.println("ResultCode : " + resultCode);			
			// 복합형(문자+숫자) 다건 전송
			transID = IMUtil.getTransactionLongRoundKey4();
			resultCode = imHttpConnector.requestColecDatas(numberRows, stringRows, new Date(), transID);
			System.out.println("ResultCode : " + resultCode);
			// 해제 및 종료
			imHttpConnector.destroy();
		} catch (SdkException e) {
			System.out.println("Code :" + e.getCode() + " Message :" + e.getMessage());
		}
	}
}
