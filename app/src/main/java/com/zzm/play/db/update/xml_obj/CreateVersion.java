package com.zzm.play.db.update.xml_obj;

import com.zzm.play.utils.l;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class CreateVersion {

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<CreateNewTable> getCreateNewTableList() {
        return createNewTableList;
    }

    public void setCreateNewTableList(List<CreateNewTable> createNewTableList) {
        this.createNewTableList = createNewTableList;
    }

    private String version;
    private List<CreateNewTable> createNewTableList;

    public CreateVersion(Element element) {

        version = element.getAttribute("version");
        l.i("CreateVersion : " + version);

        createNewTableList = new ArrayList<>();
        NodeList createNewTables = element.getElementsByTagName("createNewTable");
        for (int i = 0; i < createNewTables.getLength(); i++) {
            Element element1= (Element) createNewTables.item(i);
            createNewTableList.add(new CreateNewTable(element1));
        }
    }

}
