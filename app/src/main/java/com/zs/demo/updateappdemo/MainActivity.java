package com.zs.demo.updateappdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zs.demo.updateappdemo.bean.UpdateBean;
import com.zs.demo.updateappdemo.utils.UpdateUtil;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private RxPermissions mPermissions;
    private String mUrl = "http://ec-test01-data.oss-cn-shanghai.aliyuncs.com/file-space/hospital1/20180704/MyApp.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissions = new RxPermissions(this);
    }

    public void updateApp(View view){

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
    }
}
