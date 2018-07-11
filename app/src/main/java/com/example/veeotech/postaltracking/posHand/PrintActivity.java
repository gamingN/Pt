package com.example.veeotech.postaltracking.posHand;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.posapi.PosApi;
import android.posapi.PrintQueue;
import android.posapi.PrintQueue.OnPrintListener;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.veeotech.postaltracking.R;
import com.example.veeotech.postaltracking.utils.IntentKeyUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 * 打印测试类
 * @author wsl
 *
 */
public class PrintActivity extends Activity {

	private Button btnCreat1D;
	private Button btnCreat2D;
	private Button btnCreatPic;
	private Button btnPrint;
	private Button btnPrintText;
	private Button btnPrintMix;
	private ImageView iv;
	private EditText etContent;

	private EditText etImgHeight;
	private EditText etImgWidth;
	private EditText etImgMarginLeft;
	private EditText etConcentration;

	private EditText ed_str;

	private Bitmap mBitmap = null;

	private PrintQueue mPrintQueue = null;


	private int mCurSerialNo = 3; // usart3
	private int mBaudrate = 4; // 9600

	private static final int REQUEST_EX = 1;

	boolean isCanPrint=true;

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_barcode);
		//控件初始化
		initViews();
		//获取当前电量
		getButerryNum();
		//打印队列赋值
		mPrintQueue = new PrintQueue(this, ScanService.mApi);
		//打印队列初始化
		mPrintQueue.init();
		//打印队列设置监听
		mPrintQueue.setOnPrintListener(new OnPrintListener() {
			//打印完成
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				//打印完成
				Toast.makeText(getApplicationContext(),
						getString(R.string.print_complete), Toast.LENGTH_SHORT)
						.show();
				//当前可打印
				isCanPrint=true;
			}
			//打印失败
			@Override
			public void onFailed(int state) {
				// TODO Auto-generated method stub
				isCanPrint=true;
				switch (state) {
					case PosApi.ERR_POS_PRINT_NO_PAPER:
						// 打印缺纸
						showTip(getString(R.string.print_no_paper));
						break;
					case PosApi.ERR_POS_PRINT_FAILED:
						// 打印失败
						showTip(getString(R.string.print_failed));
						break;
					case PosApi.ERR_POS_PRINT_VOLTAGE_LOW:
						// 电压过低
						showTip(getString(R.string.print_voltate_low));
						break;
					case PosApi.ERR_POS_PRINT_VOLTAGE_HIGH:
						// 电压过高
						showTip(getString(R.string.print_voltate_high));
						break;
				}
			}

			@Override
			public void onGetState(int arg0) {
				// TODO Auto-generated method stub

			}
			//打印设置
			@Override
			public void onPrinterSetting(int state) {
				// TODO Auto-generated method stub
				isCanPrint=true;
				switch(state){
					case 0:
						Toast.makeText(PrintActivity.this, "持續有紙", Toast.LENGTH_SHORT).show();
						break;
					case 1:
						//缺纸
						Toast.makeText(PrintActivity.this, getString(R.string.no_paper), Toast.LENGTH_SHORT).show();
						break;
					case 2:
						//检测到黑标
						Toast.makeText(PrintActivity.this, getString(R.string.label), Toast.LENGTH_SHORT).show();
						break;
				}
			}
		});

		//黑标检测
