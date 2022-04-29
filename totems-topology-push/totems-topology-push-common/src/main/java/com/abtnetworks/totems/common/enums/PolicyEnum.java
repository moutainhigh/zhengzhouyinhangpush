package com.abtnetworks.totems.common.enums;
/**
 * @author luwei
 * @date 2019/5/27
 */
public enum PolicyEnum {
    SECURITY(0, "Security", "安全策略"),
    STATIC(10, "Static", "静态NAT"),
    SNAT(20, "SNat", "源NAT"),
    DNAT(30, "DNat", "目的NAT"),
    BOTH(40, "Both", "BOTH NAT"),
    ROUTING(50, "Routing", "Routing"),
    ACL(60, "ACL", "ACL策略"),
    FORBID(70, "forbid", "封禁"),
    EDIT_FORBID(80, "editForBid", "编辑封禁"),
    DISABLE(90, "disable", "禁用策略"),
    ENABLE(100, "enable", "启用策略"),
    EDIT_SECURITY(110, "editSecurity", "编辑安全策略"),
    BAN_TASK_DENY(110, "ban", "封禁任务"),
    BAN_TASK_PERMIT(110, "ban", "封禁任务解封"),
    F5_DNAT(150,"fiveDnat","Five DNat"),
    F5_BOTH_NAT(160,"fiveBothnat","Five Both Nat"),
    STRTIC_ROUTING(170, "staticRouting", "静态路由"),

    ;


    private int code;

    private String desc;

    private String key;

    PolicyEnum(int code, String key, String desc) {
        this.code = code;
        this.desc = desc;
        this.key = key;
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
}
