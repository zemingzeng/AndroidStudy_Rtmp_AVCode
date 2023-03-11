package com.zzm.play.self_view.recyclerview;

import android.view.View;
import android.widget.TextView;

import com.zzm.play.R;

public class TextViewHolder extends ViewHolder {
    TextView textView;
    public TextViewHolder(View itemView) {
        super(itemView);
        textView=itemView.findViewById(R.id.tv);
    }
}
