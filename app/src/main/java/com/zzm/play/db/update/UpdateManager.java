package com.zzm.play.db.update;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.zzm.play.db.update.xml_obj.CreateNewTable;
import com.zzm.play.db.update.xml_obj.CreateVersion;
import com.zzm.play.db.update.xml_obj.DbUpdateXml;
import com.zzm.play.db.update.xml_obj.UpdateDb;
import com.zzm.play.db.update.xml_obj.UpdateStep;
import com.zzm.play.db.utils.DomUtil;
import com.zzm.play.utils.l;

import java.io.File;
import java.util.List;

public class UpdateManager {

    //数据库文件默认在：/data/user/0/com.zzm.play/databases/dbName
    //数据库备份文件夹路径
    private final Context c;

    public UpdateManager(Context c) {
        this.c = c;
    }

    private File getDbPathFile(String dbName) {
        return c.getDatabasePath(dbName);
    }

    //copy数据库文件到/data/user/0/com.zzm.play/databases/backUp/dbName
    private File getDbBackUpPathFile(String dbName) {
        return new File(c.getDatabasePath("xx.db").getParent() + File.separator + "backUp" + File.separator + dbName);
    }

    public void startDbUpdate() {

        DbUpdateXml dbUpdateXml = DomUtil.readDbUpdateXml(c);

        if (null != dbUpdateXml) {

            //得到当前版本和需更新到的最新版本
            String currentVersion = "null";
            String newVersion = "null";
            {

            }

            //拷贝数据库文件到/data/user/0/com.zzm.play/databases/backUp目录下
            {

            }

            //根据当前版本找到需要更新的step
            UpdateStep dbUpdateStep = DomUtil.findDbUpdateStepByVersion(dbUpdateXml, currentVersion, newVersion);
            if (null != dbUpdateStep) {

                List<UpdateDb> updateDbList = dbUpdateStep.getUpdateDbList();

                executeSqlBefore(updateDbList);

                executeCreateNewTable(DomUtil.findCreateByVersion(dbUpdateXml, newVersion));

                executeSqlAfter(updateDbList);

            }

        }

    }

    private void executeSqlBefore(List<UpdateDb> updateDbList) {

        if (null != updateDbList && updateDbList.size() > 0) {

            SQLiteDatabase sqLiteDatabase = null;
            for (UpdateDb updateDb : updateDbList) {

                if (null != updateDb && updateDb.getDbName() != null) {

                    {
                        //拿到数据库sqLiteDatabase 具体需要自己写完整 可能有多张表（不同路径但是相同表名）
                        String dbName = updateDb.getDbName();
                        String dbPath = "xx/xx/xx/xxxxx.db";
                        sqLiteDatabase = getDb(dbPath);
                    }

                    if (null != sqLiteDatabase && null != updateDb.getSqlBeforeList() && updateDb.getSqlBeforeList().size() > 0) {
                        executeSql(sqLiteDatabase, updateDb.getSqlBeforeList());
                        sqLiteDatabase.close();
                    }
                }

            }

        }

    }

    private void executeCreateNewTable(CreateVersion createVersion) {

        if (null != createVersion && null != createVersion.getCreateNewTableList()) {

            SQLiteDatabase sqLiteDatabase = null;

            if (createVersion.getCreateNewTableList().size() > 0)
                for (CreateNewTable createNewTable : createVersion.getCreateNewTableList()) {

                    {
                        //拿到数据库sqLiteDatabase 具体需要自己写完整 可能有多张表（不同路径但是相同表名）
                        String dbName = createNewTable.getDbName();
                        String dbPath = "xx/xx/xx/xxxxx.db";
                        sqLiteDatabase = getDb(dbPath);
                    }

                    List<String> sqlStringList = createNewTable.getSqlStringList();
                    if (null != sqlStringList && sqlStringList.size() > 0 && null != sqLiteDatabase) {
                        executeSql(sqLiteDatabase, sqlStringList);
                        sqLiteDatabase.close();
                    }

                }

        }

    }


    private void executeSqlAfter(List<UpdateDb> updateDbList) {

        if (null != updateDbList && updateDbList.size() > 0) {

            SQLiteDatabase sqLiteDatabase = null;
            for (UpdateDb updateDb : updateDbList) {

                if (null != updateDb && updateDb.getDbName() != null) {

                    {
                        //拿到数据库sqLiteDatabase 具体需要自己写完整 可能有多张表（不同路径但是相同表名）
                        String dbName = updateDb.getDbName();
                        String dbPath = "xx/xx/xx/xxxxx.db";
                        sqLiteDatabase = getDb(dbPath);
                    }

                    if (null != sqLiteDatabase && null != updateDb.getSqlAfterList() && updateDb.getSqlAfterList().size() > 0) {
                        executeSql(sqLiteDatabase, updateDb.getSqlAfterList());
                        sqLiteDatabase.close();
                    }
                }

            }

        }

    }

    private void executeSql(SQLiteDatabase sqLiteDatabase, List<String> sqlList) {

        sqLiteDatabase.beginTransaction();
        for (String sql : sqlList) {
            sql = sql.replaceAll("\r\n", " ");
            sql = sql.replaceAll("\n", " ");
            //trim() 函数移除字符串两侧的空白字符或其他预定义字符。
            // 功能除去字符串开头和末尾的空格或其他字符。
            // 函数执行成功时返回删除了string字符串首部和尾部空格的字符串，
            // 发生错误时返回空字符串（""）
            if (!"".equals(sql.trim())) {
                try {
                    sqLiteDatabase.execSQL(sql);
                    l.i("UpdateManager executeSql sql : " + sql);
                } catch (SQLException e) {
                    l.i(e.toString());
                }
            }
        }

        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();

    }

    private SQLiteDatabase getDb(String dbPath) {

        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);

    }

}
