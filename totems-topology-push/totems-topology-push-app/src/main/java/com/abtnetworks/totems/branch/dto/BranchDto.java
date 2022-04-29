package com.abtnetworks.totems.branch.dto;

import java.util.List;

/**
 * @ Author     ：muyuanling.
 * @ Date       ：Created in 9:54 2019/5/10
 */
public class BranchDto extends Branch{

    private List<BranchDto> branchList;
    private int nodeNum;
    private int layerNum;

    public List<BranchDto> getBranchList() {
        return branchList;
    }

    public void setBranchList(List<BranchDto> branchList) {
        this.branchList = branchList;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public int getLayerNum() {
        return layerNum;
    }

    public void setLayerNum(int layerNum) {
        this.layerNum = layerNum;
    }
}
