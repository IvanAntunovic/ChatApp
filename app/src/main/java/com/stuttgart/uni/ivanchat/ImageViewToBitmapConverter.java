package com.stuttgart.uni.ivanchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

public class ImageViewToBitmapConverter {

    public static Bitmap convert(ImageView v){

        Bitmap bm = ((BitmapDrawable)v.getDrawable()).getBitmap();

        return bm;
    }

}
