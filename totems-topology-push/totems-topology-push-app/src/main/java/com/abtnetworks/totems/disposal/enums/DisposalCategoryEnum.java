package com.abtnetworks.totems.disposal.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务类型
 * @author luwei
 * @date 2019/11/12
 */
public enum DisposalCategoryEnum {

    POLICY(0, "策略"),
    ROUT(1, "路由"),
    ;


    private static Map<Integer, DisposalCategoryEnum> map = new HashMap<>();

    private Integer code;

    private String desc;

    static {
        for(DisposalCategoryEnum categoryEnum : DisposalCategoryEnum.values()){
            map.put(categoryEnum.getCode(), categoryEnum);
        }
    }

    DisposalCategoryEnum() {
    }

    DisposalCategoryEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, DisposalCategoryEnum> getMap() {
        return map;
    }

    public static String getDescByCode(Integer code) {
        String desc = "";
        DisposalCategoryEnum categoryEnum = map.get(code);
        if(categoryEnum != null){
            desc = categoryEnum.getDesc();
        }
        return desc;
    }

}
