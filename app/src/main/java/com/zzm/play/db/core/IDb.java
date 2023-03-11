package com.zzm.play.db.core;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public interface IDb<T> {

    public void createTable(SQLiteDatabase sqLiteDatabase, Class<T> table);

    public void insert(T newObject);

    public List<T> query(T where);

    public void upDate(T newObject,T where);

    public void delete(T where);

    public void dropTable();


}
