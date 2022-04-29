package com.abtnetworks.totems.command.line.enums;

import com.abtnetworks.totems.vender.Juniper.security.SecurityJuniperSRXImpl;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg2100Impl;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import com.abtnetworks.totems.vender.abt.security.AclAbtImpl;
import com.abtnetworks.totems.vender.abt.security.SecurityAbtBListImpl;
import com.abtnetworks.totems.vender.abt.security.SecurityAbtImpl;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoASA99Impl;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoASAImpl;
import com.abtnetworks.totems.vender.dptech.r003.SecurityDp003Impl;
import com.abtnetworks.totems.vender.dptech.r004.SecurityDp004Impl;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinet310BImpl;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;
import com.abtnetworks.totems.vender.h3c.acl.AclH3cSecPathV7Impl;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV5Impl;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV7Impl;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV7OPImpl;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneImpl;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneR5Impl;
import com.abtnetworks.totems.vender.sangfor.SecuritySangforImpl;
import com.abtnetworks.totems.vender.topsec.TOS_005.SecurityTopsec005Impl;
import com.abtnetworks.totems.vender.topsec.TOS_010.SecurityTopsec010Impl;
import com.abtnetworks.totems.vender.topsec.TOS_NG.SecurityTopsecNGImpl;
import com.abtnetworks.totems.vender.topsec.TOS_NG2.SecurityTopsecNG2Impl;

public  enum DeviceModelNumberEnumExtended {
    HILLSTONE(20, "HillstoneStoneOS", "山石", SecurityHillStoneImpl.class, null, null, null, null, null,null, null,null),
    HILLSTONE_V5(22, "HillstoneStoneOS V5.5", "山石V5.5", SecurityHillStoneR5Impl.class,null, null, null, null, null,null, null,null),
    H3CV5(70, "H3C SecPath V5", "华三v5",SecurityH3cSecPathV5Impl.class, null, null, null, null, null,null, null,null),
    H3CV7(80, "H3C SecPath V7", "华三v5", SecurityH3cSecPathV7Impl.class, null, null, null, null, null,null, null,null),
    H3CV7OP(81, "H3C SecPath V7 OP", "华三v7", SecurityH3cSecPathV7OPImpl.class, null, null, null, null, null,null, SecurityH3cSecPathV7OPImpl.class,null),
    H3CV7_ZONE_PAIR_ACL(84, "H3C SecPath V7 zone-pair ACL", "华三v7 zone-pair ACL", SecurityH3cSecPathV7Impl.class, AclH3cSecPathV7Impl.class, null, null, null, null, null, SecurityH3cSecPathV7Impl.class,AclH3cSecPathV7Impl.class),
    SANG_FOR_IMAGE(170, "Firewall af v8.0.25","深信服v8.0.25", SecuritySangforImpl.class, null, null, null, null, null,null, null,null),
    TOPSEC_TOS_005(30, "Topsec TOS 005",  "天融信", SecurityTopsec005Impl.class, null, null, null, null, null,null, null,null),
    TOPSEC_TOS_010_020(31, "Topsec TOS 010-020",  "天融信", SecurityTopsec010Impl.class, null, null, null, null, null,null, null,null),
    TOPSEC_NG(32, "Topsec NG",  "天融信", SecurityTopsecNGImpl.class, null, null, null, null, null,null, null,null),
    TOPSEC_NG2(33, "Topsec NG v3.2242-2294",  "天融信", SecurityTopsecNG2Impl.class, null, null, null, null, null,null, null,null),
    DPTECHR003(51, "DPTech Firewall R003", "迪普R003", SecurityDp003Impl.class, null, null, null, null, null,null, null,null),
    DPTECHR004(50, "DPTech Firewall R004", "迪普R004", SecurityDp004Impl.class, null, null, null, null, null,null, null,null),
    CISCO(10, "Cisco ASA", "思科", SecurityCiscoASAImpl.class, null, null, null, null, null,null, null,null),
    CISCO_ASA_99(15, "Cisco ASA 9.9", "思科ASA9.9", SecurityCiscoASA99Impl.class, null, null, null, null, null,null, null,null),
    USG2000(110, "USG2000", "华为USG2000", SecurityUsg2100Impl.class, null, null, null, null, null, null, null, null),
    USG6000(111, "USG6000", "华为U6000", SecurityUsg6000Impl.class, null,null,null,null,null,null,null,null,null),
    USG6000_NO_TOP(112, "USG6000 NoTop", "华为U6000 NoTop", SecurityUsg6000Impl.class, null,null,null,null,null,null,null,null,null),
    SRX(90, "JuniperSRX", "Juniper SRX", SecurityJuniperSRXImpl.class, null,null,null,null,null,null,null,null,null),
    SRX_NoCli(91, "JuniperSRX NoCli", "Juniper SRX NoCli", SecurityJuniperSRXImpl.class, null,null,null,null,null,null,null,null,null),
    FORTINET(60, "FortinetFortiOS", "飞塔", SecurityFortinetImpl.class, null,null,null,null,null,null,null,null,null),
    FORTINET_310B(61, "Fortinet 310B", "飞塔310B", SecurityFortinet310BImpl.class, null,null,null,null,null,null,null,null,null),
    ABTNETWORKS_BLIST(25, "abtnetworks blist", "安博通 blist", SecurityAbtBListImpl.class, AclAbtImpl.class,null,null,null,null,null,null,null,null),
    ABTNETWORKS(26, "abtnetworks", "安博通",SecurityAbtImpl.class, AclAbtImpl.class,null,null,null,null,null,null,null,null),
    SDNWARE(27, "sdnware", "云融",SecurityAbtImpl.class, AclAbtImpl.class,null,null,null,null,null,null,null,null)

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
