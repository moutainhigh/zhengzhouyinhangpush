package com.abtnetworks.totems.common.enums;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/8
 */

public enum RecommendTypeEnum {

    DETAIL_RECOMMEND(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND, "业务开通"),
    IN_2OUT_RECOMMEND(PolicyConstants.IN2OUT_INTERNET_RECOMMEND, "内网访问外网"),
    OUT_2IN_RECOMMEND(PolicyConstants.OUT2IN_INTERNET_RECOMMEND, "外网访问内网"),
    BIG_INTERNET_RECOMMEND(PolicyConstants.BIG_INTERNET_RECOMMEND, "大网段开通")
    ;
    /****类型code***/
    private int  typeCode;

    private String desc;

    /**
     * 根据描述获取枚举（枚举类型excel中适配，描述不能随便更改）
     * @param desc
     * @return
     */
    public static RecommendTypeEnum getRecommendTypeByDesc(String desc){
        if(StringUtils.isBlank(desc)){
            return DETAIL_RECOMMEND;
        }
        for (RecommendTypeEnum recommendTypeEnum: RecommendTypeEnum.values() ) {
            if(recommendTypeEnum.desc.equalsIgnoreCase(desc.trim())){
                return recommendTypeEnum;
            }
        }
        return DETAIL_RECOMMEND;
    }

    /**
     * 根据code，获取类型枚举
     * @param typeCode
     * @return
     */
    public static RecommendTypeEnum getRecommendTypeByTypeCode(int typeCode){

        for (RecommendTypeEnum recommendTypeEnum: RecommendTypeEnum.values() ) {
            if(recommendTypeEnum.getTypeCode()  == typeCode){
                return recommendTypeEnum;
            }
        }
        return DETAIL_RECOMMEND;
    }

    RecommendTypeEnum(int typeCode, String desc) {
        this.typeCode = typeCode;
        this.desc = desc;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