//		findViewById(R.id.black_btn).setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				//显示提示对话框
//				showAlertDialog(getString(R.string.label_test));
//			}
//		});

	}

	//黑标检测相关
	private AlertDialog.Builder builder;
	private boolean isShowAlertDialog = false;
	public void showAlertDialog(String msg) {

		isShowAlertDialog = true;

		builder = new AlertDialog.Builder(this);
		builder.setMessage(msg);

		builder.setTitle(getString(R.string.tips));
		//标签纸测试
		builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();


//				Bitmap bmp = createQRImage("1234567890", 300, 300);
//				byte[] printData = BitmapTools
//						.bitmap2PrinterBytes(bmp);
//				mPrintQueue.addBmp(50, 30, bmp.getWidth(),
//						bmp.getHeight(), printData);


				mPrintQueue.addText(50, "\n\n".getBytes());

				Bitmap btMap = BarcodeCreater.creatBarcode(PrintActivity.this,
						"1234567890", 384, 100, true, 1);
				byte[] printData = BitmapTools
						.bitmap2PrinterBytes(btMap);
				mPrintQueue.addBmp(50, 0, btMap.getWidth(),
						btMap.getHeight(), printData);

				mPrintQueue.printStart();

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						//1->走纸检测   mWidth 黑标的宽度
						ScanService.mApi.printerSetting(1, 0);
					}
				}, 1500);

			}
		});
		builder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						isShowAlertDialog = false;
					}
				});

		builder.create().show();

	}


	private void initViews() {
		// TODO Auto-generated method stub

		btnCreat1D = (Button) this.findViewById(R.id.btnCreat1d);
		btnCreat2D = (Button) this.findViewById(R.id.btnCreat2d);
//		btnCreatPic = (Button) this.findViewById(R.id.btnCreatPic);
		btnPrint = (Button) this.findViewById(R.id.btnPrint);
//		btnPrintMix = (Button) this.findViewById(R.id.btnPrintMix);
		btnPrintText = (Button) this.findViewById(R.id.btnPrintText);

		iv = (ImageView) this.findViewById(R.id.iv2d);

		etContent = (EditText) this.findViewById(R.id.etContent);
		etImgHeight = (EditText) this.findViewById(R.id.etContentHeight);
		etImgWidth = (EditText) this.findViewById(R.id.etContentWidth);
		etImgMarginLeft = (EditText) this.findViewById(R.id.etMarginLeft);
		etConcentration = (EditText) this.findViewById(R.id.etConcentration);

		ed_str = (EditText) this.findViewById(R.id.ed_str);

		ed_str.setText("");
		etContent.setText(getIntent().getStringExtra(IntentKeyUtils.INTENT_MA_CREATE));
		etImgHeight.setText("200");
		etImgWidth.setText("300");
		etImgMarginLeft.setText("0");
		etConcentration.setText("35");

		//生成一维码
		btnCreat1D.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (TextUtils.isEmpty(etContent.getText().toString())
						|| TextUtils.isEmpty(etImgHeight.getText().toString())
						|| TextUtils.isEmpty(etImgWidth.getText().toString())) {
					Toast.makeText(getApplicationContext(), "請堅持圖像參數",
							Toast.LENGTH_SHORT).show();
					return;
				}
				//如果判断为字节长度大于其字符长度，则判定为无法生成条码的字符
				if (etContent.getText().toString().getBytes().length > etContent.getText().toString().length()) {
					Toast.makeText(
							PrintActivity.this,
							getString(R.string.cannot_create_bar), Toast.LENGTH_SHORT)
							.show();
					return;
				}

				//文字编辑框设置不可见
				ed_str.setVisibility(View.GONE);
				//图片控件可见
				iv.setVisibility(View.VISIBLE);
				//图片宽度
				int mWidth = Integer.valueOf(etImgWidth.getText().toString()
						.trim());
				//图片高度
				int mHeight = Integer.valueOf(etImgHeight.getText().toString()
						.trim());
				//生成一维码
				mBitmap = BarcodeCreater.creatBarcode(getApplicationContext(),
						etContent.getText().toString(), mWidth, mHeight, true,
						1);
				//imageview中显示出生成的一维码
				iv.setImageBitmap(mBitmap);

			}
		});
		//生成二维码
		btnCreat2D.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//判断宽高和二维码字符是否为空
				if (TextUtils.isEmpty(etContent.getText().toString())
						|| TextUtils.isEmpty(etImgHeight.getText().toString())
						|| TextUtils.isEmpty(etImgWidth.getText().toString())) {
					Toast.makeText(getApplicationContext(), "請檢查圖像參數",
							Toast.LENGTH_SHORT).show();
					return;
				}

				//文字编辑框设置不可见
				ed_str.setVisibility(View.GONE);
				//图片控件可见
				iv.setVisibility(View.VISIBLE);
				//二维码宽度
				int mWidth = Integer.valueOf(etImgWidth.getText().toString()
						.trim());
				//二维码高度
				int mHeight = Integer.valueOf(etImgHeight.getText().toString()
						.trim());
				//二维码生成
				mBitmap = BarcodeCreater.encode2dAsBitmap(etContent.getText()
						.toString(), mWidth, mHeight, 2);
				//imageview中显示出生成的二维码
				iv.setImageBitmap(mBitmap);

			}
		});

