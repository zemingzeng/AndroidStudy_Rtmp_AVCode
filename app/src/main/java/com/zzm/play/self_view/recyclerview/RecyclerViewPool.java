package com.zzm.play.self_view.recyclerview;

import android.util.SparseArray;

import java.util.ArrayList;

public class RecyclerViewPool {


    static class ScrapData{
        //保存同种类型的view holder
        ArrayList<ViewHolder> mScrapList=new ArrayList<>();
    }

    //保存有各种类型的ScrapData
    SparseArray<ScrapData> mScrapArray=new SparseArray<>();

    private ScrapData getScrapDataForType(int viewType){

        ScrapData scrapData=mScrapArray.get(viewType);

        if (scrapData == null) {
            scrapData= new ScrapData();
            mScrapArray.put(viewType,scrapData);
        }
        return scrapData;
    }

    public ViewHolder getRecycledView(int viewType){
        ScrapData scrapData = mScrapArray.get(viewType);
        if (scrapData != null && !scrapData.mScrapList.isEmpty()) {
            return scrapData.mScrapList.remove(
                    scrapData.mScrapList.size()-1);
        }
        return null;
    }

    //被弃用的view holder 保存到缓存中
    public   void putRecycledView(ViewHolder viewHolder,int viewType){
        ScrapData scrapDataForType = getScrapDataForType(viewType);
        scrapDataForType.mScrapList.add(viewHolder);
    }

    //清除缓存
    public void clear(){
        for (int i = 0; i <mScrapArray.size() ; i++) {

            ScrapData scrapData = mScrapArray.valueAt(i);

               scrapData.mScrapList.clear();
        }
        mScrapArray.clear();
    }

}
