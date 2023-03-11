package com.zzm.shopping;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.router_annotation.Router;

@Router(RouterActivity = "router/shopping")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.componentization_shopping_activity_layout);

        doSomething();

    }

    private void doSomething() {


    }

    public void jumpGame(View view) {
        com.zzm.router.Router.SingleTonHolder.getInstance().jumpActivity("router/game");
    }

    public void jumpMain(View view) {
        com.zzm.router.Router.SingleTonHolder.getInstance().jumpActivity("router/main");
    }
}
