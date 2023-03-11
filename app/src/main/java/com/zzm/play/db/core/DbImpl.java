package com.zzm.play.db.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zzm.play.utils.l;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbImpl<T> implements IDb<T> {

    private SQLiteDatabase sqLiteDatabase;
    private String tableName;
    private Class<T> aClass;

    private DbImpl() {
    }

    @Override
    public void createTable(SQLiteDatabase sqLiteDatabase, Class<T> tableClass) {

        if (null == sqLiteDatabase) return;

        this.sqLiteDatabase = sqLiteDatabase;

        aClass = tableClass;

        String sql = createTableSql(tableClass);
        l.i("DbImpl createTable createTableSql: " + sql);

        //创建表
        sqLiteDatabase.execSQL(sql);

    }

    private HashMap<String, Field> fieldCacheMap;

    private String createTableSql(Class<T> tableClass) {

        //create table if not exists tableName(name TEXT,age INTEGER,sex TEXT);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table if not exists ");

        //获取表名
        DbTableName aClassAnnotation = tableClass.getAnnotation(DbTableName.class);
        if (null == aClassAnnotation) {
            tableName = tableClass.getSimpleName();
            l.i("createTableSql aClassAnnotation is null，表名为类名");
        } else {
            tableName = aClassAnnotation.tableName();
        }
        stringBuilder.append(tableName).append("(");

        //获取字段名
        Field[] declaredFields = tableClass.getDeclaredFields();
        Field field;

        if (null == fieldCacheMap)
            fieldCacheMap = new HashMap<>();

        for (int i = 0; i < declaredFields.length; i++) {

            field = declaredFields[i];
            DbFiled fieldAnnotation = field.getAnnotation(DbFiled.class);
            if (null == fieldAnnotation) continue;
            String filedName = fieldAnnotation.filedName();

            field.setAccessible(true);
            fieldCacheMap.put(filedName, field);

            String filedType = getFieldType(field.getType());
            stringBuilder.append(filedName).append(" ").append(filedType);

            if (i == declaredFields.length - 1) {
                stringBuilder.append(");");
            } else {
                stringBuilder.append(",");
            }

        }

        if(fieldCacheMap.size()==0)
            throw  new IllegalStateException("创建表失败没有表所需的字段注解！");

        return stringBuilder.toString();
    }

    @Override
    public void insert(T newObject) {

        if (null == sqLiteDatabase || null == newObject) return;

        ContentValues contentValues = new ContentValues();

        l.i("准备insert : " + newObject.toString());

        try {
            //fieldName 表的字段名
            for (String fieldName : fieldCacheMap.keySet()) {
                Field field = fieldCacheMap.get(fieldName);
                Object o = field.get(newObject);
                if (null == o) continue;
                contentValues.put(fieldName, o.toString());
            }

        } catch (Exception e) {
            l.i(e.toString());
            e.printStackTrace();
        }

        //nullColumnHack 防止values空的情况出现 她两不能同时为空
        sqLiteDatabase.insert(tableName, null, contentValues);

    }

    @Override
    public List<T> query(T where) {

        if (null == sqLiteDatabase) return null;

        try {

            StringBuilder sqlStringBuilder = new StringBuilder();
            sqlStringBuilder.append("select * from ").append(tableName).append(" where 1=1 ");

            List<String> fieldValues = new ArrayList<>();
            if (null != where) {

                for (String key : fieldCacheMap.keySet()) {

                    Field field = fieldCacheMap.get(key);
                    Object o = field.get(where);
                    if (null == o) continue;

                    sqlStringBuilder.append("and ").append(key).append("=? ");

                    fieldValues.add(o.toString());

                }
            }

            sqlStringBuilder.append(";");

            String[] selectArgs = fieldValues.toArray(new String[]{});
            Cursor cursor = sqLiteDatabase.rawQuery(sqlStringBuilder.toString(), selectArgs);
            if (null == cursor) return null;

            sqlStringBuilder.append("  selectArgs : ");
            for (String arg : selectArgs) {
                sqlStringBuilder.append(arg).append(" , ");
            }
            l.i("DbImpl query sql string : " + sqlStringBuilder.toString());

            return getResultsFromCursor(cursor);

        } catch (Exception e) {
            l.i(e.toString());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void upDate(T newObject, T where) {

        if (null == sqLiteDatabase) return;

        try {

            ContentValues contentValues = new ContentValues();
            for (String key : fieldCacheMap.keySet()) {
                Field field = fieldCacheMap.get(key);
                Object o = field.get(newObject);
                if (null == o) continue;
                contentValues.put(key, o.toString());
            }

            StringBuilder sqlStringBuilder = new StringBuilder();
            sqlStringBuilder.append(" 1=1 ");

            List<String> fieldValues = new ArrayList<>();
            if (null != where) {

                for (String key : fieldCacheMap.keySet()) {

                    Field field = fieldCacheMap.get(key);
                    Object o = field.get(where);
                    if (null == o) continue;

                    sqlStringBuilder.append("and ").append(key).append("=? ");

                    fieldValues.add(o.toString());

                }
            }

            sqlStringBuilder.append(";");

            String[] selectArgs = fieldValues.toArray(new String[]{});

            sqLiteDatabase.update(tableName, contentValues, sqlStringBuilder.toString(), selectArgs);

            sqlStringBuilder.append("  selectArgs : ");
            for (String arg : selectArgs) {
                sqlStringBuilder.append(arg).append(" , ");
            }
            l.i("DbImpl upDate sql string : " + sqlStringBuilder.toString());


        } catch (Exception e) {
            l.i(e.toString());
            e.printStackTrace();
        }
    }


    @Override
    public void delete(T where) {

        if (null == sqLiteDatabase) return;

        try {

            StringBuilder sqlStringBuilder = new StringBuilder();
            sqlStringBuilder.append(" 1=1 ");

            List<String> fieldValues = new ArrayList<>();
            if (null != where) {

                for (String key : fieldCacheMap.keySet()) {

                    Field field = fieldCacheMap.get(key);
                    Object o = field.get(where);
                    if (null == o) continue;

                    sqlStringBuilder.append("and ").append(key).append("=? ");

                    fieldValues.add(o.toString());

                }
            }
            sqlStringBuilder.append(";");

            String[] selectArgs = fieldValues.toArray(new String[]{});


            sqLiteDatabase.delete(tableName, sqlStringBuilder.toString(), selectArgs);

            sqlStringBuilder.append("  selectArgs : ");
            for (String arg : selectArgs) {
                sqlStringBuilder.append(arg).append(" , ");
            }
            l.i("DbImpl delete sql string : " + sqlStringBuilder.toString());

        } catch (IllegalAccessException e) {
            l.i(e.toString());
            e.printStackTrace();
        }
    }

    //可以删除其他所有表
    @Override
    public void dropTable() {

        if (null == sqLiteDatabase) return;

        l.i("删除表drop table : " + tableName);

        sqLiteDatabase.execSQL("drop table " + tableName + ";");

    }


    public void unInit() {

        l.i("DbImpl unInit");

        if (null != fieldCacheMap) {
            fieldCacheMap.clear();
        }

        fieldCacheMap = null;
        sqLiteDatabase = null;
        aClass = null;
        tableName = null;

    }


    private List<T> getResultsFromCursor(Cursor cursor) throws Exception {

        List<T> list = null;

        if (cursor.moveToFirst()) {

            list = new ArrayList<>();

            T t = null;
            //反射构造函数
            Constructor<T> declaredConstructor = aClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);

            do {

                t = declaredConstructor.newInstance();

                for (String key : fieldCacheMap.keySet()) {

                    int columnIndex = cursor.getColumnIndex(key);

                    Object object = getColumnIndexValue(columnIndex, cursor);

                    //赋值
                    Field field = fieldCacheMap.get(key);
                    if (null == field) continue;
                    field.set(t, object);
                }

                list.add(t);
                //l.i("装载查询数据的list 已添加个数 :" + list.size());
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (null == list) l.i("DbImpl query 此数据表为空 ");
        return list;
    }

    private Object getColumnIndexValue(int columnIndex, Cursor cursor) {

        switch (cursor.getType(columnIndex)) {
            case Cursor.FIELD_TYPE_STRING:
                return cursor.getString(columnIndex);
            case Cursor.FIELD_TYPE_INTEGER:
                return cursor.getInt(columnIndex);
            case Cursor.FIELD_TYPE_FLOAT:
                return cursor.getFloat(columnIndex);
            case Cursor.FIELD_TYPE_BLOB:
                return cursor.getBlob(columnIndex);
            case Cursor.FIELD_TYPE_NULL:
            default:
                return null;
        }
    }

    //获取字段在表中的类型
    private String getFieldType(Class<?> aClass) {

        String type = null;

        if (aClass == String.class) {
            type = "TEXT";
        } else if (aClass == int.class || aClass == Integer.class) {
            type = "INT";
        } else if (aClass == Long.class) {
            type = "BIGINT";
        } else if (aClass == Byte[].class) {
            type = "BLOB";
        } else {
            l.i("unKnow class type aClass :" + aClass.getName());
        }
        return type;
    }
}
