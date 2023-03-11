package com.zzm.play.db;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.db.core.DbImpl;
import com.zzm.play.db.core.DbManager;
import com.zzm.play.db.update.xml_obj.DbUpdateXml;
import com.zzm.play.db.update.xml_obj.UpdateStep;
import com.zzm.play.db.utils.DomUtil;
import com.zzm.play.db.utils.FileUtil;
import com.zzm.play.utils.PermissionUtil;
import com.zzm.play.utils.l;

import java.io.File;
import java.util.List;

public class MyActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        setTheme(R.style.App_Theme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_activity_layout);

        init();

        doSomething();

    }

    private Person person;
    private Person person1;
    private Person person2;

    private void init() {
        DbManager.getInstance().init(this, "zemingzeng.db");
        person = new Person("小明", "boy", 100);
        person1 = new Person("小红", "boy", 55);
        person2 = new Person("小和", "girl", 66);
    }

    private void doSomething() {

        PermissionUtil.checkPermission(this);

        DbImpl<Person> db = DbManager.getInstance().createTable(Person.class);
        db.insert(person);
        db.insert(person1);
        db.insert(person2);

        //查询所有
        List<Person> query = db.query(null);
        Person p;
        if (query != null)
            for (int i = 0; i < query.size(); i++) {
                p = query.get(i);
                l.i("查询结果" + i + " : " + p.toString());
            }

    }

    public void readDbUpdateXml(View view) {
        DbUpdateXml dbUpdateXml = DomUtil.readDbUpdateXml(this);
        UpdateStep dbUpdateStepByVersion = DomUtil.findDbUpdateStepByVersion(dbUpdateXml, "v003", "v007");
        if (null != dbUpdateStepByVersion) {
            l.i("dbUpdateStepByVersion VersionFrom : " + dbUpdateStepByVersion.getVersionFrom());
        }
//        l.i("getCacheDir: " + getCacheDir());
        File file = getDatabasePath("xx.db");
        File file1 = new File(file.getParent() + File.separator + "backup" + File.separator + "zengzeming_backup.db");
        l.i("7777777777777777777: " + file1);
//        FileUtil.CopySingleFile(getDatabasePath("zemingzeng.db").getAbsolutePath(),
//                file1.getAbsolutePath());
//        l.i("getExternalFilesDir DIRECTORY_DOCUMENTS: " + getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
//        l.i("getExternalFilesDir DIRECTORY_MUSIC: " + getExternalFilesDir(Environment.DIRECTORY_MUSIC));
    }

    @Override
    protected void onDestroy() {
        DbManager.getInstance().unInit();
        super.onDestroy();
    }

}
