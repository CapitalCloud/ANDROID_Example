package cn.cc.sdk.demo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import cn.cc.sdk.demo.R;

/**
 * Created by shangsong on 14-9-24.
 */
public class ImageDownLoader {
    private static LruCache<String, Bitmap> lruCache;
    private static ImageDownLoader bitmapLoader = null;
    private  final  String LOG_TAG=this.getClass().getSimpleName();

    private ImageDownLoader() {
        if (lruCache == null) {
            long maxMemoerySize = Runtime.getRuntime().maxMemory() / 1024;
            int memorySize = (int) (maxMemoerySize / 8);
            lruCache = new LruCache<String, Bitmap>(memorySize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
    }

    private static synchronized void init() {
        if (bitmapLoader == null) {
            bitmapLoader = new ImageDownLoader();
        }

    }

    public static ImageDownLoader getInstance() {
        if (bitmapLoader == null) {
            init();
        }

        return bitmapLoader;
    }

    public void downloadImage(final String url, final ImageView imageView) {
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }

        if (cancelPotentialDownload(url, imageView)) {
            ImageDownloaderTask downloaderTask = new ImageDownloaderTask(imageView);
            WeakReference<ImageDownloaderTask> weakReference = new WeakReference<ImageDownloaderTask>(downloaderTask);
            imageView.setTag(weakReference);
            imageView.setImageResource(R.drawable.default_image);
            downloaderTask.execute(url);
        }

    }


    public void clear() {
        lruCache.evictAll();
        lruCache = null;
    }

    private Bitmap getBitmapFromCache(String key) {
        final Bitmap bitmap = lruCache.get(key);
        if (bitmap != null) {
            lruCache.remove(key);
            lruCache.put(key, bitmap);
        }
        return bitmap;
    }

    private void addBitmapToCache(String key, Bitmap bitmap) {
        if (lruCache.get(key) == null) {
            lruCache.put(key, bitmap);
        }
    }


    private boolean cancelPotentialDownload(String url, ImageView imageView) {
        ImageDownloaderTask downloaderTask = getImageDownloadTask(imageView);
        if (downloaderTask != null) {
            String bitmapUrl = downloaderTask.url;
            if (bitmapUrl == null || (!bitmapUrl.equals(url))) {
                downloaderTask.cancel(true);
            } else {
                return false;
            }
        }

        return true;
    }


    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageReference = new WeakReference<ImageView>(imageView);
        }


        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return downloadBitmap(url);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }

            if(bitmap==null){
                return;
            }

            addBitmapToCache(url, bitmap);

            if (imageReference != null) {
                ImageView imageView = imageReference.get();
                ImageDownloaderTask downloaderTask = getImageDownloadTask(imageView);
                if (downloaderTask != null && this == downloaderTask) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private ImageDownloaderTask getImageDownloadTask(ImageView imageView) {
        if (imageView != null) {
            WeakReference<ImageDownloaderTask> weakReference = (WeakReference<ImageDownloaderTask>) imageView.getTag();
            if (weakReference != null) {
                return weakReference.get();
            }
        }

        return null;
    }

    private Bitmap downloadBitmap(String url) {
        final AndroidHttpClient androidHttpClient = AndroidHttpClient.newInstance("ImageLoader");
        final HttpGet getRequest = new HttpGet(url);
        Bitmap bitmap = null;
        try {
            HttpResponse response = androidHttpClient.execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpEntity = response.getEntity();

                if (httpEntity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = httpEntity.getContent();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        httpEntity.consumeContent();
                    }
                }

            } else {
                MyLogger.w(LOG_TAG,"Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;
            }
        } catch (IOException exception) {
            getRequest.abort();
            MyLogger.w(LOG_TAG,"I/O error while retrieving bitmap from " + url, exception);
        } catch (IllegalStateException e) {
            getRequest.abort();
            MyLogger.w(LOG_TAG,"Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
            MyLogger.w(LOG_TAG,"Error while retrieving bitmap from " + url, e);
        } finally {
            androidHttpClient.close();
        }

        return bitmap;
    }
}