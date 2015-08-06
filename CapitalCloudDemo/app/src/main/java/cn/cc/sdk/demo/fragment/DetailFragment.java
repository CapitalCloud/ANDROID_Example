package cn.cc.sdk.demo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.cc.android.sdk.util.VideoData;
import cn.cc.sdk.demo.R;
import cn.cc.sdk.demo.util.MyLogger;

public class DetailFragment extends Fragment {
    private VideoData mVideoData;
    private Callback mCallback;
    private ImageView mActivatedView;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();


    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DetailFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mVideoData = getArguments().getParcelable("data");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            initHeader(rootView.findViewById(R.id.detail_header));
            return rootView;
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "onActivityCreated Exception", e);
        }

        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            setData();
        } catch (Exception e) {
            MyLogger.i(LOG_TAG, "onActivityCreated Exception", e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    private void initHeader(View root) {
        ImageView downloadBtn = (ImageView) root.findViewById(R.id.image_download);
        downloadBtn.setOnClickListener(mOnClickListener);

        ImageView editBtn = (ImageView) root.findViewById(R.id.image_edit);
        editBtn.setOnClickListener(mOnClickListener);

        ImageView cancelBtn = (ImageView) root.findViewById(R.id.image_cancel);
        cancelBtn.setVisibility(View.GONE);

        ImageView completeBtn = (ImageView) root.findViewById(R.id.image_complete);
        completeBtn.setVisibility(View.GONE);
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
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
                case R.id.activated:
                    onActivatedStatusChanged(v);
                    break;
                default:
                    MyLogger.w(LOG_TAG, "OnClickListener Error!");
                    break;

            }
        }
    };

    public void onDownloadBtnClick() {
        if (mCallback != null) {
            mCallback.onDownloadBtnClick();
        }
    }

    public void onEditBtnClick() {
        if (mCallback != null) {
            mCallback.onEditBtnClick();
        }
    }

    public void onActivatedStatusChanged(View view) {
        MyLogger.i(LOG_TAG, "onActivatedStatusChanged onClick");
        boolean newStatus;
        if (view.isSelected()) {
            newStatus = false;
            view.setSelected(false);
            mVideoData.setActivated(false);
        } else {
            newStatus = true;
            view.setSelected(true);
            mVideoData.setActivated(true);
        }

        if (mCallback != null) {
            mCallback.onActivatedStatusChanged(newStatus);
        }
    }

    public void setData(VideoData data) {
        mVideoData = data;
    }

    private void setData() {
        TextView idView = (TextView) getActivity().findViewById(R.id.id_value);
        idView.setText(mVideoData.getId());

        TextView titleView = (TextView) getActivity().findViewById(R.id.title_value);
        titleView.setText(mVideoData.getTitle());

        TextView descriptionView = (TextView) getActivity().findViewById(R.id.description_value);
        descriptionView.setText(mVideoData.getDescription());

        StringBuilder builder = new StringBuilder();
        if (mVideoData.getTags() != null) {
            String[] tags = mVideoData.getTags();
            for (int index = 0; index < tags.length; index++) {
                if (index != 0) {
                    builder.append(',');
                }
                builder.append(tags[index]);
            }
        }
        TextView tagsView = (TextView) getActivity().findViewById(R.id.tags_value);
        tagsView.setText(builder);

        mActivatedView = (ImageView) getActivity().findViewById(R.id.activated);
        if (mVideoData.isActivated()) {
            mActivatedView.setTag(true);
            mActivatedView.setSelected(true);
        } else {
            mActivatedView.setTag(false);
            mActivatedView.setSelected(false);
        }
        mActivatedView.setOnClickListener(mOnClickListener);
    }

    public interface Callback {
        public void onDownloadBtnClick();

        public void onEditBtnClick();

        public void onActivatedStatusChanged(boolean status);
    }

}
