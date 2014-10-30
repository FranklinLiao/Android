package com.GPShunt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;


public class MainActivity extends Activity {
	JSONParser jParser = new JSONParser();	
	   // url to get all points
	private static String url = "http://www.treashunt.net/webservice3.php";	
	private static String url_del = "http://www.treashunt.net/delete.php";
	private static String url_rep = "http://www.treashunt.net/replace.php";
	private static String url_put = "http://www.treashunt.net/newput.php";
	public  List<OverlayItem> GeoList = new ArrayList<OverlayItem>();	
	final int DIALOG_TAKE=1;
	private ProgressDialog pDialog;

	AlertDialog DIALOG_GPINFO;
	String latLongString;
	String gpString;
	String gpsInfo;
		
	/*实现菜单*/
    AlertDialog menuDialog;// menu菜单Dialog
	GridView menuGrid;
	View menuView;
	
	private final int ITEM_PUT = 0;//放置
	//private final int ITEM_REPLACE = 1;//替换
	//private final int ITEM_TAKE = 2;//取走
	private final int ITEM_HELP = 1;//帮助
	private final int ITEM_SETTINGS = 2;// 设置
	private final int ITEM_QUIT = 3;// 退出
	
	
	/*实现地图*/
	BMapManager mBMapMan = null;
	static MapView mMapView = null;
	
