package com.zzm.play.self_view.recyclerview;

import android.view.View;
import android.widget.ImageView;

import com.zzm.play.R;

public class ImageViewHolder extends ViewHolder {
     ImageView imageView;
    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView=itemView.findViewById(R.id.image);
    }
}
