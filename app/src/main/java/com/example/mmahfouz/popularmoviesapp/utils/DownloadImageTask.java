package com.example.mmahfouz.popularmoviesapp.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by mmahfouz on 1/11/2016.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;
    private Context mContext;
    private ProgressDialog dialog;
    /* called like this
        new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute(imgUrl);
    */
    public DownloadImageTask(ImageView bmImage, Context mContext) {
        this.bmImage = bmImage;
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialog.show(mContext, "", "Loading...", true);
        dialog.show();
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        dialog.dismiss();
        bmImage.setImageBitmap(result);
    }
}