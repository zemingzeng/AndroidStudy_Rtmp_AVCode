package com.zzm.play.plugin_test;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.utils.l;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(R.style.App_Theme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plugin_activity_layout);

        doSomething();

    }

    private void doSomething() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        l.i("MainActivity rotation : " + rotation);


    }

    public void pluginText(View view) {

        try {
            Class<?> aClass = Class.forName("com.zzm.third_plugin.PluginTest");
            Method test = aClass.getDeclaredMethod("test");
            test.setAccessible(true);
            test.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            l.i(e.toString());
        }

    }
}
