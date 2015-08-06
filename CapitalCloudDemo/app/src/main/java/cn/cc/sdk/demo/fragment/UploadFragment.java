package cn.cc.sdk.demo.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.cc.android.sdk.UploadManager;
import cn.cc.android.sdk.callback.UploadListener;
import cn.cc.android.sdk.impl.UploadData;
import cn.cc.android.sdk.impl.UploadTask;
import cn.cc.android.sdk.util.Logger;
import cn.cc.sdk.demo.LocalApplication;
import cn.cc.sdk.demo.VideoService;
import cn.cc.sdk.demo.adapter.UploadAdapter;
import cn.cc.sdk.demo.util.MyLogger;
import cn.cc.sdk.demo.util.Utils;
import cn.cc.sdk.demo.R;

public class UploadFragment extends Fragment {
    private ListView mListView;
    private List<UploadTask> mTaskList;
    private UploadManager mUploadManager;
    private UploadAdapter mUploadAdapter;

    private final int UPLOAD_SINGLE = 0;
    private final int UPLOAD_MULTIPLE = 1;
    private final String LOG_TAG = UploadFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upload, container, false);

        mListView = (ListView) root.findViewById(R.id.upload_listview);


        Button uploadSingle = (Button) root.findViewById(R.id.upload);
        uploadSingle.setOnClickListener(mOnClickListener);

        Button uploadMultiple = (Button) root.findViewById(R.id.upload_multiple);
        uploadMultiple.setOnClickListener(mOnClickListener);

        Button cancelAll = (Button) root.findViewById(R.id.cancel_all);
        cancelAll.setOnClickListener(mOnClickListener);

        return root;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.upload:
                    uploadSingle();
                    break;
                case R.id.upload_multiple:
                    uploadMultiple();
                    break;
                case R.id.cancel_all:
                    cancelAll();
                    break;
                default:
                    break;
            }
        }
    };

    private void uploadSingle() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, UPLOAD_SINGLE);
    }

    private void uploadMultiple() {
        Intent intent = new Intent(getActivity(), DirectoryBrowser.class);
        startActivityForResult(intent, UPLOAD_MULTIPLE);
    }

    private void cancelAll() {
        if (mUploadManager != null)
            mUploadManager.cancelAll();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LocalApplication application = (LocalApplication) getActivity().getApplication();
        application.getVideoService(new LocalApplication.ServiceListener() {
            @Override
            public void onServiceDisconnected(VideoService service) {
                mUploadManager = service.getUploadManager();

                mTaskList = mUploadManager.getTaskList();
                for (UploadTask task : mTaskList) {
                    task.setUploadListener(mUploadListener);
                }

                mUploadAdapter = new UploadAdapter(getActivity(), mTaskList);
                mUploadAdapter.setUploadManager(mUploadManager);
                mListView.setAdapter(mUploadAdapter);
                MyLogger.i(LOG_TAG, "onServiceDisconnected");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        MyLogger.i(LOG_TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Logger.i(LOG_TAG, "onDestroy");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    UploadListener mUploadListener = new UploadListener() {
        @Override
        public void onSuccess(JSONObject response) {
            MyLogger.i(LOG_TAG, "Upload onSuccess response=" + response);
            mUploadAdapter.notifyDataSetChanged();
            Utils.showToast(getActivity(), "上传成功");
        }

        @Override
        public void onProgressChanged(int progress) {
            mUploadAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStatusChanged(int status) {
            mUploadAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFailure(Exception e) {
            mUploadAdapter.notifyDataSetChanged();
            Utils.showToast(getActivity(), "上传失败");
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case UPLOAD_SINGLE:
                Uri uri = data.getData();
                startUpload(uri);
                break;
            case UPLOAD_MULTIPLE:
                startUpload(data.getStringArrayListExtra("paths"));
                break;
            default:
                break;
        }
    }

    private void startUpload(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return;
        }

        if (cursor.moveToFirst()) {
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));
            UploadData uploadData = new UploadData(title, title, new File(data));
            uploadData.setUploadListener(mUploadListener);
            mUploadManager.upload(uploadData);
        }

        cursor.close();
        mUploadAdapter.notifyDataSetChanged();
    }

    private void startUpload(ArrayList<String> paths) {
        List<UploadData> uploadDataList = new ArrayList<UploadData>(paths.size());
        for (String path : paths) {
            File file = new File(path);
            String name = file.getName();
            name = name.substring(0, name.indexOf('.'));
            UploadData uploadData = new UploadData(name, name, file);
            uploadData.setUploadListener(mUploadListener);
            uploadDataList.add(uploadData);
        }

        mUploadManager.upload(uploadDataList);
        mUploadAdapter.notifyDataSetChanged();
    }

}
