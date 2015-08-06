package cn.cc.sdk.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.cc.sdk.demo.R;
import cn.cc.sdk.demo.LocalApplication;
import cn.cc.sdk.demo.util.DirectoryNode;
import cn.cc.sdk.demo.util.MyLogger;
import cn.cc.sdk.demo.util.Utils;

/**
 * Created by shangsong on 15-4-3.
 */
public class DirectoryAdapter extends BaseAdapter {
    private LayoutInflater mInflater;

    private DirectoryNode mCurrentNode;
    private static final String LOG_TAG = DirectoryAdapter.class.getSimpleName();

    public DirectoryAdapter(Context activityContext, DirectoryNode node) {
        mCurrentNode = node;
        mInflater = LayoutInflater.from(activityContext);
    }

    @Override
    public int getCount() {
        return mCurrentNode.mChildrenList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCurrentNode.mChildrenList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            final DirectoryNode selectedDirectoryNode = mCurrentNode.mChildrenList.get(position);
            DirectoryViewHolder holder;
            View v = convertView;

            Context context = LocalApplication.getAppContext();

        /* If view not created */
            if (v == null) {
                v = mInflater.inflate(R.layout.directory_view_item, parent, false);
                holder = new DirectoryViewHolder();
                holder.layout = v.findViewById(R.id.layout_item);
                holder.title = (TextView) v.findViewById(R.id.title);
                holder.title.setSelected(true);
                Utils.setAlignModeByPref(0, holder.title);
                holder.text = (TextView) v.findViewById(R.id.text);
                holder.icon = (ImageView) v.findViewById(R.id.dvi_icon);
                holder.checkbox = (ImageView) v.findViewById(R.id.checkbox);
                v.setTag(holder);
            } else
                holder = (DirectoryViewHolder) v.getTag();

            String holderText = "";
            holder.title.setText(selectedDirectoryNode.getVisibleName());

            if (selectedDirectoryNode.mName.equals(".."))
                holderText = context.getString(R.string.parent_folder);
            else if (!selectedDirectoryNode.isFile()) {
                int folderCount = selectedDirectoryNode.subfolderCount();
                int mediaFileCount = selectedDirectoryNode.subfilesCount();
                holderText = "";

                if (folderCount > 0)
                    holderText += context.getResources().getQuantityString(
                            R.plurals.subfolders_quantity, folderCount, folderCount
                    );
                if (folderCount > 0 && mediaFileCount > 0)
                    holderText += ", ";
                if (mediaFileCount > 0)
                    holderText += context.getResources().getQuantityString(
                            R.plurals.mediafiles_quantity, mediaFileCount,
                            mediaFileCount);
            }
            if ("".equals(holderText))
                holder.text.setVisibility(View.INVISIBLE);
            else {
                holder.text.setVisibility(View.VISIBLE);
                holder.text.setText(holderText);
            }
            if (selectedDirectoryNode.isFile())
                holder.icon.setImageResource(R.drawable.video_icon);
            else
                holder.icon.setImageResource(R.drawable.ic_menu_folder);

            if (isChildFile(position)) {
                holder.checkbox.setVisibility(View.VISIBLE);
                if (selectedDirectoryNode.isSelected) {
                    holder.checkbox.setSelected(true);
                } else {
                    holder.checkbox.setSelected(false);
                }

                holder.checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyLogger.i(LOG_TAG, "check box onclick,position=" + position);
                        if (v.isSelected()) {
                            v.setSelected(false);
                            selectedDirectoryNode.isSelected = false;
                        } else {
                            v.setSelected(true);
                            selectedDirectoryNode.isSelected = true;
                        }
                    }
                });
            } else {
                holder.checkbox.setVisibility(View.GONE);
            }

            return v;
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "getView Exception:", e);
        }

        return null;
    }

    public void setData(DirectoryNode node) {
        try {
            mCurrentNode = node;
            notifyDataSetChanged();
        } catch (Exception e) {
            MyLogger.i(LOG_TAG, "setData exception");
        }
    }

    public void onItemClicked(View view, int position) {
        DirectoryNode node = mCurrentNode.mChildrenList.get(position);
        boolean newStatus = !node.isSelected;
        node.isSelected = newStatus;
        ImageView checkbox = (ImageView) view.findViewById(R.id.checkbox);
        checkbox.setSelected(newStatus);
    }

    private static class DirectoryViewHolder {
        View layout;
        TextView title;
        TextView text;
        ImageView icon;
        ImageView checkbox;
    }

    public boolean isChildFile(int position) {
        DirectoryNode selectedDirectoryNode = mCurrentNode.mChildrenList.get(position);
        return selectedDirectoryNode.isFile;
    }


}
