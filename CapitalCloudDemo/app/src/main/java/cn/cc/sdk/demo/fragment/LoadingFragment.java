package cn.cc.sdk.demo.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cn.cc.sdk.demo.R;

/**
 * Created by shangsong on 15-4-14.
 */
public class LoadingFragment extends DialogFragment {
    private String mContent;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.loading, null);
        TextView contentView = (TextView) root.findViewById(R.id.content);
        contentView.setText(mContent);

        Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setContentView(root);

        return dialog;
    }

    public void setMessage(String content) {
        mContent = content;
    }

}