	private MapController mMapController = null;
	public MKMapViewListener mMapListener = null;
	FrameLayout mMapViewContainer = null;
	
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
    public NotifyLister mNotifyer=null;		
	EditText indexText = null;
	MyLocationOverlay myLocationOverlay = null;
	int index =0;
	LocationData locData = null;
		    
	
	//定时相关
	Handler handler=new Handler();
	Runnable runnable=new Runnable() {
	    @Override
	    public void run() {
	        // TODO Auto-generated method stub
	        //要做的事情
	    	sendMessage();
	        handler.postDelayed(this, 60000); //每30s发一次数据
	    }
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        		WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       
        /*创建菜单-------------------------------------------*/
        menuView = View.inflate(this, R.layout.gridview_menu, null);
		// 创建AlertDialog
		menuDialog = new AlertDialog.Builder(this).create();
		Window window = menuDialog.getWindow();     
		window.setGravity(Gravity.BOTTOM);   //window.setGravity(Gravity.BOTTOM); 		
		menuDialog.setView(menuView);
		menuDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)// 监听按键
					dialog.dismiss();
				return false;
			}
		});

		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		/** 监听menu选项 **/
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case ITEM_PUT://放置
					menuDialog.cancel();
        			AlertDialog dialog_put = new AlertDialog.Builder(MainActivity.this)
        	    	.setIcon(R.drawable.ic_dialog_alert)
        	    	.setTitle("放置")
        	    	.setMessage("确定在当前位置放置新宝物？")
        	    	.setPositiveButton("确定",new DialogInterface.OnClickListener(){
        	    		@Override
        	        	public void onClick(DialogInterface dialog ,int whichButton){
        	        		//TODO Auto-generated method stub
        	    			
        	    			Put putpoints = new Put();
        	    			putpoints.execute();
        	        	}
        	    	})
        	    	.setNegativeButton("取消",new DialogInterface.OnClickListener(){
        	    		@Override
        	        	public void onClick(DialogInterface dialog ,int whichButton){
        	        		//TODO Auto-generated method stub
        	        	}
        	    	})
        	    	.create();;
        	    	dialog_put.show();
                 
					break;
				/*case ITEM_REPLACE://替换
					menuDialog.cancel();
					Intent intent1 =new Intent();
					intent1.setClass(MainActivity.this, RepActivity.class);
					startActivity(intent1);

					break;*/
				/*case ITEM_TAKE://取走
					menuDialog.cancel();
					showDialog(DIALOG_TAKE);

					break;*/
				case ITEM_HELP://帮助
					menuDialog.cancel();
					Intent intent2 =new Intent();
					intent2.setClass(MainActivity.this, HelpActivity.class);
					startActivity(intent2);

					break;
				case ITEM_SETTINGS:// 设置
					menuDialog.cancel();

					break;
				case ITEM_QUIT:// 退出
					finish();

					break;
				
				
				}
				
				
			}
		});
        
		/*创建地图--------------------------------------------*/
        mBMapMan=new BMapManager(getApplication());
    	mBMapMan.init("18B7CE9A2CA7C8FCBB49089082D982BD0F8D79FB",  new MKGeneralListener() {
    	    @Override
    	    public void onGetPermissionState(int iError) {
    	        // TODO 返回授权验证错误，通过错误代码判断原因，MKEvent中常量值。
    	    }
    	    @Override
    	    public void onGetNetworkState(int iError) {
    	        // TODO 返回网络错误，通过错误代码判断原因，MKEvent中常量值。
    	    }
    	});  
    	//注意：请在试用setContentView前初始化BMapManager对象，否则会报错
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.bmapsView);
        mMapController = mMapView.getController();
        
        initMapView();
        
        mLocClient = new LocationClient( this );
        mLocClient.registerLocationListener( myListener );
        
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
        option.setScanSpan(5000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapView.getController().setZoom(14);
        mMapView.getController().enableClick(true);
        
        mMapView.setBuiltInZoomControls(true);
        mMapListener = new MKMapViewListener() {
			
			@Override
			public void onMapMoveFinish() {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				// TODO Auto-generated method stub
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					Toast.makeText(MainActivity.this,title,Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onGetCurrentMap(Bitmap b) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMapAnimationFinish() {
				// TODO Auto-generated method stub
				
			}
		};
		
	
		
		
		//设置按钮监听事件
		
        Button myloc=(Button)findViewById(R.id.mypos);//我的位置
        myloc.setOnClickListener(new Button.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		//TODO Auto-generated method stub
        		GeoPoint point =new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *1e6));
        		mMapController.setCenter(point);//设置地图中心点
        		latLongString="纬度："+ locData.latitude + "\n经度" + locData.longitude;
        		Toast.makeText(MainActivity.this, "您当前位置："+latLongString, Toast.LENGTH_LONG).show();
        		//不支持手动开启发送短信
        		//sendMessage();
        		
        		//Toast.makeText(MainActivity.this, "您当前位置："+latLongString, Toast.LENGTH_LONG).show();
        		//gpsInfo=locData.longitude+","+locData.latitude;
	        	//Toast.makeText(MainActivity.this, "您当前位置："+gpsInfo, Toast.LENGTH_LONG).show();
        		/*
	        	//判断是否输入了电话号码
        		EditText ref=(EditText)findViewById(R.id.telphone);//获得电话号码
        		String tel=ref.getText().toString();
        		String mobile;
        		if(tel.trim()==null||tel.trim().length()==0) {
                 	mobile = "13220188941";
                }else {
                 	mobile = tel.trim();
                }
        		gpsInfo=locData.longitude+","+locData.latitude+",";
	        	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
	        	gpsInfo+=(df.format(new Date()));// 
	       		 String content = gpsInfo;
	       		 Toast.makeText(MainActivity.this,content, Toast.LENGTH_LONG).show();

	       		 SmsManager smsManager = SmsManager.getDefault();
	             PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(), 0);
	             if(content.length() >= 70)
	             {
                   //短信字数大于70，自动分条
                   List<String> ms = smsManager.divideMessage(content);
                   
                   for(String str : ms )
                   {
                       //短信发送
                       smsManager.sendTextMessage(mobile, null, str, sentIntent, null);
                   }
	             } else
                {
                   smsManager.sendTextMessage(mobile, null, content, sentIntent, null);
                }
	             Toast.makeText(MainActivity.this, "发送成功！", Toast.LENGTH_LONG).show();
	            */
        	}
        	
        });
       
        /*
        Button ref=(Button)findViewById(R.id.mypos);//发送短信
        ref.setOnClickListener(new Button.OnClickListener(){
        	@Override
        	public void onClick(View v){
        		
        	}
        	
        });
        */
		/*我的位置图层*/
		myLocationOverlay = new MyLocationOverlay(mMapView);
		locData = new LocationData();
	    myLocationOverlay.setData(locData);
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mMapView.refresh();
		//覆盖物图层，添加标识	
				
			
	
	   
    }//onCreate结束    
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
    	//定时执行
		handler.post(runnable);
    	//handler.postDelayed(runnable,10000);//间隔1s后发送第一次)
		super.onStart();
	}

    
    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		handler.removeCallbacks(runnable);  
		super.onStop();
	}



	//异步处理从数据库下载数据
    class LoadAllPoints extends AsyncTask<String, String, List<OverlayItem>> {

    	/*doInBackground方法和onPostExecute的参数必须对应，这两个参数在AsyncTask声明的泛型参数列表中指定，
    	第一个为doInBackground接受的参数，第二个为显示进度的参数，
    	第第三个为doInBackground返回和onPostExecute传入的参数。*/
        @Override
        protected List<OverlayItem> doInBackground(String... arg0) {
            // TODO Auto-generated method stub
             // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jParser.makeHttpRequest(url, "POST", params);
            String result = json.toString();
            Log.d("All Points: ", result);            
            
            try {
            JSONObject jobj = new JSONObject(result);
            JSONArray jArray = jobj.getJSONArray("posts");               
            Log.i("hello", "hello1");
            for (int i = 0; i < jArray.length(); i++) {  
                
                JSONObject e = jArray.getJSONObject(i);                 
                double mlat=(double)(e.getDouble("lat"));
                double mlon=(double)(e.getDouble("lon"));
                String date=e.getString("date");
                Integer index = e.getInt("id");
                GeoPoint point = new GeoPoint((int) (mlat * 1E6), (int) (mlon * 1E6));
                Log.v("lon=", ":"+point.getLongitudeE6()); 
    			GeoList.add(new OverlayItem(point, date , index.toString()));
               
            }                  
            }
            
            catch(Exception ex){
            	System.out.print(ex.getMessage());
            }
            return GeoList;
        }
        
        @Override  
        protected void onPostExecute(List<OverlayItem> result) {  
            
        	for (int i=0;i<GeoList.size();i++){
    			
    			GeoPoint point = GeoList.get(i).getPoint();
    			Log.v("lat=", ":"+point.getLatitudeE6());
    		}
        	Drawable marker = getResources().getDrawable(R.drawable.iconmark);	    
    	    OverItemT ov = new OverItemT(marker);
    	    for(OverlayItem item : GeoList){
    	    	ov.addItem(item);
    	    }
    	    mMapView.getOverlays().add(ov);
    	    mMapView.refresh();	
        }  
        
    }
   
    /**
     * Background Async Task to Create new product
     * */
    class Put extends AsyncTask<String, String, String> {
         	
    	    	
    	/**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("正在上传..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            //String name = inputName.getText().toString();
            //String email = inputEmail.getText().toString();
            //String description = inputDesc.getText().toString();
        	//int index1 = id-1;
        	//GeoPoint point = GeoList.get(index).getPoint();
        	//String date = GeoList.get(index1).getTitle();
        	//Double lat = (double)(point.getLatitudeE6()/1e6);
        	//Double lon = (double)(point.getLongitudeE6()/1e6);
        	Double lat = locData.latitude;
        	Double lon = locData.longitude;
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("id", id.toString()));
            params.add(new BasicNameValuePair("lat", lat.toString()));
            params.add(new BasicNameValuePair("lon", lon.toString()));

            // getting JSON Object
            // Note that create product url accepts POST method
           try{
            JSONObject json = jParser.makeHttpRequest(url_put,"POST", params);
            String message = json.getString("message");
            return message;

           }catch(Exception e){
               e.printStackTrace(); 
               return "";          
           }
            // check for success tag
            
     
        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String message) {                    
            pDialog.dismiss();
           //message 为接收doInbackground的返回值
            Toast.makeText(getApplicationContext(), message, 8000).show();    
                
               
        }
        }
    
    /**
     * Background Async Task to delete the point
     * */
    class Del extends AsyncTask<String, String, String> {

        Integer id =null;
    	public Del (int i){
    		
    		this.id=i;
    	}
    	
    	
    	
    	/**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("正在上传..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            //String name = inputName.getText().toString();
            //String email = inputEmail.getText().toString();
            //String description = inputDesc.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", id.toString()));
            //params.add(new BasicNameValuePair("email", email));
            //params.add(new BasicNameValuePair("description", description));

            // getting JSON Object
            // Note that create product url accepts POST method
           try{
            JSONObject json = jParser.makeHttpRequest(url_del,"POST", params);
            String message = json.getString("message");
            return message;

           }catch(Exception e){
               e.printStackTrace(); 
               return "";          
           }
            // check for success tag
            
     
        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String message) {                    
            pDialog.dismiss();
           //message 为接收doInbackground的返回值
            Toast.makeText(getApplicationContext(), message, 8000).show();    
                
               
        }
        }

    /**
     * Background Async Task to Create new product
     * */
    class Rep extends AsyncTask<String, String, String> {

        Integer id =null;//数据表中的id字段
        Integer index = null;//GeoList列表的索引
    	public Rep (int i,int n){
    		
    		this.id=i;
    		this.index=n;
    	}
    	
    	
    	
    	/**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("正在上传..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            //String name = inputName.getText().toString();
            //String email = inputEmail.getText().toString();
            //String description = inputDesc.getText().toString();
        	//int index1 = id-1;
        	//GeoPoint point = GeoList.get(index).getPoint();
        	//String date = GeoList.get(index1).getTitle();
        	//Double lat = (double)(point.getLatitudeE6()/1e6);
        	//Double lon = (double)(point.getLongitudeE6()/1e6);
        	Double lat = locData.latitude;
        	Double lon = locData.longitude;
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", id.toString()));
            params.add(new BasicNameValuePair("lat", lat.toString()));
            params.add(new BasicNameValuePair("lon", lon.toString()));

            // getting JSON Object
            // Note that create product url accepts POST method
           try{
            JSONObject json = jParser.makeHttpRequest(url_rep,"POST", params);
            String message = json.getString("message");
            return message;

           }catch(Exception e){
               e.printStackTrace(); 
               return "";          
           }
            // check for success tag
            
     
        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String message) {                    
            pDialog.dismiss();
           //message 为接收doInbackground的返回值
            Toast.makeText(getApplicationContext(), message, 8000).show();    
                
               
        }
        }
        
   
	/** 菜单图片 **/
	int[] menu_image_array = {R.drawable.menu_put,R.drawable.menu_help,R.drawable.menu_settings, R.drawable.menu_quit};
	/** 菜单文字 **/
	String[] menu_name_array = {"放置", "帮助", "设置", "退出" };
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// 必须创建一项
		return super.onCreateOptionsMenu(menu);
	}
	
	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menuDialog == null) {
			menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
		} else {
			menuDialog.show();
		}
		return false;// 返回为true 则显示系统menu
	}
		

		

	
	/**
     * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;
            
            locData.latitude = location.getLatitude();
            locData.longitude = location.getLongitude();
            locData.accuracy = location.getRadius();
            locData.direction = location.getDerect();
            myLocationOverlay.setData(locData);
            mMapView.refresh();
            
        }
        
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }
    
    public class NotifyLister extends BDNotifyListener{
        public void onNotify(BDLocation mlocation, float distance) {
        }
    }



    
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        mMapView.onResume();
        
        super.onResume();
    }
    
    
    @Override
    protected void onDestroy() {
        if (mLocClient != null)
            mLocClient.stop();
        mMapView.destroy();
        super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	mMapView.onSaveInstanceState(outState);
    	
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	mMapView.onRestoreInstanceState(savedInstanceState);
    }
    
    
    private void initMapView() {
        mMapView.setLongClickable(true);
        //mMapController.setMapClickEnable(true);
        //mMapView.setSatellite(false);
    }
   
  //添加覆盖物
   public class OverItemT extends ItemizedOverlay<OverlayItem> {
        
        public  List<OverlayItem> GeoList = new ArrayList<OverlayItem>();	
        
    	//构造方法
    	public OverItemT(Drawable marker) {
    	super(marker);  	 
    		
    	populate(); //createItem(int)方法构造item。一旦有了数据，在调用其它方法前，首先调用这个方法
    	     	
    	} 
    		
    			
    	 @Override
    	protected OverlayItem createItem(int i) {
    	return GeoList.get(i);
    	}
    	 
    	 @Override
    	public int size() {
    	return GeoList.size();
    	}
    	 
    	public void addItem(OverlayItem item){
    			GeoList.add(item);
    			populate();
    	}
    	public void removeItem(int index){
    			GeoList.remove(index);
    			populate();
    	}
    	 @Override
    	// 处理当点击事件
    	protected boolean onTap(int i) {
    	 //Toast.makeText(MainActivity.this, GeoList.get(i).getSnippet(),
    	 //Toast.LENGTH_SHORT).show();
        
    	GeoPoint point = GeoList.get(i).getPoint();
    	String pid = GeoList.get(i).getSnippet();//数据表中的id字段
    	String date = GeoList.get(i).getTitle();
    	double lat = (double)(point.getLatitudeE6()/1e6);
    	double lon = (double)(point.getLongitudeE6()/1e6);
     	gpString="该藏宝点信息："+"\n纬度："+ lat + "\n经度：" + lon + "\n最新活动时间：" + date;
     	final int id=Integer.parseInt(pid);
     	final int index = i;//GeoList的列表索引
     	Log.v("pid", ":"+id);
        DIALOG_GPINFO = new AlertDialog.Builder(MainActivity.this)
        	.setIcon(R.drawable.ic_dialog_info)
        	.setTitle("藏宝点")
        	.setMessage(gpString)
        	.setPositiveButton("替换",new DialogInterface.OnClickListener(){
        		@Override
            	public void onClick(DialogInterface dialog ,int whichButton){
            		//TODO Auto-generated method stub
        			DIALOG_GPINFO.cancel();
        			AlertDialog dialog_take = new AlertDialog.Builder(MainActivity.this)
        	    	.setIcon(R.drawable.ic_dialog_alert)
        	    	.setTitle("替换")
        	    	.setMessage("确定替换原有宝物，请放置新宝物")
        	    	.setPositiveButton("确定",new DialogInterface.OnClickListener(){
        	    		@Override
        	        	public void onClick(DialogInterface dialog ,int whichButton){
        	        		//TODO Auto-generated method stub
        	    			
        	    			Rep reppoints = new Rep(id,index);
        	    			reppoints.execute();
        	        	}
        	    	})
        	    	.setNegativeButton("取消",new DialogInterface.OnClickListener(){
        	    		@Override
        	        	public void onClick(DialogInterface dialog ,int whichButton){
        	        		//TODO Auto-generated method stub
        	        	}
        	    	})
        	    	.create();;
        	    	dialog_take.show();
        			
            	}
        	})
        	.setNeutralButton("取走",new DialogInterface.OnClickListener(){
        		@Override
            	public void onClick(DialogInterface dialog ,int whichButton){
            		//TODO Auto-generated method stub
        			DIALOG_GPINFO.cancel();
        			AlertDialog dialog_take = new AlertDialog.Builder(MainActivity.this)
        	    	.setIcon(R.drawable.ic_dialog_alert)
        	    	.setTitle("取走")
        	    	.setMessage("确定取走后不放置新宝物？")
        	    	.setPositiveButton("确定",new DialogInterface.OnClickListener(){
        	    		@Override
        	        	public void onClick(DialogInterface dialog ,int whichButton){
        	        		//TODO Auto-generated method stub
        	    			
        	    			Del delpoints = new Del(id);
        	    			delpoints.execute();
        	        	}
        	    	})
        	    	.setNegativeButton("取消",new DialogInterface.OnClickListener(){
        	    		@Override
        	        	public void onClick(DialogInterface dialog ,int whichButton){
        	        		//TODO Auto-generated method stub
        	        	}
        	    	})
        	    	.create();;
        	    	dialog_take.show();
            	}
        	})
        	.setNegativeButton("取消",new DialogInterface.OnClickListener(){
        		@Override
            	public void onClick(DialogInterface dialog ,int whichButton){
            		//TODO Auto-generated method stub
            	}
        	})
        	.create();
    	DIALOG_GPINFO.show();
     	
    	return true;
    	}		 
    } 	
   
   
   public void sendMessage() {
	 //判断是否输入了电话号码
		EditText ref=(EditText)findViewById(R.id.telphone);//获得电话号码
		String tel=ref.getText().toString();
		String mobile;
		if(tel.trim()==null||tel.trim().length()==0) {
        	mobile = "13220188941";
       }else {
        	mobile = tel.trim();
       }
		gpsInfo=locData.longitude+","+locData.latitude+",";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		gpsInfo+=(df.format(new Date()));// 
  		 String content = gpsInfo;
  		 Toast.makeText(MainActivity.this,content, Toast.LENGTH_LONG).show();
  		 if(locData.longitude>100&&locData.longitude<180) {
	  		 SmsManager smsManager = SmsManager.getDefault();
	        PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(), 0);
	        if(content.length() >= 70)
	        {
	          //短信字数大于70，自动分条
	          List<String> ms = smsManager.divideMessage(content);
	          
	          for(String str : ms )
	          {
	              //短信发送
	              smsManager.sendTextMessage(mobile, null, str, sentIntent, null);
	          }
	        } else
	       {
	          smsManager.sendTextMessage(mobile, null, content, sentIntent, null);
	       }
	        Toast.makeText(MainActivity.this, "发送成功！", Toast.LENGTH_LONG).show();
  		 } else {
  			Toast.makeText(MainActivity.this,"gps info no corrent", Toast.LENGTH_LONG).show();
  		 }
   }
    
}

