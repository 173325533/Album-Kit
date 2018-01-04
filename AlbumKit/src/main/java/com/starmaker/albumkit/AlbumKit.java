package com.starmaker.albumkit;

import java.io.File;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.content.pm.ConfigurationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import  android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.app.ActivityManager;
import java.lang.Exception;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liuxuetao on 12/29/16.
 */

public class AlbumKit {
    /*相册中包含的最大照片数目*/
    public static int MAX_PHOTO_NUMBER  = 12;

    /*生成视频时的分辨率*/
    private int mWidth,mHeight;
    /*生成视频对象*/
    private File mVideoFile;
    /*电子相册当前的风格*/
    private AlbumStyle mStyle = AlbumStyle.NO_EFFECT;
    private int mLoopStep;



    private Timer mTimer;
    private static final int INCREMENT = 1;

    private AlbumKitView mImageView;
    private AlbumKitRender mImageRender;
    private int threadhold = 0;

    private AlbumKitRender mRender;

    public AlbumKit(AlbumKitView mView,AlbumKitRender mRender){

        mLoopStep = 100;

        mImageView = mView;
        mImageRender = mRender;

    }

    /*1.设置电子相册的图片数，count:相册内图片数*/
    public void setAlbumPhotoCount(int count){
        mImageRender.setAlbumPhotoCount(count);
    }

    /*2.设置电子相册的分辨率，width：相册宽，height：相册高*/
    public void setAlbumResolution(int width,int height){
        mImageRender.setAlbumResolution(width,height);

    }

    public void setSteps(int vStep)
    {

        mLoopStep = vStep;
        mImageRender.setSteps(vStep);
    }

    /*3.设置编码视频的文件名和位置,videoFile:目标文件对象*/
    public void setVideoPath(File videoFile)
    {
        mVideoFile = videoFile;
    }

    /*4.设置电子相册的模版类型,style：模版类型*/
    public void setStyleOfAlbum(AlbumStyle style){
        mStyle = style;


    }
    /*5.向相册内添加一张照片，photoPath:照片文件名和路径*/
    public int addPhotoInAlbum(Uri photoUri) throws Exception{
          return mImageRender.addPhotoInAlbum(photoUri);
    }
    /*6.观看相册效果*/
    public void watchAlbum(){


        mImageRender.watchAlbum();

        mTimer = new Timer(true);
        mTimer.schedule((TimerTask) new SwitchTask(mImageView.getContext()), 0, 40);


    }
    /*7.将相册编码成视频*/
    public void encodeAlbum() {

    }




    public enum AlbumStyle {
        NO_EFFECT,
    }



    class SwitchTask extends TimerTask
    {
        private Context mContext;
        public SwitchTask(Context context)
        {
            this.mContext = context;
        }
        public void run()
        {
            threadhold += INCREMENT;
            if (threadhold > mLoopStep) {
                threadhold = 0;

            }

   //         mImageView.requestRender();

        }

    }

}
