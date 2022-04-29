package com.abtnetworks.totems.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-12-30 10:52
 */
public enum PolicyMergePropertyEnum {

    MERGE_SRC_IP(0, "SRC","合并源IP"),
    MERGE_DST_IP(1,"DST", "合并目的IP"),
    MERGE_SERVICE(2,"SERVICE", "合并服务"),
    ADD_POLICY(3,"ADD_POLICY","新增策略"),
    UN_OPEN_GENERATE(4,"UN_OPEN_GENERATE","无需开通生成命令行"),

    ;


    private static Map<Integer, PolicyMergePropertyEnum> map = new HashMap<>();

    private Integer code;

    private String key;

    private String desc;

    static {
        for(PolicyMergePropertyEnum typeEnum : PolicyMergePropertyEnum.values()){
            map.put(typeEnum.getCode(), typeEnum);
        }
    }

    PolicyMergePropertyEnum() {
    }

    PolicyMergePropertyEnum(Integer code,String key, String desc) {
        this.code = code;
        this.key = key;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }

    public static Map<Integer, PolicyMergePropertyEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        PolicyMergePropertyEnum typeEnum = map.get(code);
        if(typeEnum != null){
            desc = typeEnum.getDesc();
        }
        return desc;
    }

}
