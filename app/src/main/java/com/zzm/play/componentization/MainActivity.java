package com.zzm.play.componentization;

import android.app.Application;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.router.Router;

@com.zzm.router_annotation.Router(RouterActivity = "router/main")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.App_Theme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.componentization_activity_layout);

        doSomething();
    }

    private void doSomething() {


    }

    public void jumpShopping(View view) {
        Router.SingleTonHolder.getInstance().jumpActivity("router/shopping");
    }

    public void jumpGame(View view) {
        Router.SingleTonHolder.getInstance().jumpActivity("router/game");
    }
}
