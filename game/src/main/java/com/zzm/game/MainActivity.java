package com.zzm.game;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.router_annotation.Router;

@Router(RouterActivity = "router/game")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.componentization_game_activity_layout);

        doSomething();

    }

    private void doSomething() {


    }

    public void jumpShopping(View view) {
        com.zzm.router.Router.SingleTonHolder.getInstance().jumpActivity("router/shopping");
    }

    public void jumpMain(View view) {
        com.zzm.router.Router.SingleTonHolder.getInstance().jumpActivity("router/main");
    }
}
