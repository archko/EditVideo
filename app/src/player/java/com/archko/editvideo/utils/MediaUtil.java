package com.archko.editvideo.utils;

import static java.lang.Math.abs;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.MetadataRetriever;
import androidx.media3.exoplayer.source.TrackGroupArray;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.concurrent.Executors;

/**
 * @author: archko 2023/7/1 :10:03
 */
public class MediaUtil {

    @OptIn(markerClass = UnstableApi.class)
    public static void getMetadata(Context context, MediaItem mediaItem, FutureCallback<TrackGroupArray> callback) {
        ListenableFuture<TrackGroupArray> trackGroupsFuture =
                MetadataRetriever.retrieveMetadata(context, mediaItem);
        Futures.addCallback(
                trackGroupsFuture,
                callback,
                Executors.newSingleThreadExecutor());
    }

    @OptIn(markerClass = UnstableApi.class)
    private static void handleMetadata(TrackGroupArray trackGroups) {
        System.out.println("handleMetadata:" + trackGroups);
    }

    /**
     * mimetype,srt是最容易识别的,但不是所有的都这样.通常getMimeType这两个方法获取不到时,whenUsingGetContentType这个倒可以
     * 但这三个方法都不准确,还不如靠后缀名来获取.
     */

    //
    public static String getMimeType(File file, Context context) {
        Uri uri = Uri.fromFile(file);
        ContentResolver resolver = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(resolver.getType(uri));
        if (TextUtils.isEmpty(type)) {
            type = resolver.getType(uri);
        }
        System.out.println("type:" + type);
        return type;
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        System.out.println("type:" + type);
        return type;
    }

    public static String getExtensionName(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 字幕类型的是这些,测试中发现APPLICATION_VOBSUB后面三个加载不成功
     * (MimeTypes.APPLICATION_SUBRIP.equals(mimeType)
     * || MimeTypes.TEXT_SSA.equals(mimeType)
     * || MimeTypes.TEXT_VTT.equals(mimeType)
     * || MimeTypes.APPLICATION_VOBSUB.equals(mimeType)
     * || MimeTypes.APPLICATION_PGS.equals(mimeType)
     * || MimeTypes.APPLICATION_DVBSUBS.equals(mimeType)
     *
     * @param url
     * @return
     */
    @OptIn(markerClass = UnstableApi.class)
    public static String inferMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (TextUtils.isEmpty(extension)) {
            extension = getExtensionName(url);
        }
        String type = null;
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (TextUtils.isEmpty(type)) {
            if ("srt".equals(extension)) {
                type = MimeTypes.APPLICATION_SUBRIP;
            } else if ("vtt".equals(extension)) {
                type = MimeTypes.TEXT_VTT;
            } else if ("ass".equals(extension) || "ssa".equals(extension)) {
                type = MimeTypes.TEXT_SSA;
            } else if ("xml".equals(extension)) {
                type = MimeTypes.APPLICATION_TTML;
            } else {
                type = MimeTypes.TEXT_UNKNOWN;
            }
        }
        System.out.println("type:" + type);
        return type;
    }

    /**
     * 判断名字
     *
     * @param path
     * @return
     */
    public static String inferName(String path) {
        int start = path.lastIndexOf("/");
        if (start != -1) {
            return path.substring(start + 1);
        } else {
            return null;
        }
    }

    /**
     * 返回一个路径对应的父级路径,如果没有找到/的父级路径,就设置为/
     *
     * @param path
     * @return
     */
    public static String getParentName(String path) {
        if (path != null && path.length() > 0) {
            if ("/".equals(path)) {
                return path;
            }

            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            int dot = path.lastIndexOf('/');
            if ((dot > -1) && (dot < path.length())) {
                String rs = path.substring(0, dot + 1);
                if ("/".equals(rs)) {
                    return rs;
                }

                if (rs.endsWith("/")) {
                    return rs.substring(0, rs.length() - 1);
                }
            }
        }
        return "/";
    }

    public static String getName(String path) {
        if (path != null && path.length() > 0) {
            int dot = path.lastIndexOf('/');
            if ((dot > -1) && (dot < path.length())) {
                String rs = path.substring(dot + 1);
                return rs;
            }
        }
        return path;
    }

    /**
     * Returns the specified millisecond time formatted as a string.
     *
     * @param builder   The builder that {@code formatter} will write to.
     * @param formatter The formatter.
     * @param timeMs    The time to format as a string, in milliseconds.
     * @return The time formatted as a string.
     */
    public static String getStringForTime(StringBuilder builder, Formatter formatter, long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        String prefix = timeMs < 0 ? "-" : "";
        timeMs = abs(timeMs);
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        builder.setLength(0);
        return hours > 0
                ? formatter.format("%s%d:%02d:%02d", prefix, hours, minutes, seconds).toString()
                : formatter.format("%s%02d:%02d", prefix, minutes, seconds).toString();
    }

    public static String MD5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes());
            return bytesToHexString(bytes);
        } catch (NoSuchAlgorithmException e) {
        }
        return data;
    }

    /**
     * 文件MD5值
     *
     * @param filepath
     */
    String md5File(String filepath) {
        try {
            File file = new File(filepath);
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length = -1;
            while (fis.read(buffer, 0, 1024) != -1) {
                length = fis.read(buffer, 0, 1024);
                md.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md.digest());
            return bigInt.toString(16);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static final String byteToString(long size) {
        long GB = 1024 * 1024 * 1024;//定义GB的计算常量
        long MB = 1024 * 1024;//定义MB的计算常量
        long KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + " GB  ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + " MB  ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + " KB  ";
        } else {
            resultSize = size + " B  ";
        }
        return resultSize;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 处理视频的码率 26690kbps,返回值是这样的.
     *
     * @param bitrate
     * @return
     */
    @Nullable
    public static String getBitrate(@Nullable String bitrate) {
        String bit = bitrate.replace("kbps", "");
        try {
            long nBit = Long.parseLong(bit);
            return byteToString(nBit * 1024);
        } catch (NumberFormatException e) {

        }
        return bit;
    }

}