//		btnCreatPic.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				//检测读写功能
//				verifyStoragePermissions(PrintActivity.this);
//				//选择图片
//				Intent intent = new Intent(
//						Intent.ACTION_PICK,
//						MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//				startActivityForResult(intent, REQUEST_EX);
//
//			}
//		});

		//文字打印按钮监听
		btnPrintText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isCanPrint) return;
				//电量低于12%时候不执行打印，并弹出提示
				if(level_battry<=12){
					Toast.makeText(PrintActivity.this, "Low power,can't print!", Toast.LENGTH_SHORT).show();
					return;
				}
				//打印文字
				printText();
			}
		});

		//打印图片
		btnPrint.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//图片为空则返回
				if (mBitmap == null)
					return;
				//打印中，不执行本次操作
				if(!isCanPrint) return;
				//电量低于12%不执行打印
				if(level_battry<=12){
					Toast.makeText(PrintActivity.this, "Low power,can't print!", Toast.LENGTH_SHORT).show();
					return;
				}
				//获取打印左边距离
				int mLeft = Integer.valueOf(etImgMarginLeft.getText()
						.toString().trim());
				byte[] printData = BitmapTools.bitmap2PrinterBytes(mBitmap);
				int concentration = Integer.valueOf(etConcentration.getText()
						.toString().trim());

				if(concentration<=25){
					concentration=25;
				}
				mPrintQueue.addBmp(concentration, mLeft, mBitmap.getWidth(),
						mBitmap.getHeight(), printData);
				try {
					mPrintQueue.addText(concentration, "\n\n\n\n\n".toString()
							.getBytes("GBK"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//设为不可打印
				isCanPrint=false;
				//打印队列开始执行
				mPrintQueue.printStart();

			}
		});

		//打印测试文字
