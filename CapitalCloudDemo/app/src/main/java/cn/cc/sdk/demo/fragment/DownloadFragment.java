package cn.cc.sdk.demo.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.cc.android.sdk.DownloadManager;
import cn.cc.android.sdk.callback.DownloadListener;
import cn.cc.android.sdk.impl.DownloadTask;
import cn.cc.sdk.demo.LocalApplication;
import cn.cc.sdk.demo.R;
import cn.cc.sdk.demo.VideoService;
import cn.cc.sdk.demo.adapter.DownloadAdapter;
import cn.cc.sdk.demo.util.MyLogger;

public class DownloadFragment extends Fragment {
    private Button mStartAllBtn;
    private ListView mListView;
    private DownloadManager mDownloadManager;
    private DownloadAdapter mDownloadAdapter;
    private List<DownloadTask> mDownloadList;

    private final String LOG_TAG = DownloadFragment.class.getSimpleName();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_download, container, false);

        mListView = (ListView) root.findViewById(R.id.download_listview);
        mStartAllBtn = (Button) root.findViewById(R.id.download_start_all);
        mStartAllBtn.setTag(false);
        mStartAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newStatus = !(Boolean) v.getTag();
                if (newStatus) {
                    mDownloadManager.downloadAll();
                    ((TextView) v).setText(getResources().getString(R.string.stop_all));
                } else {
                    mDownloadManager.stopAll();
                    ((TextView) v).setText(getResources().getString(R.string.start_all));
                }
                v.setTag(newStatus);
            }

        });

        Button cancelAllBtn = (Button) root.findViewById(R.id.download_cancel_all);
        cancelAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManager.cancelAll();
            }

        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LocalApplication application = (LocalApplication) getActivity().getApplication();
        application.getVideoService(new LocalApplication.ServiceListener() {
            @Override
            public void onServiceDisconnected(VideoService service) {
                mDownloadManager = service.getDownloadManager();

                mDownloadList = mDownloadManager.getTaskList();
                for (DownloadTask task : mDownloadList) {
                    task.setDownloadListener(mDownloadListener);
                }

                mDownloadAdapter = new DownloadAdapter(getActivity(), mDownloadList);
                mDownloadAdapter.setDownloadManager(mDownloadManager);
                mListView.setAdapter(mDownloadAdapter);

                if (mDownloadManager.isRunning()) {
                    mStartAllBtn.setTag(true);
                    mStartAllBtn.setText(getResources().getString(R.string.stop_all));
                } else {
                    mStartAllBtn.setTag(false);
                    mStartAllBtn.setText(getResources().getString(R.string.start_all));
                }
            }
        });

    }

    DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onSuccess() {
            mDownloadAdapter.notifyDataSetChanged();
            MyLogger.i(LOG_TAG, "onSuccess+++++++++++++++");
            showToast("下载成功");
        }

        @Override
        public void onProgressChanged(int progress) {
            mDownloadAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStatusChanged(int status) {
            mDownloadAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFailure(Exception e) {
            mDownloadAdapter.notifyDataSetChanged();
            showToast("下载失败");
        }
    };

    public void showToast(String content) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
        }
    }


}
