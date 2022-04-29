package com.abtnetworks.totems.mapping.enums;

import com.abtnetworks.totems.mapping.rule.dnatone2one.DefaultDnatOneToOne;
import com.abtnetworks.totems.mapping.rule.dnatone2one.DnatOneToOneV2;
import com.abtnetworks.totems.mapping.rule.snatmany2one.DefaultSnatManyToOne;
import com.abtnetworks.totems.mapping.rule.snatmany2one.SnatManyToOneV2;
import com.abtnetworks.totems.mapping.rule.snatone2one.DefaultSnatOneToOne;
import com.abtnetworks.totems.mapping.rule.snatone2one.SnatOneToOneV2;
import com.abtnetworks.totems.mapping.rule.staticrouting.DefaultStaticRouting;
import com.abtnetworks.totems.mapping.rule.staticrouting.StaticRoutingV2;
import io.swagger.annotations.ApiModelProperty;

/**
 * @desc    地址映射自动匹配规则类型类
 * @author liuchanghao
 * @date 2022-01-20 10:03
 */
public  enum RuleTypeTaskEnum {
    DNAT_ONE_TO_ONE(0, "dnat-one-to-one", "目的Nat一对一", DefaultDnatOneToOne.class, DnatOneToOneV2.class),
    SNAT_MANT_TO_ONE(1, "snat-many-to-one", "源Nat多对一", DefaultSnatManyToOne.class, SnatManyToOneV2.class),
    SNAT_ONE_TO_ONE(2, "snat-one-to-one", "源Nat一对一", DefaultSnatOneToOne.class, SnatOneToOneV2.class),
    STATIC_ROUTING(3, "static-routing", "静态路由", DefaultStaticRouting.class, StaticRoutingV2.class)
    ;

    @ApiModelProperty("编码")
    private int code;

    @ApiModelProperty("规则key")
    private String key;

    @ApiModelProperty("规则名称")
    private String desc;

    @ApiModelProperty("默认实现")
    private Class defaultClass;

    @ApiModelProperty("客户版V2版本实现")
    private Class v2Class;


    RuleTypeTaskEnum(int code, String key, String desc, Class defaultClass, Class v2Class) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.defaultClass = defaultClass;
        this.v2Class = v2Class;
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

    public Class getDefaultClass() {
        return defaultClass;
    }

    public Class getV2Class() {
        return v2Class;
    }

    public void setV2Class(Class v2Class) {
        this.v2Class = v2Class;
    }

    public static RuleTypeTaskEnum fromString(String text) {
        for (RuleTypeTaskEnum ruleTypeEnum : RuleTypeTaskEnum.values()) {
            if (ruleTypeEnum.key.equals(text)) {
                return ruleTypeEnum;
            }
        }
        return null;
    }

    /**
     * 根据编码查枚举
     * @param code
     * @return
     */
    public static RuleTypeTaskEnum getRuleTypeTaskEnumByCode(String code){

        for (RuleTypeTaskEnum ruleTypeTaskEnum: RuleTypeTaskEnum.values() ) {
            if(ruleTypeTaskEnum.getCode() == Integer.parseInt(code)){
                return ruleTypeTaskEnum;
            }
        }
        return DNAT_ONE_TO_ONE;
    }

}
