package com.GPShunt;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class PutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_put);		
		
        //设置按钮监听事件
        Button put=(Button)findViewById(R.id.ok);//确定按钮
        put.setOnClickListener(new Button.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		//TODO Auto-generated method stub
        	}
        	
        });
        
        
        Button toback=(Button)findViewById(R.id.cancel);//取消按钮
        toback.setOnClickListener(new Button.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		//TODO Auto-generated method stub
        		finish();
        	}
        	
        });
	}
	
		
}
