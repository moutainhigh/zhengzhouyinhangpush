package com.abtnetworks.totems.common.enums;

/**
 * @desc    任务类型枚举
 * @author liuchanghao
 * @date 2021-02-05 13:42
 */
public enum TaskTypeEnum {

    SERVICE_TYPE(1,"业务开通类型"),
    INTERNET_OUT_TO_IN_TYPE(2, "互联网开通由外到内"),
    INTERNET_IN_TO_OUT_TYPE(3, "互联网开通由内到外"),
    LARGE_NETWORK_TYPE(4, "大网段类型"),
    SECURITY_TYPE(5, "安全策略类型"),
    SNAT_TYPE(6, "源NAT类型"),
    DNAT_TYPE(7, "目的NAT类型"),
    STATIC_TYPE(8, "静态NAT类型"),
    BOTHNAT_TYPE(9, "BOTHNAT类型"),
    ;

    private Integer code;

    private String desc;


    TaskTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据描述查枚举
     * @param desc
     * @return
     */
    public static TaskTypeEnum getTaskTypeByDesc(String desc){
        for (TaskTypeEnum ipTypeEnum: TaskTypeEnum.values() ) {
            if(ipTypeEnum.getDesc().equalsIgnoreCase(desc)){
                return ipTypeEnum;
            }
        }
        return null;
    }

}
