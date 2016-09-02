package com.example.try_td_test;

import com.example.try_gameengine.framework.LightImage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class MyCanvas{

//	public MyCanvas(Bitmap screenImage) {
//		// TODO Auto-generated constructor stub
//		super(screenImage);
//	}
	private Canvas canvas;
	
	public MyCanvas(Canvas canvas){
		this.canvas = canvas;
	}

	public void drawImage(Bitmap bitmap, float left, float top, Paint paint){
		canvas.drawBitmap(bitmap, left, top, paint);
	}
	
	public void drawImage(Bitmap bitmap, float left, float top){
		canvas.drawBitmap(bitmap, left, top, null);
	}
	
	public void drawImage(Bitmap bitmap, float desleft, float destop, float deswidth, float desheight, int srcleft, int srctop, int srcright, int srcbottom, Paint paint){
		canvas.drawBitmap(bitmap, new Rect(srcleft, srctop, srcright, srcbottom), new RectF(desleft, destop, desleft+deswidth, destop+desheight), paint);
	}
	
	public void drawImage(Bitmap bitmap, float desleft, float destop, float deswidth, float desheight, float srcleft, float srctop, float srcright, float srcbottom, Paint paint){
		canvas.drawBitmap(bitmap, new Rect((int)srcleft, (int)srctop, (int)srcright, (int)srcbottom), new RectF(desleft, destop, desleft+deswidth, destop+desheight), paint);
	}
	
	public void drawImage(Bitmap bitmap, int desleft, int destop, int desright, int desbottom, Rect srcRect, Paint paint){
		canvas.drawBitmap(bitmap, srcRect, new Rect(desleft, destop, desright, desbottom), paint);
	}
	
	public void drawText(String text, float x, float y, Paint paint){
		canvas.drawText(text, x, y, paint);
	}
	
	public void drawText(String text, float x, float y){
		canvas.drawText(text, x, y, null);
	}
	
	public void fillRect(float left, float top, float width, float height, Paint paint){
		canvas.drawRect(left, top, left + width, top + height, paint);
	}
	
	public void drawImage(LightImage lightImage, int desleft, int destop, int desright, int desbottom, int srcleft, int srctop, int srcright, int srcbottom, Paint paint){
		canvas.drawBitmap(lightImage.getBitmap(), new Rect(srcleft, srctop, srcright, srcbottom), new Rect(desleft, destop, desright, desbottom), paint);
	}
	
	public void drawImage(LightImage lightImage, int desleft, int destop, int desright, int desbottom, int srcleft, int srctop, int srcright, int srcbottom){
		canvas.drawBitmap(lightImage.getBitmap(), new Rect(srcleft, srctop, srcright, srcbottom), new Rect(desleft, destop, desright, desbottom), null);
	}
	
	public void drawImage(LightImage lightImage, float desleft, float destop, float deswidth, float desheight, float srcleft, float srctop, float srcright, float srcbottom, Paint paint){
		canvas.drawBitmap(lightImage.getBitmap(), new Rect((int)srcleft, (int)srctop, (int)srcright, (int)srcbottom), new RectF(desleft, destop, desleft+deswidth, destop+desheight), paint);
	}
	
	public void drawImage(LightImage lightImage, float desleft, float destop, float deswidth, float desheight, float srcleft, float srctop, float srcright, float srcbottom){
		canvas.drawBitmap(lightImage.getBitmap(), new Rect((int)srcleft, (int)srctop, (int)srcright, (int)srcbottom), new RectF(desleft, destop, desleft+deswidth, destop+desheight), null);
	}
	
	public void drawImage(LightImage lightImage, float left, float top){
		canvas.drawBitmap(lightImage.getBitmap(), left, top, null);
	}
	
	public void drawImage(LightImage lightImage, float left, float top, Paint paint){
		canvas.drawBitmap(lightImage.getBitmap(), left, top, paint);
	}
}
