package com.zzm.play.db.update.xml_obj;

import com.zzm.play.utils.l;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class CreateNewTable {


    private List<String> sqlStringList;
    private String dbName;

    public List<String> getSqlStringList() {
        return sqlStringList;
    }

    public void setSqlStringList(List<String> sqlStringList) {
        this.sqlStringList = sqlStringList;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public CreateNewTable(Element element1) {

        dbName = element1.getAttribute("dbName");
        l.i("CreateNewTable dbName : " + dbName);

        NodeList sql_createTables = element1.getElementsByTagName("sql_createTable");
        sqlStringList = new ArrayList<>();
        for (int i = 0; i < sql_createTables.getLength(); i++) {
            String sql = sql_createTables.item(i).getTextContent();
            l.i("CreateNewTable sql_createTable : " + sql);
            sqlStringList.add(sql);
        }
    }

}
