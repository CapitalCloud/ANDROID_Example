package cn.cc.sdk.demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;

import cn.cc.android.sdk.DownloadManager;
import cn.cc.android.sdk.SDKClient;
import cn.cc.android.sdk.UploadManager;
import cn.cc.android.sdk.VideoManager;
import cn.cc.android.sdk.callback.ResponseListener;
import cn.cc.android.sdk.util.VideoData;
import cn.cc.sdk.demo.util.LocalConstants;

public class VideoService extends Service {
    private SDKClient mClient = null;
    private LocalBinder myBinder = new LocalBinder();

    private final File STORAGE_DIRECTORY = new File(Environment.getExternalStorageDirectory() + "/demo");

    private final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = SDKClient.getInstance(getApplication(), LocalConstants.TOKEN);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClient != null) {
            mClient.release();
            mClient = null;
        }

        myBinder = null;
    }

    public class LocalBinder extends Binder {
        public VideoService getService() {
            return VideoService.this;
        }
    }

    public void getVideo(String videoId, ResponseListener responseListener) {
        mClient.getVideoManager().get(videoId, responseListener);
    }


    private SDKClient getClient() {
        return mClient == null ? mClient.getInstance(this, LocalConstants.TOKEN) : mClient;
    }

    public VideoManager getVideoManager() {
        return getClient().getVideoManager();
    }


    public UploadManager getUploadManager() {
        return getClient().getUploadManager();
    }

    public DownloadManager getDownloadManager() {
        return getClient().getDownloaderManager(STORAGE_DIRECTORY);
    }

    public void getAllPlayers(ResponseListener responseListener) {
        mClient.getVideoManager().getAllPlayers(responseListener);
    }


    public void json(String videoId, ResponseListener responseListener) {
        mClient.getVideoManager().json(videoId, responseListener);
    }

    public void list(int start, int max, String categoryId, ResponseListener responseListener) {
        mClient.getVideoManager().list(start, max, categoryId, responseListener);
    }

    public void update(VideoData videoData, ResponseListener responseListener) {
        mClient.getVideoManager().update(videoData, responseListener);
    }

}
