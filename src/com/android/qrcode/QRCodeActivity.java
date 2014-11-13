package com.android.qrcode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * 本demo是仿微信的二维码名片 本身google的二维码是一个开源的项目我们要制作一个二维码很简单 本例的作用是将图片与二维码结合，当然图片不能太大
 * ，要不然二维码读不出来。
 */
public class QRCodeActivity extends Activity {

	private final String message = "http://weixin.qq.com/r/De9WTrjEW_Ukrfwt97of";
	// private final String message = "http://www.baidu.com";
	private int codeSize = 300;// 生成二维码的尺寸
	private final int logoSize = 40;// logo尺寸
	private ImageView noIcon_img;// 没有logo
	private ImageView noBg_img;// 白底黑色二维码
	private ImageView bg_img;// 有底色的二维码

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);
		int screen_width = getResources().getDisplayMetrics().widthPixels;
		codeSize = screen_width / 4 * 3;
		findViews();
		setViews();
	}

	private void findViews() {
		noIcon_img = (ImageView) findViewById(R.id.noIcon_img);
		noBg_img = (ImageView) findViewById(R.id.nobg_img);
		bg_img = (ImageView) findViewById(R.id.bg_img);
	}

	private void setViews() {
		// 构造需要插入的图片对象
		Bitmap logoBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.ic_launcher)).getBitmap();
		try {
			Bitmap noIconBitmap = QRCodeUtil.createQRCode(message, codeSize);

			Bitmap codeBitmap = QRCodeUtil.createQRCode(logoBitmap, message,
					codeSize, logoSize);

			Bitmap bgBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.bg);
			Bitmap logoBitmap1 = ((BitmapDrawable) getResources().getDrawable(
					R.drawable.ic_launcher)).getBitmap();
			Bitmap codeBitmap1 = QRCodeUtil.createQRCode(logoBitmap1, message,
					codeSize, logoSize);

			// 二维码添加背景图
			Bitmap c1 = QRCodeUtil.addBackGround(bgBitmap, codeSize,
					codeBitmap1);
			// 二维码添加纯色背景
			Bitmap c2 = QRCodeUtil
					.addBackGround(QRCodeUtil.YELLOW, codeBitmap1);


			noIcon_img.setImageBitmap(noIconBitmap);
			noBg_img.setImageBitmap(codeBitmap);
			// bg_img.setImageBitmap(c1);
			bg_img.setImageBitmap(c2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
