package com.abtnetworks.totems.command.line.inf.policy;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.EditTypeEnums;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/19 10:29'.
 */
public interface SecurityPolicyInterface extends PolicyInterface {

    /**
     * 生成策略名称
     * @param name 策略名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateSecurityPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 生成安全策略 命令行
     * @param statusTypeEnum 状态类型
     * @param groupName 策略集
     * @param name 策略名称
     * @param id 策略id
     * @param action 动作
     * @param description 备注说明
     * @param logFlag 开启日志
     * @param ageingTime 老化时间
     * @param refVirusLibrary 引用病毒库
     * @param moveSeatEnum 移动位置
     * @param swapRuleNameId 交换位置的规则名或id
     * @param srcIp 源ip
     * @param dstIp 目的ip
     * @param service 服务（源端口，目的端口，协议）
     * @param absoluteTimeParamDTO 绝对时间对象
     * @param periodicTimeParamDTO 周期时间对象
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用服务对象
     * @param refServiceObjectGroup 引用服务组对象
     * @param refTimeObject 引用时间对象
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     */
    default String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                     String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                     String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] service,
                                                     AbsoluteTimeParamDTO absoluteTimeParamDTO,PeriodicTimeParamDTO periodicTimeParamDTO,
                                                     ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                     String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                     String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                     String[] refServiceObject, String[] refServiceObjectGroup,
                                                     String[] refTimeObject,
                                                     Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成安全策略 命令行
     * @param statusTypeEnum 状态类型
     * @param groupName 策略集
     * @param name 策略名称
     * @param id 策略id
     * @param action 动作
     * @param description 备注说明
     * @param logFlag 开启日志
     * @param ageingTime 老化时间
     * @param refVirusLibrary 引用病毒库
     * @param moveSeatEnum 移动位置
     * @param swapRuleNameId 交换位置的规则名或id
     * @param srcIp 源ip
     * @param dstIp 目的ip
     * @param service 服务（源端口，目的端口，协议）
     * @param absoluteTimeParamDTO 绝对时间对象
     * @param periodicTimeParamDTO 周期时间对象
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用服务对象
     * @param refServiceObjectGroup 引用服务组对象
     * @param refTimeObject 引用时间对象
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     */
    default String generateSecurityPolicyModifyCommandLine(StatusTypeEnum statusTypeEnum, EditTypeEnums editType, String groupName, String name, String id, String action, String description,
                                                           String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                           String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp, ServiceParamDTO[] service,
                                                           AbsoluteTimeParamDTO absoluteTimeParamDTO, PeriodicTimeParamDTO periodicTimeParamDTO,
                                                           ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                           String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                           String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                           String[] refServiceObject, String[] refServiceObjectGroup,
                                                           String[] refTimeObject,
                                                           Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 删除策略
     * @param id
     * @param name
     * @param map
     * @param args
     * @return
     */
    default String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        return "";
    }
}
