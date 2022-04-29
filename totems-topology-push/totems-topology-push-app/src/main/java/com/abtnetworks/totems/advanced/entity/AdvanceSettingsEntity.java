package com.abtnetworks.totems.advanced.entity;

public class AdvanceSettingsEntity {
    private Integer id;

    private String paramName;

    private String paramValue;


    public AdvanceSettingsEntity(String paramName,String paramValue){
        this.paramName = paramName;
        this.paramValue = paramValue;
    }
    public AdvanceSettingsEntity(){
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName == null ? null : paramName.trim();
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue == null ? null : paramValue.trim();
    }
}