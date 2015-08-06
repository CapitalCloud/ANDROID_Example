package cn.cc.sdk.demo.util;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by shangsong on 15-4-8.
 */
public class DirectoryNode implements Comparable<DirectoryNode> {
    public String mName;
    public String mVisibleName;
    public String mPath;
    public boolean isFile;
    public boolean isSelected;

    public DirectoryNode mParent;
    public ArrayList<DirectoryNode> mChildrenList;

    public DirectoryNode(String name) {
        this(name, null);
    }

    public DirectoryNode(String name, String visibleName) {
        mName = name;
        mVisibleName = visibleName;
        isFile = false;
        mParent = null;
        mChildrenList = new ArrayList<DirectoryNode>();
        isSelected = false;
    }

    public void addChildNode(DirectoryNode n) {
        n.mParent = this;
        mChildrenList.add(n);
    }

    public DirectoryNode getChildNode(String directoryName) {
        for (DirectoryNode directoryNode : mChildrenList) {
            if (directoryNode.mName.equals(directoryName)) {
                return directoryNode;
            }
        }

        DirectoryNode directoryNode = new DirectoryNode(directoryName);
        addChildNode(directoryNode);

        return directoryNode;
    }

    public Boolean isFile() {
        return this.isFile;
    }

    public void setIsFile() {
        this.isFile = true;
    }

    public Boolean existsChild(String name) {
        for (DirectoryNode directoryNode : mChildrenList) {
            if (Utils.nullEquals(directoryNode.mName, name)) {
                return true;
            }
        }

        return false;
    }

    public int getChildPosition(DirectoryNode child) {
        if (child == null) {
            return -1;
        }

        ListIterator<DirectoryNode> iterator = mChildrenList.listIterator();
        while (iterator.hasNext()) {
            DirectoryNode directoryNode = iterator.next();
            if (directoryNode.equals(child)) {
                return iterator.previousIndex();
            }

        }

        return -1;
    }

    public DirectoryNode ensureExists(String name) {
        for (DirectoryNode directoryNode : mChildrenList) {
            if (Utils.nullEquals(directoryNode.mName, name)) {
                return directoryNode;
            }
        }

        DirectoryNode directoryNode = new DirectoryNode(name);
        addChildNode(directoryNode);
        return directoryNode;
    }

    public int subfolderCount() {
        int count = 0;
        for (DirectoryNode directoryNode : mChildrenList) {
            if (!directoryNode.isFile && !directoryNode.mName.equals("..")) {
                count++;
            }
        }

        return count;
    }

    public int subfilesCount() {
        int count = 0;
        for (DirectoryNode directoryNode : mChildrenList) {
            if (directoryNode.isFile) {
                count++;
            }
        }

        return count;
    }

    public String getVisibleName() {
        return (mVisibleName != null) ? mVisibleName : mName;
    }

    public ArrayList<DirectoryNode> getSelectedNodes() {
        ArrayList<DirectoryNode> selectedNodes = new ArrayList<DirectoryNode>();
        for (DirectoryNode node : mChildrenList) {
            if (node.isSelected) {
                selectedNodes.add(node);
            }
        }

        return selectedNodes;
    }

    public ArrayList<String> getSelectedPaths() {
        ArrayList<String> selectedNodes = new ArrayList<String>();
        for (DirectoryNode node : mChildrenList) {
            if (node.isSelected) {
                selectedNodes.add(node.mPath);
            }
        }

        return selectedNodes;
    }


    @Override
    public int compareTo(DirectoryNode another) {
        if (this.isFile && !another.isFile) {
            return 1;
        } else if (!this.isFile && another.isFile) {
            return -1;
        } else {
            return String.CASE_INSENSITIVE_ORDER.compare(mName, another.mName);
        }
    }
}
