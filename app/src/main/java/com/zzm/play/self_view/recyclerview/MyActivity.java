package com.zzm.play.self_view.recyclerview;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzm.play.R;
import com.zzm.play.self_view.recyclerview.RecyclerView.Adapter;

public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);
        test();
        init();
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(new Adapter() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                LogUtil.i("生成view type:" + viewType);
                LayoutInflater layoutInflater = getLayoutInflater();
                View view = null;
                if (viewType == 0) {
                    view = layoutInflater.inflate(R.layout.item2, parent, false);
                    return new TextViewHolder(view);
                } else if (viewType == 1) {
                    view = layoutInflater.inflate(R.layout.item1, parent, false);
                    return new ImageViewHolder(view);
                }
                return null;
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, int position) {
                if (position % 2 == 0) {
                    String text = ((TextViewHolder) viewHolder).textView.getText().toString();
                    ((TextViewHolder) viewHolder).textView.setText(
                            text + " position->" + position);
                }
            }

            @Override
            public int getItemViewType(int position) {
                if (position % 2 == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }

            @Override
            public int getHeight(int index) {
                return 200;
            }

            @Override
            public int getItemCount() {
                return 30;
            }
        });

    }


    void test() {
//        LogUtil.i(Thread.currentThread().getName());
        //        LogUtil.i("onCreate");
//        findViewById(R.id.frame).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                LogUtil.i("R.id.frame ontouch:" + event.getAction());
//                return false;
//            }
//        });
//        findViewById(R.id.frame1).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                LogUtil.i("R.id.frame1 ontouch:" + event.getAction());
//                return false;
//            }
//        });
//        findViewById(R.id.bt).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                LogUtil.i("R.id.bt  onTouch :" + event.getAction());
//                return false;
//            }
//        });
//        LogUtil.i("" + findViewById(R.id.bt).isClickable());
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        LogUtil.i("onStart");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        LogUtil.i("onResume");
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        LogUtil.i("onRestart");
//    }
    }
}
