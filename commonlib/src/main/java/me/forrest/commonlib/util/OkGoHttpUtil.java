package me.forrest.commonlib.util;

import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.File;
import java.util.ArrayList;

public class OkGoHttpUtil {
    public final static int Status_Error    = -1;
    public final static int Status_Start    = 1;
    public final static int Status_Progress = 2;
    public final static int Status_Success  = 3;

    private int mTotalCount;
    private int mSuccessCount;
    private int mFailedCount;

    public interface HttpUtilDownloadCallback {
        void downloadCallback(int status, int nTotal, int nSuccess, int nFailed, int nProgress, String url);
    }

    public interface HttpUtilDeleteCallback {
        void deleteCallback(int status, int ntotal, int nSuccess, int nFailed, String url);
    }

    public interface DownloadCallback {
        void callback(int status, int process);
    }

    public void downloadFiles(ArrayList<FileBean> downloadList, String destFileDir, final HttpUtilDownloadCallback callback) {
        mTotalCount = downloadList.size();
        mSuccessCount = 0;
        mFailedCount = 0;
        for (FileBean f: downloadList) {
            OkGo.<File>get(f.getUrl())
                .tag(this)
                .execute(new FileCallback(destFileDir, f.getFileName()) {

                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        callback.downloadCallback(Status_Start, mTotalCount, mSuccessCount, mFailedCount, 0, null);
                    }

                    @Override
                    public void onSuccess(Response<File> response) {
                        mSuccessCount++;
                        String url = response.getRawResponse().request().url().toString();
                        callback.downloadCallback(Status_Success, mTotalCount, mSuccessCount, mFailedCount, 100, url);
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        callback.downloadCallback(Status_Progress, mTotalCount, mSuccessCount, mFailedCount, (int)(progress.fraction * 100.0f), null);
                    }

                    @Override
                    public void onError(Response<File> response) {
                        mFailedCount++;
                        String url = response.getRawResponse().request().url().toString();
                        callback.downloadCallback(Status_Error, mTotalCount, mSuccessCount, mFailedCount, 0, url);
                    }
                });
        }
    }

    public void downloadFile(String url, String destFileDir, String destFileName, final DownloadCallback d) {
        OkGo.<File>get(url)
            .tag(this)
            .execute(new FileCallback(destFileDir, destFileName) {

                @Override
                public void onStart(Request<File, ? extends Request> request) {
                    d.callback(Status_Start, 0);
                }

                @Override
                public void onSuccess(Response<File> response) {
                    d.callback(Status_Success, 100);
                }

                @Override
                public void downloadProgress(Progress progress) {
                    d.callback(Status_Progress, (int)(progress.fraction * 100.0f));
                }

                @Override
                public void onError(Response<File> response) {
                    Log.d("test", " " + response.toString());
                    d.callback(Status_Error, 0);
                }
            });
    }

    //删除飞机上的图片,同时删除链表中的节点
    public void deleteFiles(final ArrayList<FileBean> deleteList, final HttpUtilDeleteCallback callback) {
        mTotalCount = deleteList.size();
        mSuccessCount = 0;
        mFailedCount = 0;
        for (FileBean f: deleteList) {
            OkGo.<String>delete(f.getUrl())
                    .tag(this)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            mSuccessCount++;
                            String url = response.getRawResponse().request().url().toString();
                            callback.deleteCallback(Status_Success, mTotalCount, mSuccessCount, mFailedCount, url);
                        }

                        @Override
                        public void onError(Response<String> response) {
                            mFailedCount++;
                            String url = response.getRawResponse().request().url().toString();
                            callback.deleteCallback(Status_Success, mTotalCount, mSuccessCount, mFailedCount, url);
                        }
                    });
        }
    }

    public static void delete(String url) {
        OkGo.<String>delete(url)
            .tag("delete")
            .execute(new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                }

                @Override
                public void onError(Response<String> response) {
                }
            });
    }

    public void cancel() {
        OkGo.getInstance().cancelTag(this);
    }
}
