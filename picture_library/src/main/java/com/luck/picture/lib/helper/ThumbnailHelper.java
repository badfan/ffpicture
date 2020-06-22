package com.luck.picture.lib.helper;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

public class ThumbnailHelper {
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        return bitmap;
    }

}
