package com.abtnetworks.totems.command.line.inf.policy;

import com.abtnetworks.totems.command.line.dto.InterfaceParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.dto.ZoneParamDTO;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/19 10:29'.
 */
public interface NatPolicyInterface extends PolicyInterface {

    /**
     * 生成Nat策略名称
     * @param name 策略名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateNatPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 生成 static nat策略 命令行
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
     * @param insideAddress 内网地址
     * @param globalAddress 外网地址
     * @param insideServiceParam 内网服务
     * @param globalServiceParam 外网服务
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface   源接口
     * @param outInterface  目的接口
     * @param insideRefIpAddressObject  内网地址对象名
     * @param insideRefIpAddressObjectGroup 内网地址组对象名
     * @param globalRefIpAddressObject 外网地址对象名
     * @param globalRefIpAddressObjectGroup 外网地址组对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    default String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                      String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                      String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress,
                                                      ServiceParamDTO[] insideServiceParam,ServiceParamDTO[] globalServiceParam,
                                                      ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                      InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                      String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup,
                                                      String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup,
                                                      Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成源NAT策略命令行
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
     * @param srcIp 源ip ，需生成命令行
     * @param dstIp 目的ip 需生成命令行
     * @param serviceParam 服务（端口和协议），需要生成命令行
     * @param postSrcIpAddress 转换后源地址
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param eVr 下一跳VRouter
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用 服务对象
     * @param refServiceObjectGroup 引用 服务组对象
     * @param postSrcRefIpAddressObject 转换后源地址对象名
     * @param postSrcRefIpAddressObjectGroup 转换后源地址对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    default String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                 String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                 String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,
                                                 ServiceParamDTO[] serviceParam,IpAddressParamDTO postSrcIpAddress,
                                                 ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                 InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,String eVr,
                                                 String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                 String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                 String[] refServiceObject, String[] refServiceObjectGroup,
                                                 String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup,
                                                 Map<String, Object> map, String[] args) throws Exception {
        return "";
    }


    /**
     * 生成目的NAT策略命令行
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
     * @param serviceParam 服务
     * @param postDstIpAddress  转换后目的地址
     * @param postServiceParam 转换后服务
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup 引用 目的地址组对象
     * @param refServiceObject 引用 服务对象
     * @param refServiceObjectGroup 引用 服务组对象
     * @param postDstRefIpAddressObject 转换后目的地址对象名
     * @param postDstRefIpAddressObjectGroup 转换后目的地址组对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    default String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                 String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                 String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,
                                                 ServiceParamDTO[] serviceParam,IpAddressParamDTO postDstIpAddress,ServiceParamDTO[] postServiceParam,
                                                 ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                 InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                 String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                 String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                 String[] refServiceObject, String[] refServiceObjectGroup,
                                                 String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup,
                                                 Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成bothNAT策略命令行
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
     * @param serviceParam 服务
     * @param postSrcIpAddress 转换后目源地址
     * @param postDstIpAddress 转换后目的地址
     * @param postServiceParam 转换后服务
     * @param srcZone 源域
     * @param dstZone 目的域
     * @param inInterface 进接口
     * @param outInterface 出接口
     * @param eVr
     * @param srcRefIpAddressObject 引用 源地址对象
     * @param srcRefIpAddressObjectGroup 引用 源地址组对象
     * @param dstRefIpAddressObject 引用 目的地址对象
     * @param dstRefIpAddressObjectGroup    引用 目的地址组对象
     * @param refServiceObject 引用 服务对象
     * @param refServiceObjectGroup 引用 服务组对象
     * @param postSrcRefIpAddressObject 转换后源地址对象名
     * @param postSrcRefIpAddressObjectGroup 转换后源地址组对象名
     * @param postDstRefIpAddressObject 转换后目的地址对象名
     * @param postDstRefIpAddressObjectGroup 转换后目的地址对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    default String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                    String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                    String swapRuleNameId, IpAddressParamDTO srcIp, IpAddressParamDTO dstIp,ServiceParamDTO[] serviceParam,
                                                    IpAddressParamDTO postSrcIpAddress,IpAddressParamDTO postDstIpAddress,ServiceParamDTO[] postServiceParam,
                                                    ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                    InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,String eVr,
                                                    String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                    String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                    String[] refServiceObject, String[] refServiceObjectGroup,
                                                    String[] postSrcRefIpAddressObject, String[] postSrcRefIpAddressObjectGroup,
                                                    String[] postDstRefIpAddressObject, String[] postDstRefIpAddressObjectGroup,
                                                    Map<String, Object> map, String[] args) throws Exception {
        return "";
    }


    /**
     *
     * @param natTypeEnum nat策略类型
     * @param id id
     * @param name 名称
     * @param map
     * @param args
     * @return
     */
    default String deleteNatPolicyByIdOrName(NatTypeEnum natTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        return "";
    }

    /**
     * nat地址池对象生成
     * @param statusTypeEnum 状态类型
     * @param poolIp 地址池包含ip
     * @param poolRefIpAddressObject 引用 地址池离散对象
     * @param poolRefIpAddressObjectGroup 引用 地址池整体复用
     * @param map
     * @param args

     */
    default String generateIpAddressPoolCommandLine(StatusTypeEnum statusTypeEnum,String poolName, IpAddressParamDTO poolIp,String[] poolRefIpAddressObject,String[] poolRefIpAddressObjectGroup, Map<String, Object> map, String[] args){
        return "";
    }

}
