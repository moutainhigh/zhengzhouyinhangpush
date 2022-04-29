package com.abtnetworks.totems.common.enums;

import com.abtnetworks.totems.issued.particular.impl.*;

/**
 * @author lifei
 * @desc 设备特殊移动枚举类
 * @date 2022/1/5 16:31
 */
public enum DeviceSpecialDealEnum {

    FORTINET(1, "FortinetFortiOS", "飞塔", FortMoveServiceImpl.class,null),
    FORTINET_V5(2, "FortinetFortiOS V5", "飞塔v5", FortMoveServiceImpl.class,null),
    H3CV5(20, "H3C SecPath V5", "华三v5", H3V5MoveServiceImpl.class, null),
    H3CV7(21, "H3C SecPath V7", "华三v7", H3V7SpecialMoveServiceImpl.class, null),
    USG6000_NO_TOP(30, "USG6000 NoTop", "华为U6000 NoTop", HwU6000MoveServiceImpl.class,null),
    TOPSEC_TOS_005(40, "Topsec TOS 005", "天融信", TopSecMoveServiceImpl.class, null),
    TOPSEC_TOS_010_020(41, "Topsec TOS 010-020", "天融信", TopSecMoveServiceImpl.class, null),
    TOPSEC_NG(42, "Topsec NG", "天融信", TopSecMoveServiceImpl.class, null),
    TOPSEC_NG2(43, "Topsec NG v3.2242-2294", "天融信", TopSecMoveServiceImpl.class, null),



    DEFAULTMODEL(300, "Default", "默认设备",null,null)


    ;

    private int code;

    private String key;

    private String desc;

    /**
     * 特殊移动实现类
     */
    private Class specialMoveClass;

    /**
     * 特殊交互实现类
     */
    private Class specialInteractiveClass;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Class getSpecialMoveClass() {
        return specialMoveClass;
    }

    public void setSpecialMoveClass(Class specialMoveClass) {
        this.specialMoveClass = specialMoveClass;
    }

    public Class getSpecialInteractiveClass() {
        return specialInteractiveClass;
    }

    public void setSpecialInteractiveClass(Class specialInteractiveClass) {
        this.specialInteractiveClass = specialInteractiveClass;
    }


    DeviceSpecialDealEnum(int code, String key, String desc, Class specialMoveClass, Class specialInteractiveClass) {
        this.code = code;
        this.key = key;
        this.desc = desc;
        this.specialMoveClass = specialMoveClass;
        this.specialInteractiveClass = specialInteractiveClass;
    }

    public static DeviceSpecialDealEnum getDevice(String deviceModelNumber) {
        for (DeviceSpecialDealEnum modelNumberEnum : DeviceSpecialDealEnum.values()) {
            if (modelNumberEnum.key.equals(deviceModelNumber)) {
                return modelNumberEnum;
            }
        }
        return DEFAULTMODEL;
    }


    /**
     * 判断当前设备是否有特殊移动的实现
     * @param deviceModelNumber
     * @return
     */
    public static boolean hasSpecialMove(String deviceModelNumber) {
        for (DeviceSpecialDealEnum modelNumberEnum : DeviceSpecialDealEnum.values()) {
            if (modelNumberEnum.key.equals(deviceModelNumber) && null != modelNumberEnum.getSpecialMoveClass()) {
                return true;
            }
        }
        return false;
    }


}
