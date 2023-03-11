package com.zzm.play.db.utils;

import android.content.Context;

import com.zzm.play.db.update.xml_obj.CreateVersion;
import com.zzm.play.db.update.xml_obj.DbUpdateXml;
import com.zzm.play.db.update.xml_obj.UpdateStep;
import com.zzm.play.utils.l;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DomUtil {

    private final static String dbUpdateXmlName = "db_update.xml";

    /**
     * 解析db_update.xml xml文件
     *
     * @param c
     * @return
     */
    public static DbUpdateXml readDbUpdateXml(Context c) {

        InputStream is = null;
        Document document = null;
        try {
            is = c.getAssets().open(dbUpdateXmlName);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
            l.i(e.toString());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    l.i(e.toString());
                }
            }
        }

        if (null != document) {
            return new DbUpdateXml(document);
        }

        return null;
    }

    /**
     * @param dbUpdateXml    从readDbUpdateXml方法中返回
     * @param currentVersion 当前app对应版本
     * @param newVersion     需要更新到的新版本
     * @return 找到当前数据库升级的方式和步骤
     */
    public static UpdateStep findDbUpdateStepByVersion(DbUpdateXml dbUpdateXml, String currentVersion, String newVersion) {

        if (null != currentVersion && null != newVersion && null != dbUpdateXml) {

            List<UpdateStep> updateStepList = dbUpdateXml.getUpdateStepList();

            if (null != updateStepList && updateStepList.size() > 0) {

                for (UpdateStep step : updateStepList) {
                    String versionFrom = step.getVersionFrom();
                    if (null != versionFrom && newVersion.equals(step.getVersionTo()))
                        if (versionFrom.contains(currentVersion)) {
                            return step;
                        }
                }

            }

        }

        return null;
    }

    /**
     * 解析出对应版本的建表脚本
     * @return
     */
    public static CreateVersion findCreateByVersion(DbUpdateXml xml, String version) {
        CreateVersion cv = null;
        if (xml == null || version == null) {
            return cv;
        }
        List<CreateVersion> createVersions = xml.getCreateVersionList();

        if (createVersions != null) {
            for (CreateVersion item : createVersions) {
//                // 如果表相同则要支持xml中逗号分隔   ???不太懂？？？
//                String[] createVersion = item.getVersion().trim().split(",");
//                for (String s : createVersion) {
//                    if (s.trim().equalsIgnoreCase(version)) {
//                        cv = item;
//                        break;
//                    }
//                }
                if (item.getVersion().trim().equalsIgnoreCase(version)) {
                    cv = item;
                    break;
                }
            }
        }

        return cv;
    }


}
