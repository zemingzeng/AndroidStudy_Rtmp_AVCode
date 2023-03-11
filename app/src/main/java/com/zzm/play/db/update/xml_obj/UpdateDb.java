package com.zzm.play.db.update.xml_obj;

import com.zzm.play.utils.l;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class UpdateDb {

    private String dbName;
    private List<String> sqlBeforeList;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<String> getSqlBeforeList() {
        return sqlBeforeList;
    }

    public void setSqlBeforeList(List<String> sqlBeforeList) {
        this.sqlBeforeList = sqlBeforeList;
    }

    public List<String> getSqlAfterList() {
        return sqlAfterList;
    }

    public void setSqlAfterList(List<String> sqlAfterList) {
        this.sqlAfterList = sqlAfterList;
    }

    private List<String> sqlAfterList;

    public UpdateDb(Element item) {

        dbName = item.getAttribute("dbName");
        l.i("UpdateDb dbName : " + dbName);

        NodeList sql_befores = item.getElementsByTagName("sql_before");
        sqlBeforeList = new ArrayList<>();
        for (int i = 0; i < sql_befores.getLength(); i++) {
            String sql_before = sql_befores.item(i).getTextContent();
            sqlBeforeList.add(sql_before);
            l.i("UpdateDb sql_before " + i + " : " + sql_before);
        }

        NodeList sql_afters = item.getElementsByTagName("sql_after");
        sqlAfterList = new ArrayList<>();
        for (int i = 0; i < sql_afters.getLength(); i++) {
            String sql_after = sql_afters.item(i).getTextContent();
            sqlAfterList.add(sql_after);
            l.i("UpdateDb sql_after " + i + " : " + sql_after);
        }

    }

}
