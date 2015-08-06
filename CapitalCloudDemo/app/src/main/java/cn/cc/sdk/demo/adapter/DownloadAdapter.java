package cn.cc.sdk.demo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cn.cc.android.sdk.DownloadManager;
import cn.cc.android.sdk.impl.DownloadTask;
import cn.cc.android.sdk.util.SDKConstants;
import cn.cc.android.sdk.view.PlayVideoActivity;
import cn.cc.android.sdk.view.VideoView;
import cn.cc.sdk.demo.util.MyLogger;
import cn.cc.sdk.demo.R;

/**
 * Created by shangsong on 14-9-23.
 */
public class DownloadAdapter extends BaseAdapter {
    private Context mContext;
    private List<DownloadTask> data = null;
    private LayoutInflater layoutInflater;
    private DownloadManager mDownloadManager;

    private static final int PROGRESS_MAX = 100;
    private final String LOG_TAG = this.getClass().getSimpleName();

    public DownloadAdapter(Context context, List<DownloadTask> data) {
        this.mContext = context;
        this.data = data;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data == null ? 0 : this.data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.download_listview_item, parent, false);

            holder = new ViewHolder();
            holder.videoTitle = (TextView) convertView.findViewById(R.id.download_title);
            holder.videoDefinition = (TextView) convertView.findViewById(R.id.download_defenition);

            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.download_progressbar);
            holder.btnStart = (Button) convertView.findViewById(R.id.download_start);
            holder.btnCancel = (Button) convertView.findViewById(R.id.download_cancel);
            holder.btnPlay = (Button) convertView.findViewById(R.id.download_play);
            holder.progressText = (TextView) convertView.findViewById(R.id.download_progress);
            holder.status = (TextView) convertView.findViewById(R.id.download_status);

            holder.progressBar.setMax(PROGRESS_MAX);

            holder.onClickListener = new MyOnClickListener();
            holder.btnStart.setOnClickListener(holder.onClickListener);
            holder.btnStart.setTag(true);
            holder.btnCancel.setOnClickListener(holder.onClickListener);
            holder.btnPlay.setOnClickListener(holder.onClickListener);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DownloadTask downloadTask = data.get(position);
        holder.videoTitle.setText(mContext.getString(R.string.video_title,
                downloadTask.getTitle()));
        holder.videoDefinition.setText(mContext.getString(R.string.video_defenition,
                getDefinition(downloadTask.getDefinition())));

        if (downloadTask.isRunning()) {
            holder.btnStart.setTag(true);
            holder.btnStart.setText(mContext.getString(R.string.pause));
        } else {
            holder.btnStart.setTag(false);
            holder.btnStart.setText(mContext.getString(R.string.start));
        }

        holder.onClickListener.setDownloadTask(downloadTask);
        holder.progressBar.setTag(downloadTask);
        holder.progressBar.setProgress(downloadTask.getProgress());
        holder.progressText.setText(mContext.getString(R.string.upload_progress, downloadTask.getProgress()) + "%");
        setStatus(holder, downloadTask.getDownloadStatus());

        return convertView;
    }

    private String getDefinition(String definition) {
        if (SDKConstants.DEFINITION_ANDROID_SMOOTH.equals(definition)) {
            return mContext.getString(R.string.str_smooth);
        } else if (SDKConstants.DEFINITION_ANDROID_STANDARD.equals(definition)) {
            return mContext.getString(R.string.str_standard);
        } else {
            return mContext.getString(R.string.str_hd);
        }
    }

    public void setData(List<DownloadTask> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    private void setStatus(ViewHolder holder, int status) {
        String text;
        switch (status) {
            case SDKConstants.STATUS_PENDING:
                text = "PENDING";
                holder.btnPlay.setClickable(false);
                break;
            case SDKConstants.STATUS_RUNNING:
                text = "RUNNING";
                break;
            case SDKConstants.STATUS_STOP_BY_USER:
            case SDKConstants.STATUS_AUTO_STOP:
                text = "STOP";
                break;
            case SDKConstants.STATUS_SUCCESS:
                text = "SUCCESS";
                holder.btnPlay.setClickable(true);
                break;
            case SDKConstants.STATUS_CANCELLED:
                text = "CANCEL";
                break;
            case SDKConstants.STATUS_FAILURE:
                text = "ERROR";
                break;
            case SDKConstants.STATUS_WAIT_NETWORK:
                text = "WAIT_NETWORK";
                break;

            default:
                text = "WAIT";
                holder.btnPlay.setClickable(false);
                break;
        }

        holder.status.setText(mContext.getString(R.string.upload_status, text));
    }

    private class MyOnClickListener implements View.OnClickListener {
        private DownloadTask downloadTask;

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.download_start:
                    boolean tag = !(Boolean) view.getTag();
                    if (tag) {
                        MyLogger.w(LOG_TAG, "start,");
                        downloadTask = mDownloadManager.download(downloadTask.getDownloadData());
                        ((Button) view).setText(mContext.getString(R.string.pause));
                    } else {
                        mDownloadManager.stop(downloadTask);
                        ((Button) view).setText(mContext.getString(R.string.start));
                    }

                    view.setTag(tag);
                    break;
                case R.id.download_cancel:
                    mDownloadManager.cancel(downloadTask);
                    MyLogger.w(LOG_TAG, "download_cancel,");
                    break;

                case R.id.download_play:
                    Intent intent = new Intent();
                    intent.setClass(mContext, PlayVideoActivity.class);
                    intent.putExtra(SDKConstants.KEY_AUTO_START, true);
                    intent.putExtra(SDKConstants.KEY_PLAY_MODE, VideoView.PLAY_MODE_OFFLINE);
                    intent.putExtra(SDKConstants.KEY_VIDEO_DIRECTORY, downloadTask.getStorageDirectory().getAbsolutePath());
                    intent.putExtra(SDKConstants.KEY_SHOW_PROGRESS_BAR, true);
                    mContext.startActivity(intent);
                    break;
                default:
                    break;
            }
        }

        public void setDownloadTask(DownloadTask downloadTask) {
            this.downloadTask = downloadTask;
        }
    }

    public void setDownloadManager(DownloadManager downloadManager) {
        this.mDownloadManager = downloadManager;
    }

    private final class ViewHolder {
        TextView videoTitle;
        TextView videoDefinition;
        Button btnStart;
        Button btnCancel;
        Button btnPlay;
        TextView progressText;
        TextView status;
        ProgressBar progressBar;
        MyOnClickListener onClickListener;
    }

}
