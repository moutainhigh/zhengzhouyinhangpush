package com.abtnetworks.totems.common.enums;

import com.abtnetworks.totems.common.commandline.bothnat.*;
import com.abtnetworks.totems.common.commandline.disable.DisableSecurityHillStoneR5;
import com.abtnetworks.totems.common.commandline.dnat.*;
import com.abtnetworks.totems.common.commandline.editforbid.EditForbidHillStoneR5;
import com.abtnetworks.totems.common.commandline.enable.EnableSecurityHillStoneR5;
import com.abtnetworks.totems.common.commandline.forbid.ForbidHillStoneR5;
import com.abtnetworks.totems.common.commandline.rollback.*;
import com.abtnetworks.totems.common.commandline.routing.*;
import com.abtnetworks.totems.common.commandline.security.translation.SecurityHillStoneR5Extended;
import com.abtnetworks.totems.common.commandline.snat.*;

public  enum DeviceModelNumberEnumExtended {

    HILLSTONE(20, "HillstoneStoneOS", "山石", SecurityHillStoneR5Extended.class, null, SnatHillstoneR5.class, DnatHillstoneR5.class, null, BothHillstoneR5.class, RoutingHillstone.class, RollbackSecurityHillstoneR5.class, RollbackRoutingHillstone.class, null, null, null, null, ForbidHillStoneR5.class, EditForbidHillStoneR5.class, DisableSecurityHillStoneR5.class, EnableSecurityHillStoneR5.class),
    HILLSTONE_R5(21, "HillStoneR5", "山石R5",SecurityHillStoneR5Extended.class, null, SnatHillstoneR5.class, DnatHillstoneR5.class, null, BothHillstoneR5.class, RoutingHillstone.class, RollbackSecurityHillstoneR5.class, RollbackRoutingHillstone.class, null, null, null, null, ForbidHillStoneR5.class, EditForbidHillStoneR5.class, DisableSecurityHillStoneR5.class, EnableSecurityHillStoneR5.class)

    ;

    private int code;
    private String key;
    private String desc;
    private Class securityClass;
    private Class aclClass;
    private Class snatClass;
    private Class dnatClass;
    private Class staticNatClass;
    private Class bothNatClass;
    private Class routingClass;
    private Class rollbackSecurityClass;
    private Class rollbackAclClass;
    private Class rollbackSnatClass;
    private Class rollbackDnatClass;
    private Class rollbackStaticNatClass;
    private Class rollbackBothNatClass;
    private Class rollbakcRoutingClass;

    /**封禁：大部分厂商与安全策略兼容，小部分不兼容，比如：山石地址对象**/
    private Class forbidClass;

    /**编辑封禁**/
    private Class editForbidClass;

    /** 启用策略 **/
    private Class disableSecurityClass;

    /** 禁用策略 **/
    private Class enableSecurityClass;


    DeviceModelNumberEnumExtended(int code, String key, String desc) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = null;
        this.aclClass = null;
        this.snatClass = null;
        this.dnatClass = null;
        this.staticNatClass = null;
        this.bothNatClass = null;
        this.routingClass = null;
        this.rollbackSecurityClass = null;
        this.rollbackSnatClass = null;
        this.rollbackDnatClass = null;
        this.rollbackStaticNatClass = null;
        this.rollbackBothNatClass = null;
        this.rollbakcRoutingClass = null;
    }

    DeviceModelNumberEnumExtended(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                                  Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass  ) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;

    }

    DeviceModelNumberEnumExtended(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                                  Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass , Class rollbackAclClass ) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackAclClass = rollbackAclClass;
    }

    DeviceModelNumberEnumExtended(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                                  Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass, Class rollbackSnatClass, Class rollbackDnatClass, Class rollbackStaticNatClass, Class rollbackBothNatClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackSnatClass = rollbackSnatClass;
        this.rollbackDnatClass = rollbackDnatClass;
        this.rollbackStaticNatClass = rollbackStaticNatClass;
        this.rollbackBothNatClass = rollbackBothNatClass;
    }



    DeviceModelNumberEnumExtended(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                                  Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass, Class rollbackSnatClass,
                                  Class rollbackDnatClass, Class rollbackStaticNatClass, Class rollbackBothNatClass,
                                  Class forbidClass, Class editForbidClass, Class disableSecurityClass, Class enableSecurityClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackSnatClass = rollbackSnatClass;
        this.rollbackDnatClass = rollbackDnatClass;
        this.rollbackStaticNatClass = rollbackStaticNatClass;
        this.rollbackBothNatClass = rollbackBothNatClass;
        this.forbidClass = forbidClass;
        this.editForbidClass = editForbidClass;
        this.disableSecurityClass = disableSecurityClass;
        this.enableSecurityClass = enableSecurityClass;
    }


    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }

    public Class getSecurityClass() {
        return securityClass;
    }

    public Class getAclClass() {
        return aclClass;
    }

    public Class getSnatClass() {
        return snatClass;
    }

    public Class getDnatClass() {
        return dnatClass;
    }

    public Class getStaticNatClass() {
        return staticNatClass;
    }

    public Class getBothNatClass() {
        return bothNatClass;
    }

    public Class getRoutingClass() {
        return routingClass;
    }

    public Class getRollbackSecurityClass() {
        return rollbackSecurityClass;
    }

    public Class getRollbackAclClass() {
        return rollbackAclClass;
    }

    public Class getRollbackSnatClass() {
        return rollbackSnatClass;
    }

    public Class getRollbackDnatClass() {
        return rollbackDnatClass;
    }

    public Class getRollbackStaticNatClass() {
        return rollbackStaticNatClass;
    }

    public Class getRollbackBothNatClass() {
        return rollbackBothNatClass;
    }

    public Class getRollbakcRoutingClass() {
        return rollbakcRoutingClass;
    }

    public Class getForbidClass() {
        return forbidClass;
    }

    public Class getEditForbidClass() {
        return editForbidClass;
    }

    public Class getDisableSecurityClass() {
        return disableSecurityClass;
    }

    public Class getEnableSecurityClass() {
        return enableSecurityClass;
    }

    public static DeviceModelNumberEnumExtended fromString(String text) {
        for (DeviceModelNumberEnumExtended modelNumberEnum : DeviceModelNumberEnumExtended.values()) {
            if (modelNumberEnum.key.equals(text)) {
                return modelNumberEnum;
            }
        }
        return null;
    }
}
