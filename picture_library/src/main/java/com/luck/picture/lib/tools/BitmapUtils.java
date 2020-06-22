package com.luck.picture.lib.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @author：luck
 * @date：2020-01-15 18:22
 * @describe：BitmapUtils
 */
public class BitmapUtils {
    /**
     * 旋转Bitmap
     *
     * @param bitmap
     * @param angle
     * @return
     */
    public static Bitmap rotatingImage(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 判断拍照 图片是否旋转
     *
     * @param degree
     */
    public static void rotateImage(int degree, String path) {
        if (degree > 0) {
            try {
                // 针对相片有旋转问题的处理方式
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                File file = new File(path);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                bitmap = rotatingImage(bitmap, degree);
                if (bitmap != null) {
                    saveBitmapFile(bitmap, file);
                    bitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存Bitmap至本地
     *
     * @param bitmap
     * @param file
     */
    public static void saveBitmapFile(Bitmap bitmap, File file) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取旋转角度
     *
     * @param orientation
     * @return
     */
    public static int getRotationAngle(int orientation) {
        switch (orientation) {
            case 1:
                return 0;
            case 3:
                return 180;
            case 6:
                return 90;
            case 8:
                return 270;
        }
        return 0;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @return
     */
    public static Bitmap resizeImg(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (w / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (h / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, newOpts);
        Log.e("callback", "resizeImg=" + be + "/" + bitmap.getHeight() + "/" + bitmap.getWidth()+"/"+bitmap.getByteCount());
        return bitmap;// 压缩好比例大小后再进行质量压缩
    }

    public static Bitmap compress(Bitmap bitmap, long targetSize) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int option = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, option, stream);
        while (stream.toByteArray().length >= targetSize * 1024) {
            if (option <= 5) {
                break;
            }
            stream.reset();
            option -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, option, stream);
        }
        Log.e("compress", "压缩:" + stream.toByteArray().length);
        return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().length);
    }

    public static byte[] compressToByte(Bitmap bitmap, long targetSize) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int option = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, option, stream);
        while (stream.toByteArray().length >= targetSize * 1024) {
            stream.reset();
            option -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, option, stream);
            if (option < 5) {
                break;
            }
        }
        Log.e("compress", "压缩:" + stream.toByteArray().length);
        return stream.toByteArray();
    }


    public static byte[] drawableWidthCropWidthCompressAndToBytes(Drawable drawable, float cropRate, long targetSize) {

        return compressToByte(crop(resizeImg(drawableToBitmap(drawable)), cropRate), targetSize);
    }


    public static Bitmap fill(Bitmap source, float rateH_W) {
        if (source == null) {
            return null;
        }
        int rsWidth = source.getWidth();
        int rsHeight = source.getHeight();
        int startX = 0;
        int startY = 0;
        Bitmap filledBitmap;
        float sourceRate = rsHeight * 1.f / rsWidth;
        if (sourceRate >= rateH_W) {
            int filledWidth = (int) (rsHeight / rateH_W);
            int filledHeight = rsHeight;
            startX = (filledWidth - rsWidth) / 2;
            startY = 0;
            filledBitmap = Bitmap
                    .createBitmap(
                            filledWidth,
                            filledHeight,
                            Bitmap.Config.RGB_565);
            filledBitmap.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(filledBitmap);
            Paint paint = new Paint();
            canvas.drawBitmap(source, startX, startY, paint);

        } else {
            int filledWidth = rsWidth;
            int filledHeight = (int) (rsWidth * rateH_W);
            startX = 0;
            startY = (filledHeight - rsHeight) / 2;
            filledBitmap = Bitmap
                    .createBitmap(
                            filledWidth,
                            filledHeight,
                            Bitmap.Config.RGB_565);
            filledBitmap.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(filledBitmap);
            Paint paint = new Paint();
            canvas.drawBitmap(source, startX, startY, paint);
        }

        Log.e("cropbitmap", "原始图片：h=" + rsHeight + "|w=" + rsWidth + "|r=" + sourceRate + "----"
                + "填充后图片：h=" + filledBitmap.getHeight() + "|w=" + filledBitmap.getWidth() + "|r=" + (filledBitmap.getHeight() * 1.f / filledBitmap.getWidth()));
        return filledBitmap;
    }


    public static Bitmap crop(Bitmap source, float rateH_W) {
        if (source == null) {
            return null;
        }
        int bWidth = source.getWidth();
        int bHeight = source.getHeight();
        int startX = 0;
        int startY = 0;
        int cropWidth = bWidth;
        int cropHeight = bHeight;
        float sourceRate = bHeight * 1.f / bWidth;
        if (sourceRate >= rateH_W) {
            startX = 0;
            startY = (bHeight - (int) (bWidth * rateH_W)) / 2;
            cropWidth = bWidth;
            cropHeight = (int) (bWidth * rateH_W);
        } else {
            startX = (bWidth - (int) (bHeight / rateH_W)) / 2;
            startY = 0;
            cropWidth = (int) (bHeight / rateH_W);
            cropHeight = bHeight;
        }

        Bitmap cropBitmap = Bitmap.createBitmap(source, startX, startY, cropWidth, cropHeight);
        Log.e("cropbitmap", "线程-" + Thread.currentThread().getName() + ",原始图片：h=" + bHeight + "|w=" + bWidth + "|r=" + sourceRate + "----"
                + "裁剪后图片：h=" + cropBitmap.getHeight() + "|w=" + cropBitmap.getWidth() + "|r=" + (cropBitmap.getHeight() * 1.f / cropBitmap.getWidth()));
        return cropBitmap;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String byteToBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] base64ToBytes(String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    public static String fileToBase64(String thumbVideo) {
        Bitmap bitmap = BitmapFactory.decodeFile(thumbVideo);
        return bitmapToBase64(bitmap);
    }

    public static Bitmap fileToBitmap(String thumbVideo) {
        return BitmapFactory.decodeFile(thumbVideo);
    }


}
