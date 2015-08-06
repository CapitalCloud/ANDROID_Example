package cn.cc.sdk.demo.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by shangsong on 15-4-7.
 */
public class AndroidDevices {
    private static final String LOG_TAG = AndroidDevices.class.getSimpleName();

    public static ArrayList<String> getStorageDirectories() {
        BufferedReader bufReader = null;
        ArrayList<String> list = new ArrayList<String>();
        list.add(Environment.getExternalStorageDirectory().getPath());

        List<String> typeWL = Arrays.asList("vfat", "exfat", "sdcardfs", "fuse", "ntfs", "fat32", "ext3", "ext4", "esdfs");
        List<String> typeBL = Arrays.asList("tmpfs");
        String[] mountWL = {"/mnt", "/Removable"};
        String[] mountBL = {
                "/mnt/secure",
                "/mnt/shell",
                "/mnt/asec",
                "/mnt/obb",
                "/mnt/media_rw/extSdCard",
                "/mnt/media_rw/sdcard",
                "/storage/emulated"};
        String[] deviceWL = {
                "/dev/block/vold",
                "/dev/fuse",
                "/mnt/media_rw/extSdCard"};

        try {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = bufReader.readLine()) != null) {

                StringTokenizer tokens = new StringTokenizer(line, " ");
                String device = tokens.nextToken();
                String mountpoint = tokens.nextToken();
                String type = tokens.nextToken();

                // skip if already in list or if type/mountpoint is blacklisted
                if (list.contains(mountpoint) || typeBL.contains(type) || Utils.StartsWith(mountBL, mountpoint))
                    continue;

                // check that device is in whitelist, and either type or mountpoint is in a whitelist
                if (Utils.StartsWith(deviceWL, device) && (typeWL.contains(type) || Utils.StartsWith(mountWL, mountpoint)))
                    list.add(mountpoint);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            Utils.close(bufReader);
        }
        return list;
    }
}
