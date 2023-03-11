package com.zzm.play.db.update.xml_obj;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class DbUpdateXml {

    private List<CreateVersion> createVersionList;
    private List<UpdateStep> updateStepList;

    public DbUpdateXml(Document document) {

        //解析updateStep成员
        NodeList updateSteps = document.getElementsByTagName("updateStep");
        updateStepList = new ArrayList<>();
        for (int i = 0; i < updateSteps.getLength(); i++) {
            Element element = ((Element) updateSteps.item(i));
            updateStepList.add(new UpdateStep(element));
        }

        //解析createVersion
        NodeList createVersions = document.getElementsByTagName("createVersion");
        createVersionList = new ArrayList<>();
        for (int i = 0; i < createVersions.getLength(); i++) {
            Element element = ((Element) createVersions.item(i));
            createVersionList.add(new CreateVersion(element));
        }

    }

    public List<CreateVersion> getCreateVersionList() {
        return createVersionList;
    }

    public void setCreateVersionList(List<CreateVersion> createVersionList) {
        this.createVersionList = createVersionList;
    }

    public List<UpdateStep> getUpdateStepList() {
        return updateStepList;
    }

    public void setUpdateStepList(List<UpdateStep> updateStepList) {
        this.updateStepList = updateStepList;
    }
}
