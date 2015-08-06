package cn.cc.sdk.demo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cn.cc.sdk.demo.util.ImageDownLoader;
import cn.cc.sdk.demo.util.MyLogger;


/**
 * Created by shangsong on 14-9-23.
 */
public class LocalApplication extends Application {
    private boolean isBind = false;
    private List<ServiceListener> mListenerList;
    private VideoService videoService = null;
    private static LocalApplication instance;
    private final String LOG_TAG = this.getClass().getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
        MyLogger.i(LOG_TAG, "onCreate*******************************");
        instance = this;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        clearImageCache();
        MyLogger.d(LOG_TAG, "onLowMemory:release cache");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        clearImageCache();
        MyLogger.d(LOG_TAG, "onTerminate:release cache");
    }

    private void clearImageCache() {
        ImageDownLoader.getInstance().clear();
    }

    public void onActivityCreate() {
        mListenerList = new ArrayList<ServiceListener>();
        bindService();
    }

    private void bindService() {
        Intent intent = new Intent(this, VideoService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            isBind = true;
            videoService = ((VideoService.LocalBinder) iBinder).getService();

            for (ServiceListener listener : mListenerList) {
                listener.onServiceDisconnected(videoService);
            }
            mListenerList.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyLogger.w(LOG_TAG, "onServiceDisconnected");
            isBind = false;
        }
    };

    public void getVideoService(ServiceListener listener) {
        if (videoService == null) {
            mListenerList.add(listener);
            bindService();
        } else {
            listener.onServiceDisconnected(videoService);
        }
    }

    private void unBindService() {
        if (isBind) {
            unbindService(mServiceConnection);
            isBind = false;
        }

    }

    public void clear() {
        unBindService();
        videoService = null;
    }

    public interface ServiceListener {
        void onServiceDisconnected(VideoService service);
    }

    public static Context getAppContext() {
        return instance;
    }

}
