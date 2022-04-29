package com.abtnetworks.totems.mapping.enums;


/**
 * @desc    地址映射自动匹配状态枚举类
 * @author liuchanghao
 * @date 2022-01-26 9:34
 */
public enum AutoMappingTaskStatusEnum {

    WAIT_RECOMMEND(0,"等待仿真"),
    ADDED_RECOMMEND(1, "已加入仿真任务"),
    ERROR(2, "异常")
    ;

    private Integer code;

    private String desc;


    AutoMappingTaskStatusEnum(Integer code, String desc) {
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
    public static AutoMappingTaskStatusEnum  getStatusByDesc(String desc){
        for (AutoMappingTaskStatusEnum ipTypeEnum: AutoMappingTaskStatusEnum.values() ) {
            if(ipTypeEnum.getDesc().equalsIgnoreCase(desc)){
                return ipTypeEnum;
            }
        }
        return WAIT_RECOMMEND;
    }

}
