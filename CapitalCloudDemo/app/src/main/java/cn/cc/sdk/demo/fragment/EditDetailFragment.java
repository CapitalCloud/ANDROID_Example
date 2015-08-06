package cn.cc.sdk.demo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.cc.android.sdk.util.VideoData;
import cn.cc.sdk.demo.R;
import cn.cc.sdk.demo.util.MyLogger;

public class EditDetailFragment extends Fragment {
    private TextView mIdView;
    private EditText mTitleView;
    private EditText mDescriptionView;
    private EditText mTagsView;

    private VideoData mVideoData;
    private Callback mCallback;

    private static final String LOG_TAG = EditDetailFragment.class.getSimpleName();

    public EditDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
        View view = inflater.inflate(R.layout.fragment_edit_detail, container, false);
        initHeader(view.findViewById(R.id.edit_detail_header));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setData();
    }

    @Override
    public void onDetach() {
        MyLogger.i(LOG_TAG, "onDetach");
        super.onDetach();
    }

    private void initHeader(View root) {
        ImageView downloadBtn = (ImageView) root.findViewById(R.id.image_download);
        downloadBtn.setVisibility(View.GONE);

        ImageView editBtn = (ImageView) root.findViewById(R.id.image_edit);
        editBtn.setVisibility(View.GONE);


        ImageView cancelBtn = (ImageView) root.findViewById(R.id.image_cancel);
        cancelBtn.setOnClickListener(mListener);
        cancelBtn.setVisibility(View.VISIBLE);

        ImageView completeBtn = (ImageView) root.findViewById(R.id.image_complete);
        completeBtn.setOnClickListener(mListener);
        completeBtn.setVisibility(View.VISIBLE);
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.image_cancel:
                    onCancelBtnClick();
                    break;
                case R.id.image_complete:
                    onCompleteBtnClick();
                    break;

                default:
                    MyLogger.w(LOG_TAG, "OnClickListener Error!");
                    break;

            }
        }
    };

    public void onCancelBtnClick() {
        if (mCallback != null) {
            mCallback.onCancelBtnClick();
        }
    }

    public void onCompleteBtnClick() {
        getData();

        if (mCallback != null) {
            mCallback.onCompleteBtnClick(mVideoData);
        }
    }

    public void setData(VideoData data) {
        mVideoData = data;
    }

    private void setData() {
        mIdView = (TextView) getActivity().findViewById(R.id.edit_id_value);
        mIdView.setText(mVideoData.getId());

        mTitleView = (EditText) getActivity().findViewById(R.id.edit_title_value);
        mTitleView.setText(mVideoData.getTitle());

        mDescriptionView = (EditText) getActivity().findViewById(R.id.edit_description_value);
        mDescriptionView.setText(mVideoData.getDescription());

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
        mTagsView = (EditText) getActivity().findViewById(R.id.edit_tags_value);
        mTagsView.setText(builder);
    }

    private void getData() {
        mVideoData.setTitle(mTitleView.getText().toString().trim());
        mVideoData.setDescription(mDescriptionView.getText().toString().trim());
        String[] tags = mTagsView.getText().toString().trim().split(",");
        mVideoData.setTags(tags);
    }


    public interface Callback {
        public void onCancelBtnClick();

        public void onCompleteBtnClick(VideoData data);
    }
}
