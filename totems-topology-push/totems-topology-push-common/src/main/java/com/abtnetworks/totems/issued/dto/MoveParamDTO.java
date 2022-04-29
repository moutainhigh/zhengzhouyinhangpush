package com.abtnetworks.totems.issued.dto;

/**
 * @author zakyoung
 * @Title:
 * @Description: 移动的参数
 * @date 2020-03-15
 */
public class MoveParamDTO {
    /***移动方式，如前，top ，after**/
    private String relatedName;

    /***移动的规则id**/
    private String relatedRule;
    /***0：ipv4，1：ipv6，只有目前h3v7用到**/
    private Integer ipType;
    /**
     * 天融信的组移动
     **/
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getIpType() {
        return ipType;
    }

    public void setIpType(Integer ipType) {
        this.ipType = ipType;
    }

    public String getRelatedName() {
        return relatedName;
    }

    public void setRelatedName(String relatedName) {
        this.relatedName = relatedName;
    }

    public String getRelatedRule() {
        return relatedRule;
    }

    public void setRelatedRule(String relatedRule) {
        this.relatedRule = relatedRule;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
