package cn.cc.sdk.demo;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Formatter;

import cn.cc.android.sdk.DownloadManager;
import cn.cc.android.sdk.VideoManager;
import cn.cc.android.sdk.callback.DownloadListener;
import cn.cc.android.sdk.callback.ResponseListener;
import cn.cc.android.sdk.impl.DownloadData;
import cn.cc.android.sdk.impl.SimplePlayerProperty;
import cn.cc.android.sdk.impl.TransCodingInformation;
import cn.cc.android.sdk.util.SDKConstants;
import cn.cc.android.sdk.util.VideoData;
import cn.cc.android.sdk.view.VideoView;
import cn.cc.sdk.demo.fragment.DetailFragment;
import cn.cc.sdk.demo.fragment.EditDetailFragment;
import cn.cc.sdk.demo.util.MyLogger;

public class PlayActivity extends Activity implements DetailFragment.Callback,
        EditDetailFragment.Callback {
    private VideoData mVideoData;
    private VideoService mVideoService;
    private VideoView mVideoView;
    private TextView mProgressView;
    private static TransCodingInformation mInformation;
    private ProgressReceiver mProgressRecevier;
    private ArrayList<SimplePlayerProperty> mPlayerList;

    private String mVideoId1;
    private String mVideoId2;
    private String mVideoId3;
    private DefinitionDialog mDialog;
    private DetailFragment mDetailFragment;
    private EditDetailFragment mEditDetailFragment;

    private static final String LOG_TAG = PlayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_play_video);

            mProgressView = (TextView) findViewById(R.id.display_progress);
            String text = getString(R.string.current_play_progress, formatTime(0));
            mProgressView.setText(text);
            initControllView();

            Intent intent = getIntent();
            initChangeVideoView(intent);
            mVideoData = intent.getExtras().getParcelable("data");
            LocalApplication application = (LocalApplication) this.getApplication();
            application.getVideoService(new LocalApplication.ServiceListener() {
                @Override
                public void onServiceDisconnected(VideoService service) {
                    mVideoService = service;
                    initPlayer();
                    initFragment();
                    registerReceiver();
                }
            });
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "Exception:", e);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mProgressRecevier != null)
            unregisterReceiver(mProgressRecevier);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            if (mVideoView != null) {
                if (mVideoView.rotatedFromBtn()) {
                    return;
                } else {
                    if (mVideoView.isFullScreen()) {
                        mVideoView.toMiniScreen();
                    } else {
                        mVideoView.toFullScreen();
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "onConfigurationChanged exception");
        }
    }

    private void initFragment() {
        mDetailFragment = new DetailFragment();
        Bundle bundle = new Bundle(1);
        bundle.putParcelable("data", mVideoData);
        mDetailFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, mDetailFragment);
        transaction.commit();
    }


    @Override
    public void onEditBtnClick() {
        try {
            if (mEditDetailFragment == null) {
                mEditDetailFragment = new EditDetailFragment();
                Bundle bundle = new Bundle(1);
                bundle.putParcelable("data", mVideoData);
                mEditDetailFragment.setArguments(bundle);
            } else {
                mEditDetailFragment.setData(mVideoData);
            }

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            transaction.replace(R.id.fragment_container, mEditDetailFragment, "edit");
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "onConfigurationChanged exception", e);
        }
    }

    @Override
    public void onCancelBtnClick() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onCompleteBtnClick(VideoData videoData) {
        try {
            mVideoService.update(mVideoData, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    mVideoData = getVideoData(response);

                    if (mDetailFragment == null) {
                        mDetailFragment = new DetailFragment();
                    }
                    mDetailFragment.setData(mVideoData);

                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(android.R.animator.fade_in,
                            android.R.animator.fade_out);
                    transaction.replace(R.id.fragment_container, mDetailFragment, "detail");
                    transaction.commit();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    MyLogger.e(LOG_TAG, "onErrorResponse:", throwable);
                }
            });
        } catch (Exception e) {
            MyLogger.i(LOG_TAG, "exception:", e);
        }

    }

    @Override
    public void onDownloadBtnClick() {
        if (mDialog != null && mDialog.isAdded()) {
            mDialog.dismiss();
            return;
        }

        if (!mVideoData.isActivated()) {
            Toast.makeText(PlayActivity.this, getString(R.string.deactivate_prompt), Toast.LENGTH_SHORT).show();
            return;
        }

        mVideoService.json(mVideoData.getId(), new ResponseListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            mInformation = mVideoView.getTranscodingInformation();
                            String[] definitions = mInformation.getDefinitionArray(PlayActivity.this);

                            if (mDialog == null) {
                                mDialog = DefinitionDialog.newInstance(getString(R.string.select_definition), definitions);
                            }
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            mDialog.show(transaction, "dialog");
                        } catch (Exception e) {
                            MyLogger.w(LOG_TAG, "onDownloadBtnClick JSONException", e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                    }
                }
        );

    }

    DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onSuccess() {
            showToast("下载成功");
        }

        @Override
        public void onProgressChanged(int progress) {
        }

        @Override
        public void onStatusChanged(int status) {
//            showToast("状态改变：" + status);
        }

        @Override
        public void onFailure(Exception e) {
            showToast("下载失败");
        }
    };


    @Override
    public void onActivatedStatusChanged(boolean status) {
        if (status) {
            mVideoService.getVideoManager().activate(mVideoData.getId());
        } else {
            mVideoService.getVideoManager().deactivate(mVideoData.getId());
        }
    }

    public void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void registerReceiver() {
        mProgressRecevier = new ProgressReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SDKConstants.ACTION_PLAY_PROGRESS_CHANGED);
        registerReceiver(mProgressRecevier, filter);
    }


    private class ProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SDKConstants.ACTION_PLAY_PROGRESS_CHANGED == action) {
                String id = intent.getStringExtra(SDKConstants.KEY_ID);
                String title = intent.getStringExtra(SDKConstants.KEY_TITLE);
                float progress = intent.getFloatExtra(SDKConstants.KEY_PROGRESS, 0);
                String text = getString(R.string.current_play_progress, formatTime(progress));
                mProgressView.setText(text);
            }

        }
    }

    private String formatTime(float time) {
        if (time < 60) {
            return time + "";
        }

        int seconds = (int) (time % 60);
        int minutes = (int) ((time % 3600) / 60);
        int hours = (int) (time / 3600);

        Formatter formatter = new Formatter();
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static class DefinitionDialog extends DialogFragment {
        private int index = 0;

        public static DefinitionDialog newInstance(String title, String[] definitions) {
            DefinitionDialog definitionDialog = new DefinitionDialog();
            Bundle bundle = new Bundle(2);
            bundle.putString("title", title);
            bundle.putStringArray("definitions", definitions);
            definitionDialog.setArguments(bundle);
            return definitionDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            final String[] definitions = getArguments().getStringArray("definitions");

            return new AlertDialog.Builder(getActivity()).setTitle(title)
                    .setSingleChoiceItems(definitions, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            index = which;
                        }
                    }).setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String definition = mInformation.getDefinitionEN(getActivity(), definitions[index]);
                                ((PlayActivity) getActivity()).download(definition);
                                dialog.dismiss();
                            } catch (Exception e) {
                                MyLogger.w(LOG_TAG, "fragment_download exception", e);
                            }

                        }
                    }).create();

        }
    }

    private void download(String definition) {
        try {
            DownloadManager downloadManager = mVideoService.getDownloadManager();

            String videoId = mVideoData.getId();
            DownloadData downloadData = new DownloadData(videoId, definition);
            downloadData.setDownloadListener(mDownloadListener);
            downloadManager.download(downloadData);
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "fragment_download exception", e);
        }
    }

    private void initPlayer() {
        mVideoService.getAllPlayers(new ResponseListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (mPlayerList != null)
                        mPlayerList.clear();
                    else
                        mPlayerList = new ArrayList<SimplePlayerProperty>();

                    JSONArray jsonArray = new JSONArray(response);
                    for (int index = 0; index < jsonArray.length(); index++) {
                        String item = jsonArray.getString(index);
                        Gson gson = new Gson();
                        SimplePlayerProperty player = gson.fromJson(item, SimplePlayerProperty.class);
                        mPlayerList.add(player);
                    }

                    play();
                } catch (JSONException e) {
                    MyLogger.e(LOG_TAG, "JSONException: ", e);
                }

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    private void play() {
        String videoId = mVideoData.getId();
        if (videoId == null || TextUtils.isEmpty(videoId)) {
            MyLogger.w(LOG_TAG, "videoId can't be null or empty");
            finish();
        }
        VideoManager videoManager = mVideoService.getVideoManager();
        String defaultDefinition = SDKConstants.DEFINITION_ANDROID_SMOOTH;

        mVideoView = (VideoView) findViewById(R.id.video_view);
        String playId = mPlayerList.get(0).getId();
        mVideoView.showProgressBar(true);
        mVideoView.playVideo(videoManager, videoId, playId, defaultDefinition);
    }

    public VideoData getVideoData(String response) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<VideoData>() {
            }.getType();
            return gson.fromJson(response, type);
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "parseJsonToObject error:", e);
        }

        return null;
    }

    public void initChangeVideoView(Intent intent) {
        Button btn1 = (Button) findViewById(R.id.change1);
        btn1.setOnClickListener(mOnClickListener);
        Button btn2 = (Button) findViewById(R.id.change2);
        btn2.setOnClickListener(mOnClickListener);
        Button btn3 = (Button) findViewById(R.id.change3);
        btn3.setOnClickListener(mOnClickListener);

        mVideoId1 = intent.getStringExtra("videoId1");
        mVideoId2 = intent.getStringExtra("videoId2");
        mVideoId3 = intent.getStringExtra("videoId3");
    }

    public void initControllView() {
        Button playBtn = (Button) findViewById(R.id.btn_play);
        playBtn.setOnClickListener(mOnClickListener);
        Button seekBtn = (Button) findViewById(R.id.btn_seek);
        seekBtn.setOnClickListener(mOnClickListener);

        Button btn = (Button) findViewById(R.id.btn_hide_progress_bar);
        btn.setTag(true);
        btn.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String videoId = null;
            String definition = null;
            int id = v.getId();
            switch (id) {
                case R.id.change1:
                    videoId = mVideoId1;
                    definition = SDKConstants.DEFINITION_ANDROID_HD;
                    break;
                case R.id.change2:
                    videoId = mVideoId2;
                    definition = SDKConstants.DEFINITION_ANDROID_HD;
                    break;
                case R.id.change3:
                    videoId = mVideoId3;
                    definition = SDKConstants.DEFINITION_ANDROID_HD;
                    break;
                case R.id.btn_play:
                    mVideoView.startOrPause();
                    break;
                case R.id.btn_seek:
                    mVideoView.seekToPosition(10000);
                    break;
                case R.id.btn_hide_progress_bar:
                    updateProgressBar(v);
                default:
                    break;
            }

            if (mVideoView != null && videoId != null)
                mVideoView.changeVideo(videoId, definition);

        }
    };

    private void updateProgressBar(View view) {
        boolean newStatus = !(Boolean) view.getTag();
        MyLogger.i(LOG_TAG, "updateProgressBar,newStatus=" + newStatus);
        mVideoView.showProgressBar(newStatus);
        view.setTag(newStatus);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null)
            mVideoView.onActivityPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null)
            mVideoView.onActivityResume();

    }

}

