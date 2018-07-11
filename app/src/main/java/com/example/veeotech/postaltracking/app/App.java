package com.example.veeotech.postaltracking.app;

import android.app.Application;
import android.os.Build;
import android.posapi.PosApi;
import android.util.Log;
import android.widget.Toast;

import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.example.veeotech.postaltracking.utils.SPUtils;

/**
 * Application类，请注意，整个代码中均只能使用这里的mPosApi变量，
 * 不能另外再重复实例化PosApi，否则将会出现打印延迟或者不打印，
 * 扫描会扫描二次或者不出扫描光的情况，请注意
 * @author wsl
 *
 */
public class App extends Application{

	private static String mCurDev1 = "";

	static App instance = null;
	//PosSDK mSDK = null;
	static PosApi mPosApi = null;
	public App(){
		super.onCreate();
		instance = this;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//mDb = Database.getInstance(this);
		Log.v("hello", "APP onCreate~~");
		//PosApi类初始化，该类为项目核心类，请注意务必实例化，否则将会出现无法打印和扫描等现象
		//初始
		init();

	}

	public static void init(){
		//根据型号进行初始化mPosApi类
		if (Build.MODEL.contains("LTE")|| Build.DISPLAY.contains("3508")||
				Build.DISPLAY.contains("403")||
				Build.DISPLAY.contains("35S09")) {
			mPosApi = PosApi.getInstance(instance);
			mPosApi.initPosDev("ima35s09");
			setCurDevice("ima35s09");
		} else if(Build.MODEL.contains("5501")){
			mPosApi = PosApi.getInstance(instance);
			mPosApi.initPosDev("ima35s12");
			setCurDevice("ima35s12");
		}else if(Build.MODEL.contains("3505")){
			mPosApi = PosApi.getInstance(instance);
			mPosApi.initPosDev(PosApi.PRODUCT_MODEL_IMA80M01);
			setCurDevice(PosApi.PRODUCT_MODEL_IMA80M01);
			SPUtils.put(instance, IntentKeyUtils.SP_INITSCANNER,1);
		}else{
			SPUtils.put(instance,IntentKeyUtils.SP_INITSCANNER,0);
			Toast.makeText(instance, "此設備為手機設備,無法使用生成條碼功能", Toast.LENGTH_SHORT).show();
		}

	}


	public static  App getInstance(){
		if(instance==null){
			instance =new App();
		}
		return instance;
	}

	public String getCurDevice() {
		return mCurDev1;
	}

	public static  void setCurDevice(String mCurDev) {
		mCurDev1 = mCurDev;
	}

	//其他地方引用mPosApi变量
	public PosApi getPosApi(){
		return mPosApi;
	}

}
