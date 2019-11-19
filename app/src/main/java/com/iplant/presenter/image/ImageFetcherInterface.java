package com.iplant.presenter.image;

import com.nostra13.universalimageloader.core.display.BitmapDisplayer;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageFetcherInterface {

	/**
	 * 根据url地址加载到指定imageview
	 * @param imgUrl
	 * @param imageView
	 */
	public void attachImage(String imgUrl, ImageView imageView);
	
	/**
	 * 根据url地址加载到指定imageview
	 * @param imgUrl
	 * @param imageView
	 * @param radius 圆角半径
	 */
	public void attachImage(String imgUrl, ImageView imageView, int radius);

	/**
	 * 根据url地址加载到指定imageview,且带有加载回调
	 * @param imgUrl 
	 * @param imageView
	 * @param listener
	 */
	public void attachImage(String imgUrl, ImageView imageView, ImageFetchListener listener);
	
	/**
	 * 根据url地址加载到指定imageview,且带有加载回调
	 * @param imgUrl 
	 * @param imageView
	 * @param listener
	 */
	public void attachImageAlpha(String imgUrl, ImageView imageView, ImageFetchListener listener);

	/**
	 * 根据url获取bitmap
	 * @param imgUrl
	 * @return
	 */
	public Bitmap getImage(String imgUrl);

	/**
	 * 根据url获取指定大小bitmap
	 * @param imgUrl
	 * @param targetImageWidth 宽
	 * @param targetImageHeight 高
	 * @return bitmap
	 */
	public Bitmap getImage(String imgUrl, int targetImageWidth, int targetImageHeight);
	
	
	 /**
     * 根据url地址加载图片,且带有加载回调
     * @param imgUrl
     * @param listener
     * @return
     */
	public void loadImage(String imgUrl, ImageFetchListener listener);
	

	/**
	 * 取消某个imageview的加载任务
	 * @param imageView
	 */
	public void cancelTask(ImageView imageView);
	
	/**
	 * 取消所有加载任务
	 */
	public void stopAllTask();

	/**
	 * 清除内存缓存
	 */
	public void clearMemoryCache();

	/**
	 * 清楚文件缓存
	 */
	public void clearDiskCache();
	
	/**
	 * 设置显示效果
	 * @param bitmapDisplayer
	 */
	public void setDisplayer(BitmapDisplayer bitmapDisplayer);

	/**
	 * 圆角，渐影显示
	 * @param imgUrl
	 * @param imageView
	 * @param radius
	 * @param duration
	 */
	void attachImageAlpha(String imgUrl, ImageView imageView, int radius, int duration);

}