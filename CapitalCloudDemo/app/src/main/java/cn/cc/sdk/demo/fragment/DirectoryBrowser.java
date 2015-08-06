package cn.cc.sdk.demo.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import cn.cc.sdk.demo.LocalApplication;
import cn.cc.sdk.demo.R;
import cn.cc.sdk.demo.adapter.DirectoryAdapter;
import cn.cc.sdk.demo.interfaces.IRefreshable;
import cn.cc.sdk.demo.util.AndroidDevices;
import cn.cc.sdk.demo.util.DirectoryNode;
import cn.cc.sdk.demo.util.Extensions;
import cn.cc.sdk.demo.util.MyLogger;
import cn.cc.sdk.demo.util.Utils;

public class DirectoryBrowser extends Activity implements IRefreshable, SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {
    private final String LOG_TAG = DirectoryBrowser.class.getSimpleName();
    private DirectoryAdapter mDirectoryAdapter;
    private ListView mListView;
    private DirectoryNode mRootNode;
    private DirectoryNode mCurrentNode;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            MyLogger.i(LOG_TAG, "onCreate");
            setContentView(R.layout.directory_view);

            initView();
            setData();
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "Exceptino:", e);
        }
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.directory_list);
        mListView.setOnItemClickListener(this);
        mListView.requestFocus();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                MyLogger.i(LOG_TAG, "onScrollStateChanged");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                MyLogger.i(LOG_TAG, "onScroll");
                mSwipeRefreshLayout.setEnabled(firstVisibleItem == 0);
            }
        });

        Button uploadBtn = (Button) findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> paths = mCurrentNode.getSelectedPaths();
                if (paths.size() == 0) {
                    Utils.showToast(DirectoryBrowser.this, R.string.no_file_selected);
                } else {
                    MyLogger.i(LOG_TAG, "setResult");
                    Intent intent = new Intent();
                    intent.putExtra("paths", paths);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });

    }

    private void setData() {
        createStorageDirNode();

        mDirectoryAdapter = new DirectoryAdapter(this, mRootNode);
        mListView.setAdapter(mDirectoryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addDataScheme("file");
        registerReceiver(messageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(messageReceiver);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED) ||
                    action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED) ||
                    action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT) ||
                    action.equalsIgnoreCase(Intent.ACTION_MEDIA_REMOVED)) {
                refresh();
            }
        }
    };

    @Override
    public void refresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mCurrentNode.mChildrenList.clear();
        new PopulateNodeTask().execute();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DirectoryNode selectedNode = mCurrentNode.mChildrenList.get(position);
        if (selectedNode.isFile()) {
            mDirectoryAdapter.onItemClicked(view, position);
        } else {
            if (!selectedNode.mName.equals("..")) {
                updateData(selectedNode);
            } else {
                updateData(mCurrentNode.mParent);

            }


        }

    }

    private void updateData(DirectoryNode currentNode) {
        mCurrentNode = currentNode;
        new PopulateNodeTask().execute();
    }


    private void createStorageDirNode() {
        mRootNode = new DirectoryNode("root");
        mCurrentNode = mRootNode;
        new PopulateNodeTask().execute();
    }

    //生成rootNode的儿子和孙子节点
    private void populateNode(DirectoryNode rootNode, int depth) {
        //如果已经生成儿子节点，不再生成
        if (rootNode.mChildrenList.size() > 0) {
            for (DirectoryNode node : rootNode.mChildrenList) {
                if (!node.mName.equals("..") && (node.mChildrenList.size() == 0)) {
                    populateNode(node, depth + 1);
                }
            }
            return;
        }

        String path = rootNode.mPath;
        if (path == null || path.trim().isEmpty()) {
            ArrayList<String> storageList = AndroidDevices.getStorageDirectories();
            for (String storage : storageList) {
                File file = new File(storage);
                DirectoryNode subDirectoryNode = new DirectoryNode(file.getName(), getVisibleName(file));
                subDirectoryNode.mPath = storage;
                subDirectoryNode.mParent = rootNode;
                populateNode(subDirectoryNode, depth + 1);
                rootNode.mChildrenList.add(subDirectoryNode);
            }

            return;
        }

        File rootDir = new File(path);
        if (!rootDir.isDirectory()) {
            MyLogger.i(LOG_TAG, "rootDir is not directory,path=" + path);
            return;
        }

        File[] subFiles = rootDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File subFile : subFiles) {
                String fileName = subFile.getName();
                if (fileName.equals(".") || fileName.equals("..") || fileName.startsWith(".")) {
                    continue;
                }

                //生成儿子节点
                DirectoryNode subNode = new DirectoryNode(subFile.getName());
                StringBuilder builder = new StringBuilder(path);
                builder.append("/").append(subNode.mName);
                subNode.mPath = builder.toString();

                if (subFile.isFile()) {
                    if (acceptedPath(subFile.getPath())) {
                        subNode.setIsFile();
                    } else {
                        continue;
                    }
                } else {
                    //生成孙子节点
                    if (depth < 1) {
                        populateNode(subNode, depth + 1);
                    }
                }
                subNode.mParent = rootNode;
                rootNode.addChildNode(subNode);
            }

            Collections.sort(rootNode.mChildrenList);
        }

        DirectoryNode upNode = new DirectoryNode("..");
        upNode.mParent = rootNode;
        rootNode.mChildrenList.add(0, upNode);
    }

    private String getVisibleName(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (file.equals(Environment.getExternalStorageDirectory())) {
                return LocalApplication.getAppContext().getString(R.string.internal_memory);
            }
        }

        return file.getName();
    }

    public boolean acceptedPath(String path) {
        final StringBuilder sb = new StringBuilder();
        sb.append(".+(\\.)((?i)(");
        boolean first = true;
        for (String ext : Extensions.VIDEO) {
            if (!first)
                sb.append('|');
            else
                first = false;
            sb.append(ext.substring(1));
        }
        sb.append("))");
        String str = sb.toString();

        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE).matcher(path).matches();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentNode != mRootNode) {
            updateData(mCurrentNode.mParent);
        } else {
            super.onBackPressed();
        }

    }

    private class PopulateNodeTask extends AsyncTask {
        private LoadingFragment mLoadingFragment;

        @Override
        protected void onPreExecute() {
            try {
                if (mLoadingFragment == null) {
                    mLoadingFragment = new LoadingFragment();
                    mLoadingFragment.setMessage(getResources().getString(R.string.loading));
                }
                mLoadingFragment.show(getFragmentManager(), "Loading");
            } catch (Exception e) {
                MyLogger.w(LOG_TAG, "onPreExecute", e);
            }

        }

        @Override
        protected Object doInBackground(Object[] params) {
            populateNode(mCurrentNode, 0);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mDirectoryAdapter.setData(mCurrentNode);
            mLoadingFragment.dismiss();

            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }


}
