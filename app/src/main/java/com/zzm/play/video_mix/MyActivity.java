package com.zzm.play.video_mix;

import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.utils.PermissionUtil;

import java.io.File;

public class MyActivity extends AppCompatActivity {

    private VideoProcess videoProcess;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_mix);

        init();

        doSomeThing();

    }

    private void init() {

        //new PermissionUtil().checkPermission(this);

        String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "TaylorSwift1.mp4";

        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "TaylorSwift1.mp4";

        String outPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "TaylorSwift.mp4";

        videoProcess = new VideoProcess(path1, path2, outPath);

    }

    private void doSomeThing() {

        videoProcess.start();

    }


}
