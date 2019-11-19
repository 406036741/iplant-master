package com.iplant.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class CaptchaCode {
	private static final char[] CHARS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};
	
	private static CaptchaCode bpUtil;
	private CaptchaCode(){};
	public static CaptchaCode getInstance() {
		if(bpUtil == null)
			bpUtil = new CaptchaCode();
		return bpUtil;
	}
	//default settings
	private static final int DEFAULT_CODE_LENGTH = 4;//��֤��ĳ���  ������4λ
	private static final int DEFAULT_FONT_SIZE = 60;//�����С
	private static final int DEFAULT_LINE_NUMBER = 3;//������������
	private static final int BASE_PADDING_LEFT = 20; //��߾�
	private static final int RANGE_PADDING_LEFT = 35;//��߾෶Χֵ  
	private static final int BASE_PADDING_TOP = 42;//�ϱ߾�
	private static final int RANGE_PADDING_TOP = 15;//�ϱ߾෶Χֵ
	private static final int DEFAULT_WIDTH = 200;//Ĭ�Ͽ��.ͼƬ���ܿ�
	private static final int DEFAULT_HEIGHT = 70;//Ĭ�ϸ߶�.ͼƬ���ܸ�
	private  final int DEFAULT_COLOR=0xdf;//Ĭ�ϱ�����ɫֵ
	
	//settings decided by the layout xml
	//canvas width and height
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT; 
	
	//random word space and pading_top
	private int base_padding_left = BASE_PADDING_LEFT;
	private int range_padding_left = RANGE_PADDING_LEFT;
	private int base_padding_top = BASE_PADDING_TOP;
	private int range_padding_top = RANGE_PADDING_TOP;
	
	//number of chars, lines; font size
	private int codeLength = DEFAULT_CODE_LENGTH;
	private int line_number = DEFAULT_LINE_NUMBER;
	private int font_size = DEFAULT_FONT_SIZE;
	
	//variables
	private String code;//�������ɵ���֤��
	private int padding_left, padding_top;
	private Random random = new Random();
	
	private Bitmap createBitmap() {
		padding_left = 0;
		
		Bitmap bp = Bitmap.createBitmap(width, height, Config.ARGB_8888); 
		Canvas c = new Canvas(bp);

		code = createCode();
		
		c.drawColor(Color.rgb(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR));
		Paint paint = new Paint();
		paint.setTextSize(font_size);
		
		for (int i = 0; i < code.length(); i++) {
			randomTextStyle(paint);
			randomPadding();
			c.drawText(code.charAt(i)+"", padding_left, padding_top, paint);
		}

		for (int i = 0; i < line_number; i++) {
			drawLine(c, paint);
		}
		
		c.save( );//����
		c.restore();//
		return bp;
	}
	
	public String getCode() {
		return code.toLowerCase();
	}
	
	public Bitmap getBitmap(){
		return createBitmap();
	}
	private String createCode() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < codeLength; i++) {
			buffer.append(CHARS[random.nextInt(CHARS.length)]);
		}
		return buffer.toString();
	}
	
	private void drawLine(Canvas canvas, Paint paint) {
		int color = randomColor();
		int startX = random.nextInt(width);
		int startY = random.nextInt(height);
		int stopX = random.nextInt(width);
		int stopY = random.nextInt(height);
		paint.setStrokeWidth(1);
		paint.setColor(color);
		canvas.drawLine(startX, startY, stopX, stopY, paint);
	}
	
	private int randomColor() {
		return randomColor(1);
	}

	private int randomColor(int rate) {
		int red = random.nextInt(256) / rate;
		int green = random.nextInt(256) / rate;
		int blue = random.nextInt(256) / rate;
		return Color.rgb(red, green, blue);
	}
	
	private void randomTextStyle(Paint paint) {
		int color = randomColor();
		paint.setColor(color);
		paint.setFakeBoldText(random.nextBoolean());  //trueΪ���壬falseΪ�Ǵ���
		float skewX = random.nextInt(11) / 10;
		skewX = random.nextBoolean() ? skewX : -skewX;
		paint.setTextSkewX(skewX); //float���Ͳ�����������ʾ��б��������б
	}
	
	private void randomPadding() {
		padding_left += base_padding_left + random.nextInt(range_padding_left);
		padding_top = base_padding_top + random.nextInt(range_padding_top);
	}
}
