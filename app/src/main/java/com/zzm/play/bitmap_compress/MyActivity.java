package com.zzm.play.bitmap_compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.zzm.play.R;
import com.zzm.play.utils.l;
import com.zzm.play.utils.PermissionUtil;

import java.io.File;

public class MyActivity extends AppCompatActivity {

    static {
        System.loadLibrary("jpeg");
        System.loadLibrary("play");
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bitmap_compress);

        init();
    }

    ImageView imageView;
    Button bt;

    private void init() {
        bt = findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSomeThing();
            }
        });
        imageView = findViewById(R.id.image);
    }

    private void doSomeThing() {

        PermissionUtil.checkPermission(this);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        l.i("path:" + path);
        File inFile = new File(Environment.getExternalStorageDirectory(), "akali1.jpg");
        File outFile = new File(path, "哈夫曼压缩1.jpg");
        Bitmap inputBitmap = BitmapFactory.decodeFile(inFile.getAbsolutePath());
        imageView.setImageBitmap(inputBitmap);
        bitmapCompress(inputBitmap, outFile.getAbsolutePath());

    }

    private native static void bitmapCompress(Bitmap bitmap, String path);
}
