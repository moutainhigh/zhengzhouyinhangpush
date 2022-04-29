package com.abtnetworks.totems.command.line.inf.policy;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/19 10:29'.
 */
public interface ACLPolicyInterface extends PolicyInterface {
    /**
     * 查找源域，目的域间策略的ACL策略集
     * @param strName
     * @param dstName
     * @return
     */
    default String getDomainAcl(String strName, String dstName){
        return null;
    }

    default String generateAclName(String aclType,String name,RuleIPTypeEnum ruleIPTypeEnum){
        return null;
    }

    default String generateAclDescription(String description ){
        return null;
    }

    /**
     * acl 后置命令行
     * @return
     */
    default String generateAclPost(){
        return null;
    }

    /**
     * 生产ACL 添加命令
     * @param statusTypeEnum
     * @param statusTypeEnum 状态类型
     * @param name 策略名称
     * @param action 动作
     * @param description 备注说明
     * @param srcIpDto
     * @param dstIpDto
     * @param serviceParam
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     */
    default String generateAclPolicyCommandLine(StatusTypeEnum statusTypeEnum, String aclType,String name,RuleIPTypeEnum ipTypeEnum,
                                                String action,String description,
                                                IpAddressParamDTO srcIpDto, IpAddressParamDTO dstIpDto,
                                                ServiceParamDTO serviceParam,
                                                String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                Map<String, Object> map, String[] args) throws Exception {
        return null;
    }
}
