package me.forrest.commonlib.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BlendMode;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AndroidRuntimeException;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class FileUtil {
    private final static String TAG = "FileUtil";
    private final static String DIR_NAME = "W3";
    private final static String DIR_NAME_PIC = "photo";
    private final static String DIR_NAME_VID = "video";
    private final static String DIR_NAME_Thumb = "thumb";
    private final static String DIR_NAME_TR = "TR"; // test_result
    private final static String DIR_NAME_EQ = "EQ";
    private final static String DIR_NAME_DEVICES = "DEVICES";
//    private final static String DIR_NAME_PIC = "photo";
//    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    /**
     * get current date and time as String
     * @return
     */
    public static final String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    /**
     * generate output file
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM etc.
     * @param ext .mp4(.m4a for audio) or .png
     * @return return null when this app has no writing permission to external storage.
     */
    public static final File getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        dir.mkdirs();
        if (dir.canWrite()) {
            File dirs = new File(dir, getDateTimeString() + ext);
            return dirs;
        }
        return null;
    }

    public static File getPictureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath() + File.separator + DIR_NAME + File.separator + DIR_NAME_PIC);
        dir.mkdirs();
        if (dir.canWrite()) {
            File dirs = new File(dir, getDateTimeString() + ext);
            return dirs;
        }
        return null;
    }

    public static File getVideoFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath() + File.separator + DIR_NAME + File.separator + DIR_NAME_VID);
        dir.mkdirs();
        if (dir.canWrite()) {
            File dirs = new File(dir, getDateTimeString() + ext);
            return dirs;
        } else {
            throw new AndroidRuntimeException("Video File can not be written!!");
        }
    }

    public static final String getThumbnailDir(final String type) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath() + File.separator + DIR_NAME + File.separator + DIR_NAME_Thumb);
        dir.mkdirs();
        if (dir.canWrite()) {
            return dir.getAbsolutePath();
        }
        return null;
    }

    /**
     * 修改文件/文件夹名称
     * @param src 旧文件/文件夹路径
     * @param dest 新文件/文件夹路径
     * @return
     */
    private static boolean renameToNewFile(String src, String dest) {
        File srcFile = new File(src);  //旧文件夹路径
        if (!srcFile.exists()) { return false; }
        boolean isOk = srcFile.renameTo(new File(dest));  //dest新文件夹路径，通过renameto修改
        System.out.println("renameToNewFile is OK ? :" +isOk);
        return isOk;
    }

    /**
     * 修改文件/文件夹名称
     * @param src 旧文件/文件夹路径
     * @param dest 新文件/文件夹路径
     * @param force 是否强制命名为新文件，不强制的情况下，新文件名已经存在的情况下，返回FALSE
     * @return
     */
    private static boolean renameToNewFile(String src, String dest, boolean force) {
        File srcFile = new File(src);  //旧文件夹路径
        File dstFile = new File(dest);
        if (!srcFile.exists()) { return false; }
        if (!force && dstFile.exists()) { return false; }
        boolean isOk = srcFile.renameTo(dstFile);  //dest新文件夹路径，通过renameto修改
        System.out.println("renameToNewFile is OK ? :" +isOk);
        return isOk;
    }

    /**
     * 写入内容到指定的文件中
     * @param context 上下文
     * @param fileName 文件名
     * @param content 内容
     * @return 文件路径
     */
    public static String writeToInternalFile(Context context, String fileName, String content) {
        File file = new File(context.getFilesDir(), fileName);
        String fileAbsolutePath = file.getAbsolutePath();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return fileAbsolutePath;
    }

    public final static String writeToCacheFile(Context context, String fileName, String content) {
        File file = new File(context.getCacheDir(), fileName);
        String fileAbsolutePath = file.getAbsolutePath();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return fileAbsolutePath;
    }

    //过滤文件, 选在符合formatSet格式的文件
    private static boolean filterFile(String path, String[] formatSet) {
        for (String f : formatSet){
            if (path.trim().toLowerCase().endsWith(f)){
                return true;
            }
        }
        return false;
    }

    public static void scanMediaFile(String filePath, ArrayList<FileBean> imagePathList, String[] formatSet) {
        imagePathList.clear();

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        File[] subFiles = file.listFiles();
        for (File f : subFiles) {
            if (f.isDirectory()) {
                scanMediaFile(f.getAbsolutePath(), imagePathList, formatSet);

            } else {
                String path = f.getAbsolutePath();
                if (filterFile(path, formatSet)) {
                    FileBean info = new FileBean();
                    info.setFilePath(path);
                    info.setSelected(false);
                    info.setFileName(path.substring(path.lastIndexOf('/') + 1));
                    if (path.trim().toLowerCase().endsWith(".jpg")) {
                        info.setFileType(FileBean.IMG_TYPE);
                    } else if (path.trim().toLowerCase().endsWith(".mp4")) {
                        info.setFileType(FileBean.VIDEO_TYPE);
                    }
                    imagePathList.add(info);
                }
            }
        }
        Collections.sort(imagePathList, new Comparator<FileBean>() {
            @Override
            public int compare(FileBean o1, FileBean o2) {
                return -1 * o1.getFileName().compareTo(o2.getFileName());
            }
        });
    }

    /**
     * 删除文件安全方式：
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (File childFile : childFiles) {
                deleteFile(childFile);
            }
            deleteFileSafely(file);
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (File childFile : childFiles) {
                deleteFile(childFile);
            }
            deleteFileSafely(file);
        }
    }

    public static void deleteFileList(ArrayList<FileBean> fileInfoList) {
        for (FileBean f : fileInfoList) {
            deleteFile(new File(f.getUrl()));
        }
    }

    //从文件链表中删除指定文件名的数据节点，只删除数据对象， 不对实际文件操作
    private void deleteDataByFileNames(ArrayList<FileBean> dataList, ArrayList<String> filenames) {
        Iterator<FileBean> infoIterator = dataList.iterator();
        while (infoIterator.hasNext()) {
            String fileName = infoIterator.next().getFileName();
            Iterator<String> iterator = filenames.iterator();
            while (iterator.hasNext()) {
                if(iterator.next().equals(fileName)) {
                    iterator.remove();
                    infoIterator.remove();
                    break;
                }
            }
            if (!iterator.hasNext()) return;
        }
    }

    /**
     * 安全删除文件.
     * @param file
     * @return
     */
    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    public static void updateMedia(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public static void updateMedia(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(filePath));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 针对非系统文件夹下的文件,使用该方法 插入时初始化公共字段
     * @param filePath 文件
     * @return ContentValues
     */
    private static ContentValues initCommonContentValues(String filePath, long createTime) {
        ContentValues values = new ContentValues();
        File saveFile = new File(filePath);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, createTime);
        values.put(MediaStore.MediaColumns.DATE_ADDED, createTime);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    /**
     * 保存到视频到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的视频获取不到宽高
     * @param context    上下文
     * @param filePath   文件路径
     * @param duration   视频长度 ms
     * @param width      宽度
     * @param height     高度
     */
    public static void insertVideoToMediaStore(Context context, String filePath, int width, int height, long duration) {
        long createTime = System.currentTimeMillis();
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, createTime);
        if (duration > 0) values.put(MediaStore.Video.VideoColumns.DURATION, duration);
        if (width > 0) values.put(MediaStore.Video.VideoColumns.WIDTH, width);
        if (height > 0) values.put(MediaStore.Video.VideoColumns.HEIGHT, height);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 保存到照片到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的照片获取不到宽高
     * @param context    上下文
     * @param filePath   文件路径
     * @param width      宽度
     * @param height     高度
     */
    public static void insertImageToMediaStore(Context context, String filePath, int width, int height) {
        long createTime = System.currentTimeMillis();
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, createTime);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        if (width > 0) values.put(MediaStore.Images.ImageColumns.WIDTH, width);
        if (height > 0) values.put(MediaStore.Images.ImageColumns.HEIGHT, height);
        values.put(MediaStore.MediaColumns.MIME_TYPE,  "image/jpeg");
        context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    // JH Smart
    public static String writeTestResultToFile(Context context, String fileName, String content) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_TR);
        if (!dir.exists()) { dir.mkdir(); }
        return writeToInternalFile(context, DIR_NAME_TR + File.separator + fileName, content);
    }

    public static String readTestResultFromFile(Context context, String fileName) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_TR);
        if (!dir.exists()) { return null; }
        FileInputStream fis;
        byte[] data = new byte[128];
        try {
            fis = new FileInputStream(dir.getAbsoluteFile() + File.separator + fileName);
            int len = fis.read(data);
            return new String(data, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getTestResultFilename(Context context) {
        File dir = new File(context.getFilesDir(), DIR_NAME_TR);
        if (!dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        String[] filenames;
        if (files != null && files.length > 0) {
            filenames = new String[files.length];
            for (int i=0; i<filenames.length; i++) {
                filenames[i] = files[i].getName();
            }
            return filenames;
        }
        return null;
    }

    public static void deleteTestResultFilename(Context context, String fileName) {
        final File file = new File(context.getFilesDir(), DIR_NAME_TR + File.separator + fileName);
        if (!file.exists()) { return ; }
        deleteFile(file);
    }

    // 重命名TestResult
    public static boolean renameTRFilename(Context context, String oldName, String newName) {
        String oldFilePath = context.getFilesDir() + File.separator + DIR_NAME_TR + File.separator + oldName;
        String newFilePath = context.getFilesDir() + File.separator + DIR_NAME_TR + File.separator + newName;
        return renameToNewFile(oldFilePath, newFilePath, false);
    }

    public static String writeEQToFile(Context context, String fileName, String content) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_EQ);
        if (!dir.exists()) { dir.mkdir(); }
        return writeToInternalFile(context, DIR_NAME_EQ + File.separator + fileName, content);
    }

    public static String readEQFromFile(Context context, String fileName) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_EQ);
        if (!dir.exists()) { return null; }
        FileInputStream fis;
        byte[] data = new byte[128];
        try {
            fis = new FileInputStream(dir.getAbsoluteFile() + File.separator + fileName);
            int len = fis.read(data);
            return new String(data, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getEQFilename(Context context) {
        File dir = new File(context.getFilesDir(), DIR_NAME_EQ);
        if (!dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        String[] filenames;
        if (files != null && files.length > 0) {
            filenames = new String[files.length];
            for (int i=0; i<filenames.length; i++) {
                filenames[i] = files[i].getName();
            }
            return filenames;
        }
        return null;
    }

    public static boolean renameEQFilename(Context context, String oldName, String newName) {
        String oldFilePath = context.getFilesDir() + File.separator + DIR_NAME_EQ + File.separator + oldName;
        String newFilePath = context.getFilesDir() + File.separator + DIR_NAME_EQ + File.separator + newName;
        return renameToNewFile(oldFilePath, newFilePath, false);
    }

    public static void deleteEQFilename(Context context, String fileName) {
        final File file = new File(context.getFilesDir(), DIR_NAME_EQ + File.separator + fileName);
        if (!file.exists()) { return ; }
        deleteFile(file);
    }

    // filename = "Left" or "Right"
    public static String writePairedDevices(Context context, String fileName, HashSet<BLEUtil.BLEDevice> pairedDevices) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_DEVICES);
        if (!dir.exists()) { dir.mkdir(); }
        StringBuffer sb = new StringBuffer(512);
        for (BLEUtil.BLEDevice device : pairedDevices) {
            sb.append(device.deviceName + "," + device.mac + "\n");
        }
        Log.d(TAG, "writePairedDevices : " + sb);
        return writeToInternalFile(context, DIR_NAME_DEVICES + File.separator + fileName, sb.toString());
    }

    public static boolean readPairedDevices(Context context, String fileName, HashSet<BLEUtil.BLEDevice> pairedDevices) {
        pairedDevices.clear();
        final File dir = new File(context.getFilesDir(), DIR_NAME_DEVICES);
        if (!dir.exists()) { return false; }
        final File file = new File(context.getFilesDir(), DIR_NAME_DEVICES + File.separator + fileName);
        if (!file.exists()) { return false; }
        FileInputStream fis;
        byte[] data = new byte[128];
        StringBuffer sb = new StringBuffer(512);
        try {
            fis = new FileInputStream(dir.getAbsoluteFile() + File.separator + fileName);
            int len;
            while( (len = fis.read(data)) > 0) {
                sb.append(new String(data, 0, len));
            }
            Log.d(TAG, "readPairedDevices : " + sb);

            String[] subStrings = sb.toString().split("\n"); // deviceName,mac
            for (String deviceName_mac : subStrings) {
                String[] subSubString = deviceName_mac.split(",");
                if (subSubString.length == 2) {
                    pairedDevices.add(new BLEUtil.BLEDevice(subSubString[0], subSubString[1], 0, ""));
                }
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 保存一个配对设备 filename = "Left" or "Right"
    public static String writePairedDevice(Context context, String fileName, BLEUtil.BLEDevice pairedDevice) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_DEVICES);
        if (!dir.exists()) { dir.mkdir(); }
        if (pairedDevice == null) { return writeToInternalFile(context, DIR_NAME_DEVICES + File.separator + fileName, ""); }
        StringBuilder sb = new StringBuilder(128);
        sb.append(pairedDevice.deviceName).append(",")
                .append(pairedDevice.mac).append(",")
                .append(pairedDevice.alias).append(",")
                .append("\n");
        Log.d(TAG, "writePairedDevice : " + sb);
        return writeToInternalFile(context, DIR_NAME_DEVICES + File.separator + fileName, sb.toString());
    }

    public static boolean readPairedDevice(Context context, String fileName, BLEUtil.BLEDevice pairedDevice) {
        final File dir = new File(context.getFilesDir(), DIR_NAME_DEVICES);
        if (!dir.exists()) { return false; }
        final File file = new File(context.getFilesDir(), DIR_NAME_DEVICES + File.separator + fileName);
        if (!file.exists()) { return false; }
        FileInputStream fis;
        byte[] data = new byte[128];
        StringBuffer sb = new StringBuffer(512);
        try {
            fis = new FileInputStream(dir.getAbsoluteFile() + File.separator + fileName);
            int len;
            while( (len = fis.read(data)) > 0) {
                sb.append(new String(data, 0, len));
            }

            String[] subStrings = sb.toString().split("\n"); // deviceName,mac
            for (String deviceName_mac : subStrings) {
                String[] subSubString = deviceName_mac.split(",");
                if (subSubString.length == 3) {
                    pairedDevice.deviceName = subSubString[0];
                    pairedDevice.mac        = subSubString[1];
                    pairedDevice.alias      = subSubString[2];
                    pairedDevice.connectStatus = 0;
                    return true;
                }
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
