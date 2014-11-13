package com.android.qrcode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/**
 * 二维码工具类
 * 
 * @author Yang
 * 
 */
public class QRCodeUtil {

	private static final String TAG = QRCodeUtil.class.getSimpleName();

	/** 颜色值 */

	/** 黑色 **/
	public static final int BLACK = 0xff000000;
	/** 白色 */
	public static final int WHITE = 0xffffffff;
	/** 黄色 */
	public static final int YELLOW = 0xffffff00;
	/** 红色 */
	public static final int RED = 0xffff0000;
	/** 蓝色 */
	public static final int BLUE = 0xff0000ff;
	/** 绿色 */
	public static final int GREEN = 0xff008000;
	/** 深灰色 */
	public static final int DARK_GRAY = 0xffa9a9a9;
	/** 灰色 */
	public static final int GRAY = 0xff808080;


	/**
	 * 生成二维码，不包含logo
	 * 
	 * @param message
	 *            二维码内容
	 * @param sizeOfCode
	 *            生成二维码的尺寸
	 * @return
	 * @throws Exception
	 *             二维码内容为空，尺寸<=0
	 */
	public static Bitmap createQRCode(String message, int sizeOfCode)
			throws Exception {
		return createQRCode(message, sizeOfCode, BLACK);
	}

	private static Bitmap createQRCode(String message, int sizeOfCode, int color)
			throws Exception {
		if (null == message || message.length() <= 0) {
			throw new NullPointerException("QRCode message is not null");
		}

		if (sizeOfCode <= 0) {
			throw new Exception("QRCode Size is not < 0");
		}

		BitMatrix matrix = new MultiFormatWriter().encode(message,
				BarcodeFormat.QR_CODE, sizeOfCode, sizeOfCode);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = color;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 生成包含logo的二维码 （logo尺寸尽量越小越好，超过二维码尺寸的1/8就会覆盖二维码而难以识别）
	 * 代码对尺寸做了处理，如果超出1/6则按二维码尺寸的1/6展示logo
	 * 
	 * @param bitmap
	 *            logo图片
	 * @param message
	 *            二维码内容
	 * @param sizeOfCode
	 *            生成二维码尺寸
	 * @param sizeOfLogo
	 *            显示logo尺寸
	 * @return
	 * @throws Exception
	 * 
	 * 
	 */
	public static Bitmap createQRCode(Bitmap bitmap, String message,
			int sizeOfCode, int sizeOfLogo) throws Exception {
		return createQRCode(bitmap, message, sizeOfCode, sizeOfLogo, BLACK);

	}

	private static Bitmap createQRCode(Bitmap bitmap, String message,
			int sizeOfCode, int sizeOfLogo, int color) throws Exception {
		Log.i(TAG, "/----------createQRCode-------------/");
		if (bitmap == null || sizeOfLogo <= 0) {
			return createQRCode(message, sizeOfCode);
		}

		if (null == message || message.length() <= 0) {
			throw new NullPointerException("QRCode message is not null");
		}

		if (sizeOfCode <= sizeOfLogo) {
			throw new Exception("QRCode size is smaller than the logo size");
		}
		if (sizeOfCode / sizeOfLogo <= 6) {
			sizeOfLogo = sizeOfCode / 7;
		}
		Log.i(TAG, "QRCode size:" + sizeOfCode + " X " + sizeOfCode);
		Log.i(TAG, "logo size:" + sizeOfLogo + " X " + sizeOfLogo);

		Bitmap logoBitmap = scaleBitmap(bitmap, sizeOfLogo);// logo缩放
		final int LOGO_HALFWIDTH = sizeOfLogo / 2;
		// 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
		MultiFormatWriter formatWriter = new MultiFormatWriter();
		BitMatrix matrix = formatWriter.encode(message, BarcodeFormat.QR_CODE,
				sizeOfCode, sizeOfCode);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		// 二维矩阵转为一维像素数组,也就是一直横着排了
		int halfW = width / 2;
		int halfH = height / 2;
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x > halfW - LOGO_HALFWIDTH && x < halfW + LOGO_HALFWIDTH
						&& y > halfH - LOGO_HALFWIDTH
						&& y < halfH + LOGO_HALFWIDTH) {
					pixels[y * width + x] = logoBitmap.getPixel(x - halfW
							+ LOGO_HALFWIDTH, y - halfH + LOGO_HALFWIDTH);
				} else {
					if (matrix.get(x, y)) {
						pixels[y * width + x] = color;
					}
				}
			}
		}
		Bitmap codeBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		// 通过像素数组生成bitmap
		codeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		Log.i(TAG, "/----------finishedQRCode-------------/");
		return codeBitmap;

	}

	/**
	 * bitmap 缩放处理
	 * 
	 * @param bitmap
	 * @param size
	 * @return
	 */
	private static Bitmap scaleBitmap(Bitmap bitmap, int size) {
		if (bitmap == null) {
			return null;
		}
		Matrix m = new Matrix();
		float sx = (float) size / bitmap.getWidth();
		float sy = (float) size / bitmap.getHeight();
		m.setScale(sx, sy);
		Bitmap scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), m, false);
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		return scaleBitmap;

	}

	/**
	 * 二维码添加图片背景
	 * 
	 * @param background
	 * @param backgroundSize
	 * @param code
	 * @return
	 */
	public static Bitmap addBackGround(Bitmap background, int backgroundSize,
			Bitmap code) {
		if (background == null) {
			return code;
		}
		int codeSize = code.getWidth();
		if (backgroundSize < codeSize) {
			backgroundSize = codeSize;
		}
		Bitmap newBitmap = Bitmap.createBitmap(backgroundSize, backgroundSize,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		Log.i(TAG, "code bitmap width:"+codeSize);
		int padding = (backgroundSize - codeSize) / 2;

		canvas.drawBitmap(scaleBitmap(background, backgroundSize), 0, 0, null);
		canvas.drawBitmap(code, padding, padding, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newBitmap;
	}

	/**
	 * 二维码添加纯色背景
	 * 
	 * @param color
	 * @param code
	 * @return
	 */
	public static Bitmap addBackGround(int color, Bitmap code) {
		int codeSize = code.getWidth();
		Log.i(TAG, "code bitmap width:" + codeSize);
		Bitmap newBitmap = Bitmap.createBitmap(codeSize, codeSize,
				Bitmap.Config.ARGB_8888);

		int[] pix = new int[codeSize * codeSize];

		for (int y = 0; y < codeSize; y++)
			for (int x = 0; x < codeSize; x++) {
				int index = y * codeSize + x;
				pix[index] = color;
			}
		newBitmap.setPixels(pix, 0, codeSize, 0, 0, codeSize, codeSize);

		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(code, 0, 0, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newBitmap;
	}


	/**
	 * 保存生成的二维码图片到本地，文件名为yyyyMMddhhssmm 尺寸X尺寸.png，保存至ChuanShuo文件夹下
	 * 
	 * @param bitmap
	 * @param size
	 */
	public static void saveBitmap(Bitmap bitmap, int size) {
		if (null == bitmap)
			return;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (!sdCardExist) {
			return;
		}
		File sdDir = Environment.getExternalStorageDirectory();
		String dirPath = sdDir + File.separator + "ChuanShuo";
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String name = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(
				System.currentTimeMillis()));
		// String imageName = name + ".png";
		String imageName = name + " " + size + "X" + size + ".png";
		File file = new File(dir, imageName);
		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

}
