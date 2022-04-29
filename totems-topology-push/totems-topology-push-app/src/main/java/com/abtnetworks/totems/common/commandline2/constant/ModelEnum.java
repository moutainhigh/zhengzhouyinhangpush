package com.abtnetworks.totems.common.commandline2.constant;

import static com.abtnetworks.totems.common.commandline2.constant.CommandConstant.FIREWALL;

/**
 * @author zc
 * @date 2019/12/31
 */
public enum ModelEnum {

    /**
     * 各个型号
     */
    USG2000("USG2000", FIREWALL),
    USG6000("USG6000",FIREWALL),
    H3C_SEC_PATH_V5("H3C SecPath V5", FIREWALL),
    H3C_SEC_PATH_V7("H3C SecPath V7", FIREWALL);

    private String name;

    private String type;

    ModelEnum(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
