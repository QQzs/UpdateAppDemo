package com.zs.demo.updateappdemo.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zs.demo.updateappdemo.AppApplication;
import com.zs.demo.updateappdemo.R;
import com.zs.demo.updateappdemo.bean.UpdateBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateUtil {

    private static UpdateUtil mUpdateUtil;
    private Context mContext;
    private OkHttpClient okHttpClient;
    private Dialog mUpdateDialog;
    private ProgressBar progress_update;
    private TextView tv_update;
    private TextView tv_update_progress;
    /**
     * 更新的内容
     */
    private UpdateBean mUpdateBean;
    /**
     * 更新进度
     */
    private int mProgress = 0;
    /**
     * 是否取消下载
     */
    private boolean mCancelUpdate = false;
    /**
     * apk名字
     */
    private String mApkName = "AAA.apk";

    public static UpdateUtil getInstance() {
        if (mUpdateUtil == null) {
            mUpdateUtil = new UpdateUtil();
        }
        return mUpdateUtil;
    }

    private UpdateUtil() {
        okHttpClient = new OkHttpClient();
        this.mContext = AppApplication.getAppContext();
    }

    /**
     * @param url 下载安装包
     */
    public void downloadAPK(final String url) {
        mCancelUpdate = false;
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
//                        listener.onDownloadFailed(mUpdateBean.getMustbe());
                        Toast.makeText(mContext,"下载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = isExistDir(getApkDir());
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, mApkName);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1  && !mCancelUpdate) {
                        fos.write(buf, 0, len);
                        sum += len;
                        mProgress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        new Handler(mContext.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
//                                listener.onDownloading(mProgress);
                                progress_update.setProgress(mProgress);
                                tv_update_progress.setText("下载进度："+ mProgress + "%");
                                Log.d("My_Log","progress = " + mProgress);
                            }
                        });
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    if (mProgress >= 100){
                        // 下载完成
                        new Handler(mContext.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
//                                listener.onDownloadSuccess();
                                tv_update.setText("安装");
                                installApk();
                                Log.d("My_Log","onDownloadSuccess");
                            }
                        });
                    }
                } catch (Exception e) {
                    new Handler(mContext.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
//                            listener.onDownloadFailed(mUpdateBean.getMustbe());
                            Toast.makeText(mContext,"下载失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * @param saveDir
     * @return
     * @throws IOException
     * 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    /**
     * @param url
     * @return
     * 从下载连接中解析出文件名
     */
    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * apk下载的位置
     * @return
     */
    public String getApkDir() {
        String apkPath = Environment.getExternalStorageDirectory() + "/" + "AAUPDATE";
        return apkPath;
    }

    /**
     * 下载回调 暂时没用
     */
    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * @param progress
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed(boolean mustbe);
    }

    /**
     * 更新dialog
     * @param context 最好传进来新的 防止上下文失效
     * @param updateBean 更新信息
     *
     */
    public void showUpdateDialog(final Context context , UpdateBean updateBean){
        mUpdateBean = updateBean;
        mUpdateDialog = new Dialog(context, R.style.public_dialog_style);
        View view = View.inflate(context,R.layout.dialog_update_layout,null);
        if (updateBean.isMustbe()){
            mUpdateDialog.setCancelable(false);
        }
        mUpdateDialog.setContentView(view);
        Window window = mUpdateDialog.getWindow();
        if (window != null){
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(attributes);
        }
        progress_update = view.findViewById(R.id.progress_update);
        CardView card_view_update = view.findViewById(R.id.card_view_update);
        tv_update = view.findViewById(R.id.tv_update);
        tv_update_progress = view.findViewById(R.id.tv_update_progress);
        TextView tv_update_version = view.findViewById(R.id.tv_update_version);
        TextView tv_release_note = view.findViewById(R.id.tv_release_note);
        tv_update_version.setText("最新版本：" + updateBean.getVersion());
        tv_release_note.setText(updateBean.getReleasenote());
        card_view_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("立即更新".equals(tv_update.getText().toString())){
                    progress_update.setVisibility(View.VISIBLE);
                    tv_update.setText("取消");
                    tv_update_progress.setVisibility(View.VISIBLE);
                    downloadAPK(mUpdateBean.getDownloadurl());
                }else if ("取消".equals(tv_update.getText().toString())){
                    if (mUpdateBean.isMustbe()){
                        mCancelUpdate = true;
//                        new AppManager().getAppManager().AppExit(context);
                        ((Activity)context).finish();
                    }else{
                        if (!mCancelUpdate){
                            mCancelUpdate = true;
                            mUpdateDialog.dismiss();
                        }
                    }
                }else if ("安装".equals(tv_update.getText().toString())){
                    installApk();
                }
            }
        });
        mUpdateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mCancelUpdate = true;
            }
        });
        try {
            if (context != null && mUpdateDialog != null) {
                mUpdateDialog.show();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 安装APK文件
     */
    public void installApk() {
        if (!mUpdateBean.isMustbe() && mUpdateDialog != null){
            mUpdateDialog.dismiss();
        }
        File apkFile = new File(getApkDir(), mApkName);
        if (!apkFile.exists()){
            return;
        }
        Intent install = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.zs.demo.fileprovider", apkFile);//在AndroidManifest中的android:authorities值
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else{
            install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        mContext.startActivity(install);
    }

}
