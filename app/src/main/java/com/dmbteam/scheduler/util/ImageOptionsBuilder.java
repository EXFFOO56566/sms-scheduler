package com.dmbteam.scheduler.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by dobrikostadinov on 2/19/15.
 */
public class ImageOptionsBuilder {

    /**
     * Builds the general image options.
     *
     * @param resetViewBeforeLoading the reset view before loading
     * @param emptyUrlImage the empty url image
     * @return the display image options
     */
    public static DisplayImageOptions buildGeneralImageOptions(
            boolean resetViewBeforeLoading, int emptyUrlImage) {

        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.displayer(new RoundedBitmapDisplayer((int) 50f));
        builder.bitmapConfig(Bitmap.Config.RGB_565);
        builder.showImageForEmptyUri(emptyUrlImage > 0 ? emptyUrlImage : 0);
        builder.cacheInMemory(true);
        if (resetViewBeforeLoading)
            builder.resetViewBeforeLoading(true);
        builder.cacheOnDisc(true);
        builder.imageScaleType(ImageScaleType.IN_SAMPLE_INT);
        builder.cacheOnDisc(true);
        return builder.build();
    }

    /**
     * Creates the image loader configuration.
     *
     * @param appContext the app context
     * @return the image loader configuration
     */
    @SuppressWarnings("deprecation")
    public static ImageLoaderConfiguration createImageLoaderConfiguration(
            Context appContext) {
        // Disc cache 10mb maximum
        int MAX_DISC_CACHE_SIZE = 20 * 1024 * 1024;

        // Set memory storage
        int MEMORY_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() * 0.33);

        WindowManager windowManager = (WindowManager) appContext
                .getSystemService(Context.WINDOW_SERVICE);

        Display display = windowManager.getDefaultDisplay();
        Log.v("Utils",
                "Start building ImageLoader configuration for API level "
                        + Build.VERSION.SDK_INT);
        ImageLoaderConfiguration.Builder b = new ImageLoaderConfiguration.Builder(
                appContext);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            b.threadPoolSize(1);
        } else {
            b.threadPoolSize(3);
        }
        b.memoryCacheExtraOptions(display.getWidth()/5, display.getHeight()/10);
        b.discCacheExtraOptions(display.getWidth(), display.getHeight(),
                Bitmap.CompressFormat.PNG, 80, null);
        b.threadPriority(Thread.NORM_PRIORITY - 1);
        b.memoryCacheSize(MEMORY_CACHE_SIZE);
        b.discCacheSize(MAX_DISC_CACHE_SIZE);
        b.discCacheFileNameGenerator(new HashCodeFileNameGenerator());
        b.tasksProcessingOrder(QueueProcessingType.FIFO);
        b.defaultDisplayImageOptions(DisplayImageOptions.createSimple());
        return b.build();
    }
}
