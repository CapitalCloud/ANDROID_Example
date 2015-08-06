package cn.cc.sdk.demo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.cc.android.sdk.util.Logger;
import cn.cc.sdk.demo.R;

public class TitleFragment extends Fragment {
    private static final int TYPE_DISPLAY = 0;
    private static final int TYPE_EDIT = 1;

    private ImageView mEditBtn;
    private ImageView mCancelBtn;
    private ImageView mCompleteBtn;
    private TitleFragmentCallback mCallback;
    private static final String LOG_TAG = "TitleFragment";

    public TitleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Logger.i(LOG_TAG, "onAttach");
        try {
            mCallback = (TitleFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEditBtnClickedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.i(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.header_layout, container, false);
        initView(rootView, TYPE_DISPLAY);

        return rootView;
    }

    private void initView(View root, int type) {
        ImageView downloadBtn = (ImageView) root.findViewById(R.id.image_download);
        downloadBtn.setOnClickListener(mListener);

        mEditBtn = (ImageView) root.findViewById(R.id.image_edit);
        mEditBtn.setVisibility(TYPE_DISPLAY == type ? View.VISIBLE : View.GONE);
        mEditBtn.setOnClickListener(mListener);

        mCancelBtn = (ImageView) root.findViewById(R.id.image_cancel);
        mCancelBtn.setVisibility(TYPE_DISPLAY == type ? View.GONE : View.VISIBLE);
        mCancelBtn.setOnClickListener(mListener);

        mCompleteBtn = (ImageView) root.findViewById(R.id.image_complete);
        mCompleteBtn.setVisibility(TYPE_DISPLAY == type ? View.GONE : View.VISIBLE);
        mCompleteBtn.setOnClickListener(mListener);
    }

    private void refreshView(int type) {
        Logger.i(LOG_TAG, "refreshView type=" + type);
        mEditBtn.setVisibility(TYPE_DISPLAY == type ? View.VISIBLE : View.GONE);
        mCancelBtn.setVisibility(TYPE_DISPLAY == type ? View.GONE : View.VISIBLE);
        mCompleteBtn.setVisibility(TYPE_DISPLAY == type ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.i(LOG_TAG, "onCreateView");
        mListener = null;
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.image_download:
                    onDownloadBtnClick();
                    break;
                case R.id.image_edit:
                    onEditBtnClick();
                    break;
                case R.id.image_cancel:
                    onCancelBtnClick();
                    break;
                case R.id.image_complete:
                    onCompleteBtnClick();
                    break;
            }
        }
    };

    public void onUploadBtnClick() {
        if (mCallback != null) {
            mCallback.onUploadBtnClick();
        }
    }

    public void onDownloadBtnClick() {
        if (mCallback != null) {
            mCallback.onDownloadBtnClick();
        }
    }

    public void onEditBtnClick() {
        refreshView(TYPE_EDIT);
        if (mCallback != null) {
            mCallback.onEditBtnClick();
        }
    }

    public void onCancelBtnClick() {
        refreshView(TYPE_DISPLAY);
        if (mCallback != null) {
            mCallback.onCancelBtnClick();
        }
    }

    public void onCompleteBtnClick() {
        refreshView(TYPE_DISPLAY);
        if (mCallback != null) {
            mCallback.onCompleteBtnClick();
        }
    }

    public interface TitleFragmentCallback {
        public void onEditBtnClick();

        public void onCancelBtnClick();

        public void onCompleteBtnClick();

        public void onUploadBtnClick();

        public void onDownloadBtnClick();
    }


}
