# UpdateAppDemo
## android 更新软件 适配7.0自动安装软件
### 效果图
![](https://github.com/QQzs/Image/blob/master/UpdateAppDemo/update_art.gif)

### 引入库部分
```Java
// 请求 下载文件
    implementation 'com.squareup.okhttp3:okhttp:3.9.1'
    // 权限申请
    implementation 'io.reactivex.rxjava2:rxjava:2.1.3'
    implementation 'com.tbruyelle.rxpermissions2:rxpermissions:0.9.5@aar'
```
### 获取读写权限 初始化更新的数据
```Java
// 获取权限
        mPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean){
                    UpdateBean updateBean = new UpdateBean();
                    updateBean.setDownloadurl(mUrl);
                    updateBean.setMustbe(false);
                    updateBean.setVersion("2.0.0");
                    updateBean.setReleasenote("更新吧~");
                    UpdateUtil.getInstance().showUpdateDialog(MainActivity.this,updateBean);
                }else{
                    Toast.makeText(MainActivity.this,"获取权限失败，请手动打开",Toast.LENGTH_SHORT).show();
                }
            }
        });
```

### 显示更新下载的dialog
```Java
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
```

### 下载安装包
```Java
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

```
### 安装apk
```Java
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
```
Android6.0引入动态权限控制(Runtime Permissions)，Android7.0引入私有目录被限制访问和StrictMode API 。私有目录被限制访问是指在Android7.0中为了提高应用的安全性，
在7.0上应用私有目录将被限制访问，这与iOS的沙盒机制类似。StrictMode API是指禁止向你的应用外公开 file:// URI。 如果一项包含文件 file:// URI类型 的 Intent 离开你的
应用，则会报出异常。

### 解决方案处理
在清单文件中添加 provider ，新建xml文件夹，添加共享文件夹目录
```Java
//适配7.0
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.zs.demo.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```
```Java
<paths >
    <external-path
        name="files_root"
        path="Android/data/com.zs.demo/" />
    <external-path
        name="external_storage_root"
        path="." />

</paths>
```
注意authorities的值，可以自己随便写，但是为了保险起见，一般用自己的包名定义，安装的时候注意FileProvider.getUriForFile和清单文件里要匹配好
8.0以后安装apk要手动打开 允许安装未知来源的应用 的设置。





