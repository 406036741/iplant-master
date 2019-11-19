
package com.iplant.presenter.image;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageFetcherModule implements ImageFetcherInterface {
    private static final String tag = "ImageFetcherModule";

    /**
     * loader实例
     */
    private ImageLoader mImageLoader;

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * 全局配置
     */
    private ImageLoaderConfiguration mImageLoaderConfiguration;

    /**
     * 默认显示配置
     */
    private DisplayImageOptions mDisplayImageOptions;

    /**
     * 圆角显示配置
     */
    private DisplayImageOptions mRoundedDisplayImageOptions;

    /**
     * 默认显示配置
     */
    private DisplayImageOptions mDisplayAlphaImageOptions;

    /**
     * single instance
     */
    private volatile static ImageFetcherModule g_instance;

	/** 用于记录该模块是否被初始化， 没有被初始化的模块将无法使用 **/
	private boolean mbIsInited = false;
	
    /**
     * 获取实体类单例
     * 
     * @return ImageFetcherModule单例
     */
    public static ImageFetcherModule getInstance() {
        if (g_instance == null) {
            synchronized (ImageLoader.class) {
                if (g_instance == null) {
                    g_instance = new ImageFetcherModule();
                }
            }
        }
        return g_instance;
    }

    /**
     * 初始化
     * 
     * @param c 上下文环境
     */
    public void init(Context c) {
		if (mbIsInited) {
			return;
		}

		mbIsInited = true;
		
        this.mImageLoader = ImageLoader.getInstance();
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(c)
                .threadPoolSize(2)
                .threadPriority(Thread.MIN_PRIORITY - 1)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(40 * 1024 * 1024)).diskCacheSize(100 * 1024 * 1024)
                .imageDownloader(new BaseImageDownloader(c)) // default
                .imageDecoder(new BaseImageDecoder(false)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .build();
        mDisplayImageOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(false) // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .considerExifParams(false) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .displayer(new SimpleBitmapDisplayer()) // default
                .handler(new Handler()) // default
                .build();

        mDisplayAlphaImageOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(false) // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .considerExifParams(false) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .displayer(new FadeInBitmapDisplayer(500)) // default
                .handler(new Handler()) // default
                .build();

        mRoundedDisplayImageOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false) // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .considerExifParams(false) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default
                .handler(new Handler()) // default
                .build();

        init();
    }

    protected ImageFetcherModule() {

    }

    /**
     * 设置圆形图片
     * 
     * @param Res 默认图片
     * @param imageView 显示的控件
     * @param url 图片链接
     * @param flag 是否缓存
     */
    public static void setRouteDisplayImager(ImageView imageView, String url, boolean flag) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(flag).bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(200)).build();
        ImageLoader.getInstance().displayImage(url, imageView, options);
    }

    /**
     * 初始化
     */
    private synchronized void init() {
        mImageLoader.init(mImageLoaderConfiguration);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#attachImage(java.lang.String,
     * android.widget.ImageView)
     */
    @Override
    public void attachImage(String imgUrl, ImageView imageView) {
        mImageLoader.displayImage(imgUrl, imageView, mDisplayImageOptions);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#attachImage(java.lang.String,
     * android.widget.ImageView, java.lang.Boolean)
     */
    @Override
    public void attachImage(String imgUrl, ImageView imageView, int radius) {
        setDisplayer(mRoundedDisplayImageOptions, new RoundedBitmapDisplayer(radius));
        mImageLoader.displayImage(imgUrl, imageView, mRoundedDisplayImageOptions);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#attachImage(java.lang.String,
     * android.widget.ImageView, java.lang.Boolean)
     */
    @Override
    public void attachImageAlpha(String imgUrl, ImageView imageView, int radius, int duration) {
        setDisplayer(mRoundedDisplayImageOptions, new FadeInBitmapRoundDisplayer(duration, true,
                false, false, radius, 0));
        mImageLoader.displayImage(imgUrl, imageView, mRoundedDisplayImageOptions);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#attachImage(java.lang.String,
     * android.widget.ImageView, com.wasu.imageloader.ImageFetchListener)
     */
    @Override
    public void attachImage(String imgUrl, ImageView imageView, final ImageFetchListener listener) {
        mImageLoader.displayImage(imgUrl, imageView, mDisplayImageOptions,
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        if (view instanceof ImageView) {
                            listener.onFetchAdded((ImageView) view, imageUri);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if (view instanceof ImageView) {
                            listener.onFetchFailed((ImageView) view, imageUri, new Exception(
                                    failReason.getCause()));
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (view instanceof ImageView) {
                            listener.onFetchCompleted((ImageView) view, imageUri, loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        if (view instanceof ImageView) {
                            listener.onFetchCancelled((ImageView) view, imageUri);
                        }
                    }
                });
    }

    @Override
    public void attachImageAlpha(String imgUrl, ImageView imageView,
            final ImageFetchListener listener) {
        mImageLoader.displayImage(imgUrl, imageView, mDisplayAlphaImageOptions,
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        if (view instanceof ImageView) {
                            listener.onFetchAdded((ImageView) view, imageUri);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if (view instanceof ImageView) {
                            listener.onFetchFailed((ImageView) view, imageUri, new Exception(
                                    failReason.getCause()));
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (view instanceof ImageView) {
                            listener.onFetchCompleted((ImageView) view, imageUri, loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        if (view instanceof ImageView) {
                            listener.onFetchCancelled((ImageView) view, imageUri);
                        }
                    }
                });
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#getImage(java.lang.String)
     */
    @Override
    public Bitmap getImage(String imgUrl) {
        return mImageLoader.loadImageSync(imgUrl, mDisplayImageOptions);
    }

    /*
     * (非 Javadoc) <p>Title: loadImage</p> <p>Description: </p>
     * @param imgUrl
     * @param listener
     * @see
     * com.wasu.module.image.ImageFetcherInterface#loadImage(java.lang.String,
     * com.wasu.module.image.ImageFetchListener)
     */
    @Override
    public void loadImage(String imgUrl, final ImageFetchListener listener) {
        mImageLoader.loadImage(imgUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (view instanceof ImageView) {
                    listener.onFetchAdded((ImageView) view, imageUri);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (view instanceof ImageView) {
                    listener.onFetchFailed((ImageView) view, imageUri,
                            new Exception(failReason.getCause()));
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (view instanceof ImageView) {
                    listener.onFetchCompleted((ImageView) view, imageUri, loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (view instanceof ImageView) {
                    listener.onFetchCancelled((ImageView) view, imageUri);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#getImage(java.lang.String,
     * int, int)
     */
    @Override
    public Bitmap getImage(String imgUrl, int targetImageWidth, int targetImageHeight) {
        return mImageLoader.loadImageSync(imgUrl,
                new ImageSize(targetImageWidth, targetImageHeight), mDisplayImageOptions);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#cancelTask(android.widget.
     * ImageView)
     */
    @Override
    public void cancelTask(ImageView imageView) {
        mImageLoader.cancelDisplayTask(imageView);
    }

    /*
     * (non-Javadoc)
     * @see com.wasu.imageloader.ImageFetcherInterface#stopAllTask()
     */
    @Override
    public void stopAllTask() {
        mImageLoader.stop();
    }

    /*
     * (non-Javadoc)
     * @see com.wasu.imageloader.ImageFetcherInterface#clearMemoryCache()
     */
    @Override
    public void clearMemoryCache() {
        mImageLoader.clearMemoryCache();
    }

    /*
     * (non-Javadoc)
     * @see com.wasu.imageloader.ImageFetcherInterface#clearDiskCache()
     */
    @Override
    public void clearDiskCache() {
        mImageLoader.clearDiskCache();
        mImageLoader.stop();
    }

    /*
     * (non-Javadoc)
     * @see
     * com.wasu.imageloader.ImageFetcherInterface#setDisplayer(com.wasu.thirdparty
     * .image.BitmapDisplayer)
     */
    @Override
    public void setDisplayer(BitmapDisplayer bitmapDisplayer) {
        setDisplayer(mDisplayImageOptions, bitmapDisplayer);
    }

    private void setDisplayer(DisplayImageOptions option, BitmapDisplayer bitmapDisplayer) {
        Field field;
        try {
            field = DisplayImageOptions.class.getDeclaredField("displayer");
            field.setAccessible(true);
            field.set(option, bitmapDisplayer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
