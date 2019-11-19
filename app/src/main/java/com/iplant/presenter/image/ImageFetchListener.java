package com.iplant.presenter.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * com.wasu.imageloader.ImageFetcherInterface
 * @author Administrator <br/>
 * create at 2014年8月7日 下午1:37:22
 */
public interface ImageFetchListener {

	/**
	 * 加载任务开始
	 * @param imageView
	 * @param imgUrl
	 */
	public void onFetchAdded(ImageView imageView, String imgUrl);

	/**
	 * 取消加载任务
	 * @param imageView
	 * @param imgUrl
	 */
	public void onFetchCancelled(ImageView imageView, String imgUrl);

	/**
	 * 加载失败
	 * @param imageView
	 * @param imgUrl
	 * @param e
	 */
	public void onFetchFailed(ImageView imageView, String imgUrl, Exception e);

	/**
	 * 加载完成
	 * @param imageView
	 * @param imgUrl
	 * @param bm
	 */
	public void onFetchCompleted(ImageView imageView, String imgUrl, Bitmap bm);

}
