package cn.cc.sdk.demo.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by shangsong on 15-4-3.
 */
public class Utils {
    /**
     * equals() with two strings where either could be null
     */
    public static boolean nullEquals(String s1, String s2) {
        return s1 == null ? s2 == null : (s1.equals(s2));
    }

    static boolean StartsWith(String[] array, String text) {
        for (String item : array) {
            if (text.startsWith(item))
                return true;
        }

        return false;
    }

    public static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Set the alignment mode of the specified TextView with the desired align
     * mode from preferences.
     * <p/>
     * See @array/audio_title_alignment_values
     *
     * @param alignMode Align mode as read from preferences
     * @param t         Reference to the textview
     */
    public static void setAlignModeByPref(int alignMode, TextView t) {
        if (alignMode == 1)
            t.setEllipsize(TextUtils.TruncateAt.END);
        else if (alignMode == 2)
            t.setEllipsize(TextUtils.TruncateAt.START);
        else if (alignMode == 3) {
            t.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            t.setMarqueeRepeatLimit(-1);
            t.setSelected(true);
        }
    }

    public static void showToast(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getCurrentTime(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
    }
}
