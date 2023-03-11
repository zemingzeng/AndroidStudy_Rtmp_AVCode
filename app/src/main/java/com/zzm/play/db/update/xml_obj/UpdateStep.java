package com.zzm.play.db.update.xml_obj;

import com.zzm.play.utils.l;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class UpdateStep {

    private String versionFrom;
    private String versionTo;
    private List<UpdateDb> updateDbList;

    public String getVersionFrom() {
        return versionFrom;
    }

    public void setVersionFrom(String versionFrom) {
        this.versionFrom = versionFrom;
    }

    public String getVersionTo() {
        return versionTo;
    }

    public void setVersionTo(String versionTo) {
        this.versionTo = versionTo;
    }

    public List<UpdateDb> getUpdateDbList() {
        return updateDbList;
    }

    public void setUpdateDbList(List<UpdateDb> updateDbList) {
        this.updateDbList = updateDbList;
    }

    public UpdateStep(Element element) {

        versionFrom=element.getAttribute("versionFrom");
        versionTo=element.getAttribute("versionTo");
        l.i("UpdateStep versionFrom: "+ versionFrom + " versionTo: "+versionTo);

        updateDbList=new ArrayList<>();
        NodeList updateDbs = element.getElementsByTagName("updateDb");
        for (int i = 0; i <updateDbs.getLength() ; i++) {
            updateDbList.add(new UpdateDb(((Element) updateDbs.item(i))));
        }
    }

}
