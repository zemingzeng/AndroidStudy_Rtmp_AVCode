package com.zzm.play.db.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.zzm.play.utils.l;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class DbManager {

    private volatile static DbManager instance;
    private static final int DATABASE_VERSION = 1;

    private DbManager() {
    }

    public static DbManager getInstance() {

        if (instance == null) {
            synchronized (DbManager.class) {
                if (instance == null) {
                    instance = new DbManager();
                }
            }
        }
        return instance;
    }

    private SQLiteDatabase sqLiteDatabase;
    private MySqliteOpenHelp mySqliteOpenHelp;

    public void init(Context c, String databaseName) {

        //File file = new File(c.getFilesDir(), databaseName);
        //File file = new File(Environment.getExternalStorageDirectory(), databaseName);
        //在应用私有目录下这样创建会无权限
        //sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(file.getAbsolutePath(), null);
        mySqliteOpenHelp = new MySqliteOpenHelp(c, databaseName);
        sqLiteDatabase = mySqliteOpenHelp.getWritableDatabase();
        ///data/user/0/包名/databases/数据库名

        if (sqLiteDatabase != null) {
            l.i("数据库创建或读取成功目录 ： " + sqLiteDatabase.getPath() +
                    " version : " + sqLiteDatabase.getVersion());
        } else {
            l.i("数据库创建或读取失败目录 ");
        }

    }

    private Map<Class<?>, DbImpl<?>> dbImplCacheMap;

    public synchronized <T> DbImpl<T> createTable(Class<T> tableClass) {

        DbImpl<T> db = null;

        try {
            if (null == sqLiteDatabase) return null;

            if ((db = getDb(tableClass)) == null) {

                l.i("dbImplCacheMap 没有存储有对应的此表操作类，马上创建！");

                Constructor<DbImpl> declaredConstructor = DbImpl.class.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                db = declaredConstructor.newInstance();

            } else {
                return db;
            }

            db.createTable(sqLiteDatabase, tableClass);
            dbImplCacheMap.put(tableClass, db);

        } catch (IllegalAccessException |
                InstantiationException |
                NoSuchMethodException |
                InvocationTargetException e) {
            l.i(e.toString());
            e.printStackTrace();
        }

        return db;
    }

    public <T> DbImpl<T> getDb(Class<T> tableClass) {

        if (dbImplCacheMap == null)
            dbImplCacheMap = new HashMap<>();

        DbImpl<T> db = (DbImpl<T>) dbImplCacheMap.get(tableClass);

        if (null == db) {
            l.i("此表还未创建！请先创建！");
        }

        return db;
    }

    public void unInit() {

        l.i("DbManager unInit");

        if (null != dbImplCacheMap) {
            for (Map.Entry<Class<?>, DbImpl<?>> entry : dbImplCacheMap.entrySet()) {
                DbImpl<?> value = entry.getValue();
                value.unInit();
            }
            dbImplCacheMap.clear();
        }

//        if (null != mySqliteOpenHelp) {
//            mySqliteOpenHelp.close();
//        }
//
//        if (null != sqLiteDatabase && sqLiteDatabase.isOpen()) {
//            sqLiteDatabase.close();
//        }

        dbImplCacheMap = null;

    }

    private static class MySqliteOpenHelp extends SQLiteOpenHelper {

        public MySqliteOpenHelp(@Nullable Context context, @Nullable String name) {
            super(context, name, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //回调方法，库创建的时候回调
            l.i("MySqliteOpenHelp onCreate--->db version：" + db.getVersion() + "  path : " + db.getPath());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //回调方法，库更新的时候回调
            l.i("MySqliteOpenHelp onUpgrade--->oldVersion ：" + oldVersion + "  newVersion : " + newVersion);
        }
    }

}