//		btnPrintMix.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if(!isCanPrint) return;
//
//				if(level_battry<=12){
//					Toast.makeText(PrintActivity.this, "Low power,can't print!", Toast.LENGTH_SHORT).show();
//					return;
//				}
//
//				//打印混合测试文字
//				printMix();
//
//			}
//		});
	}

	//检测是否有读写权限，确保在android各个版本都能够读写SD卡
	public static void verifyStoragePermissions(Activity activity) {
		try {
			// 检测是否有写的权限
			int permission = ActivityCompat.checkSelfPermission(activity,
					"android.permission.WRITE_EXTERNAL_STORAGE");
			if (permission != PackageManager.PERMISSION_GRANTED) {
				// 没有写的权限，去申请写的权限，会弹出对话框
				ActivityCompat.requestPermissions(activity,
						PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//图片路径
	String picPath;
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_EX && resultCode == RESULT_OK
				&& null != data) {

			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			picPath = picturePath;
			iv.setImageURI(selectedImage);
			//图片压缩
			mBitmap = resizeImage(BitmapFactory.decodeFile(picPath),300,300);
			if (mBitmap.getHeight() > 384) {
				mBitmap = BitmapFactory.decodeFile(picPath);
				iv.setImageBitmap(resizeImage(mBitmap, 300, 300));
			}
			mBitmap=BitmapTools.gray2Binary(mBitmap);
			ed_str.setVisibility(View.GONE);
			iv.setVisibility(View.VISIBLE);
			cursor.close();

		}
	}

	//图片处理，按给定的宽高进行缩放
	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		Bitmap BitmapOrg = bitmap;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		if (width >= newWidth) {
			float scaleWidth = ((float) newWidth) / width;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleWidth);
			Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
					height, matrix, true);
			return resizedBitmap;
		} else {

			Bitmap bitmap2 = Bitmap.createBitmap(newWidth, newHeight,
					bitmap.getConfig());
			Canvas canvas = new Canvas(bitmap2);
			canvas.drawColor(Color.WHITE);

			canvas.drawBitmap(BitmapOrg, (newWidth - width) / 2, 0, null);

			return bitmap2;
		}
	}

	/*
	 * 打印文字 size 1 --倍大小 2--2倍大小
	 */
	private void addPrintTextWithSize(int size, int concentration, byte[] data) {
		if (data == null)
			return;
		// 2倍字体大小
		byte[] _2x = new byte[] { 0x1b, 0x57, 0x02 };
		// 1倍字体大小
		byte[] _1x = new byte[] { 0x1b, 0x57, 0x01 };
		byte[] mData = null;
		if (size == 1) {
			mData = new byte[3 + data.length];
			// 1倍字体大小 默认
			System.arraycopy(_1x, 0, mData, 0, _1x.length);
			System.arraycopy(data, 0, mData, _1x.length, data.length);

			mPrintQueue.addText(concentration, mData);

		} else if (size == 2) {
			mData = new byte[3 + data.length];
			// 1倍字体大小 默认
			System.arraycopy(_2x, 0, mData, 0, _2x.length);
			System.arraycopy(data, 0, mData, _2x.length, data.length);

			mPrintQueue.addText(concentration, mData);

		}

	}

	/**
	 * text to bitmap
	 *
	 * @param str
	 * @return
	 */
	public Bitmap word2bitmap(String str) {
		Bitmap bMap = Bitmap.createBitmap(384, 120, Config.ARGB_8888);
		Canvas canvas = new Canvas(bMap);
		canvas.drawColor(Color.WHITE);
		TextPaint textPaint = new TextPaint();
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(35.0F);
		StaticLayout layout = new StaticLayout(str, textPaint, bMap.getWidth(),
				Alignment.ALIGN_NORMAL, (float) 1.0, (float) 0.0, true);
		layout.draw(canvas);
		return bMap;
	}

	//打印当前编辑框中的文字
	private void printText() {

		int concentration = Integer.valueOf(etConcentration.getText()
				.toString().trim());

		if(concentration<=25){
			concentration=25;
		}

		String str = ed_str.getText().toString();

		try {
			addPrintTextWithSize(1, concentration,
					(str + "\n").getBytes("GBK"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		isCanPrint=false;
		mPrintQueue.printStart();

	}

	//打印混合语句
	private void printMix() {
		try {
			int concentration = Integer.valueOf(etConcentration.getText()
					.toString().trim());

			if(concentration<=25){
				concentration=25;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("        收 银 凭 据                 ");
			sb.append("\n");
			sb.append("时间   : ");
			sb.append("2016-11-15     16:00");
			sb.append("\n");
			sb.append("操作员:admin");
			sb.append("\n");
			sb.append("收据单号：1234567890");
			sb.append("\n");
			sb.append("编号  数量  单价  折扣  小计");
			sb.append("\n");
			sb.append("-----------------------------");
			sb.append("\n");
			sb.append("AM126   1  1200  0   1200");
			sb.append("\n");
			sb.append("AM127   1  1300  0   1300");
			sb.append("\n");
			sb.append("AM128   1  1400  0   1400");
			sb.append("\n");
			sb.append("-----------------------------");
			sb.append("\n");
			sb.append("共销售数量: 3 ");
			sb.append("\n");
			sb.append("售价合计(RMB): 3900");
			sb.append("\n");
			sb.append("实收金额(RMB): 3900");
			sb.append("\n");
			sb.append("找零金额(RMB): 0");
			sb.append("\n");
			sb.append("-----------------------------");
			sb.append("\n");
			sb.append("支付方式: 微信支付 ");
			sb.append("\n");
			sb.append("欢迎下次光临    请保留好小票！");
			sb.append("\n");
			sb.append("-----------------------------");
			sb.append("\n");
			byte[] text = null;
			text = sb.toString().getBytes("GBK");

			addPrintTextWithSize(1, concentration, text);

			sb = new StringBuilder();
			sb.append("   谢谢惠顾");
			sb.append("\n");

			text = sb.toString().getBytes("GBK");
			addPrintTextWithSize(2, concentration, text);

			sb = new StringBuilder();
			sb.append("\n");
			text = sb.toString().getBytes("GBK");
			addPrintTextWithSize(1, concentration, text);

			int mWidth = 300;
			int mHeight = 60;
			mBitmap = BarcodeCreater.creatBarcode(getApplicationContext(),
					"1234567890", mWidth, mHeight, true, 1);
			byte[] printData = BitmapTools.bitmap2PrinterBytes(mBitmap);
			mPrintQueue.addBmp(concentration, 30, mBitmap.getWidth(),
					mBitmap.getHeight(), printData);

			sb = new StringBuilder();
			sb.append("\n");
			sb.append("     扫一扫下载APP更多优惠");
			sb.append("\n");
			sb.append("\n");
			text = sb.toString().getBytes("GBK");
			addPrintTextWithSize(1, concentration, text);

			mWidth = 150;
			mHeight = 150;

			mBitmap = BarcodeCreater.encode2dAsBitmap("1234567890", mWidth,
					mHeight, 2);
			printData = BitmapTools.bitmap2PrinterBytes(mBitmap);
			mPrintQueue.addBmp(concentration, 100, mBitmap.getWidth(),
					mBitmap.getHeight(), printData);

			sb = new StringBuilder();
			sb.append("1个月之内可凭票至服务台开具发票!");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			sb.append("\n");
			text = sb.toString().getBytes("GBK");
			//把文字添加到打印队列
			addPrintTextWithSize(1, concentration, text);

			/**
			 * 打印队列启动，请注意，该打印队列在打印过程中只能执行一次，同时执行多次时候将会出现打印顺序错乱问题
			 */
			mPrintQueue.printStart();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 提示框
	 * @param msg 提示内容
	 */
	private void showTip(String msg) {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.tips))
				.setMessage(msg)
				.setNegativeButton(getString(R.string.close),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).show();
	}

	/**
	 * 文字转图片
	 *
	 * @param str 要转成图片的字符串
	 * @return
	 */
	public Bitmap word2bitmap2(String str,int wight,int height,int size) {
		Bitmap bMap = Bitmap.createBitmap(wight, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bMap);
		canvas.drawColor(Color.WHITE);
		TextPaint textPaint = new TextPaint();
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(size);
		StaticLayout layout = new StaticLayout(str, textPaint, bMap.getWidth(),
				Alignment.ALIGN_NORMAL, (float) 1.0, (float) 0.0, true);
		layout.draw(canvas);
		return bMap;
	}

	/**
	 * 图片旋转
	 * @param bm
	 * @param orientationDegree
	 * @return
	 */
	Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

		Matrix m = new Matrix();
		m.setRotate(orientationDegree, (float) bm.getWidth() / 2,
				(float) bm.getHeight() / 2);
		float targetX, targetY;
		if (orientationDegree == 90) {
			targetX = bm.getHeight();
			targetY = 0;
		} else {
			targetX = bm.getHeight();
			targetY = bm.getWidth();
		}

		final float[] values = new float[9];
		m.getValues(values);

		float x1 = values[Matrix.MTRANS_X];
		float y1 = values[Matrix.MTRANS_Y];

		m.postTranslate(targetX - x1, targetY - y1);

		Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(),
				Config.ARGB_8888);
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bm1);
		canvas.drawBitmap(bm, m, paint);

		return bm1;
	}
	//获取电量
	public void getButerryNum(){
		registerReceiver(new BatteryBroadcastReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	public int level_battry=50;

	/**接受电量改变广播*/
	class BatteryBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {

			if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){

				level_battry = intent.getIntExtra("level", 0);

			}
		}
	}

	/**
	 * 文字转图片
	 * @param text 文字
	 * @param textSize 转成图片里面的文字大小
	 * @return
	 */
	public static Bitmap textAsBitmap(String text, float textSize) {

		TextPaint textPaint = new TextPaint();

		textPaint.setColor(Color.BLACK);

		textPaint.setTextSize(textSize);

		StaticLayout layout = new StaticLayout(text, textPaint, 300,
				Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
		Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,
				layout.getHeight() + 20, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.translate(10, 10);
		canvas.drawColor(Color.WHITE);

		layout.draw(canvas);

		return bitmap;

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//记住当前的宽高参数，下次进来时候设为上次的数据
		SharedPreferences sharedPreferences = this.getSharedPreferences("setting", MODE_PRIVATE);
		etImgHeight.setText(sharedPreferences.getString("height","200"));
		etImgWidth.setText(sharedPreferences.getString("wight","300"));

	}

	@Override
	protected void onStop() {
		//获取一个文件名为test、权限为private的xml文件的SharedPreferences对象
		SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);

		//得到SharedPreferences.Editor对象，并保存数据到该对象中
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("height", etImgHeight.getText().toString().trim());
		editor.putString("wight", etImgWidth.getText().toString().trim());
		//保存key-value对到文件中
		editor.commit();
		super.onStop();
	}

	/**
	 * 生成二维码 要转换的地址或字符串,可以是中文
	 *
	 * @param url
	 * @param width
	 * @param height
	 * @return
	 */
	public Bitmap createQRImage(String url, final int width, final int height) {
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "GBK");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url,
					BarcodeFormat.QR_CODE, width, height, hints);
			int[] pixels = new int[width * height];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap
					.createBitmap(width, height, Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mBitmap != null) {
			mBitmap.recycle();
		}

		if (mPrintQueue != null) {
			mPrintQueue.close();
		}
//		unregisterReceiver(receiver);
	}

}
