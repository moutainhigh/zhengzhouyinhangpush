package com.abtnetworks.totems.common.commandline2.constant;


/**
 * @author zc
 * @date 2019/12/31
 */
public enum VendorEnum {

    /**
     * 各个厂商
     */
    HUA_WEI("huaWei", ModelEnum.USG2000, ModelEnum.USG6000),
    H3C("H3C", ModelEnum.H3C_SEC_PATH_V5, ModelEnum.H3C_SEC_PATH_V7);

    private String name;

    private ModelEnum[] modelEnums;

    VendorEnum(String name, ModelEnum... modelEnums) {
        this.name = name;
        this.modelEnums = modelEnums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelEnum[] getModelEnums() {
        return modelEnums;
    }

    public void setModelEnums(ModelEnum[] modelEnums) {
        this.modelEnums = modelEnums;
    }
}
