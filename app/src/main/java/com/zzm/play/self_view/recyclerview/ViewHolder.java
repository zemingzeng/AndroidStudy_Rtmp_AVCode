package com.zzm.play.self_view.recyclerview;

import android.view.View;

public   class ViewHolder {

    public  ViewHolder(View itemView) {
        this.itemView = itemView;
    }

    public void setItemViewType(int mItemViewType) {
        this.mItemViewType = mItemViewType;
    }

    public int getItemViewType() {
        return mItemViewType;
    }

    public View getItemView() {
        return itemView;
    }

    View itemView;
    int mItemViewType=-1;

}
