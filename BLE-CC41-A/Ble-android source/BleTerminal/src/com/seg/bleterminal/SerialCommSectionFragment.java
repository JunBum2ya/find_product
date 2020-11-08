/**
 * @Copyright 2014 by SEG, Inc. All rights reserved.
**/
package com.seg.bleterminal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * <pre>
 * 블루투스 장치를 검색하고 정보를 보여줄 프래그 먼트
 * DevInfoSectionFragment.java
 * @author bume16
 * @date 2014. 6. 4.
 * @version 
 * </pre>
 **/
public class SerialCommSectionFragment extends Fragment{

	private Context mCtx = null;

	/**
	 * 로그박스
	 */
	private TextView mTvLogBox = null;
	
	/**
	 * 메시지 입력창
	 */
	private EditText mEdtMessage = null;
	
	/**
	 * 전송 버튼
	 */
	private Button mBtnSend = null;
	
	/**
	 * 에코 토클
	 */
	private ToggleButton mTbEcho = null;
	
	/**
	 * 엔터 토글 
	 */
	private ToggleButton mTbEnter = null;
	
	
    // newInstance constructor for creating fragment with arguments
    public static SerialCommSectionFragment newInstance() {
    	
    	SerialCommSectionFragment fragment = new SerialCommSectionFragment();
    	
    	// Supply index input as an argument.
        Bundle args = new Bundle();
        
        fragment.setArguments(args);
        
        return fragment;
    }

    public SerialCommSectionFragment()
    {
    	
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.serialcomm_fragment, container, false);
		
		//initial fragment layout
		mTvLogBox = (TextView)rootView.findViewById(R.id.tvLogbox);
		mTvLogBox.setVerticalScrollBarEnabled(true);
		mTvLogBox.setMovementMethod(new ScrollingMovementMethod());
		mEdtMessage = (EditText)rootView.findViewById(R.id.edtMessage);		
		mBtnSend = (Button)rootView.findViewById(R.id.btnSendMessage);
		mTbEcho = (ToggleButton)rootView.findViewById(R.id.tbEcho);
		mTbEnter = (ToggleButton)rootView.findViewById(R.id.tbEnter);
		
		return rootView;//super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mCtx = activity;
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mCtx = null;
	}
	
	/**
	 * <PRE>
	 * Comment : <br>
	 * 로그박스를 업데이트 한다
	 * @author bume16
	 * @date 2014. 6. 10.
	 * </PRE>
	 * @param str
	 */
	public void updateLogbox(String str)
	{
		mTvLogBox.append(str);
	}
	
	/**
	 * <PRE>
	 * Comment : <br>
	 * 입력된 메시지를 반환한다.
	 * @author bume16
	 * @date 2014. 6. 10.
	 * </PRE>
	 * @return
	 */
	public String getInputMessage()
	{
		String str = "";
		if(mEdtMessage.getText().length() < 0);
		else
			str = mEdtMessage.getText().toString();
			
		return str;
	}
	
}
