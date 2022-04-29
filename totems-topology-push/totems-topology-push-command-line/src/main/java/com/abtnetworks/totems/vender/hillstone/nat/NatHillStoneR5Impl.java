package com.abtnetworks.totems.vender.hillstone.nat;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.enums.SymbolsEnum;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import com.abtnetworks.totems.vender.hillstone.acl.AclHillStoneR5Impl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.abtnetworks.totems.command.line.enums.NatTypeEnum.DST;
import static com.abtnetworks.totems.command.line.enums.NatTypeEnum.SRC;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/25
 */
public class NatHillStoneR5Impl extends AclHillStoneR5Impl{

    @Override
    public String generateNatPolicyName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    @Override
    public String generateStaticNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                     String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                     String swapRuleNameId, IpAddressParamDTO insideAddress, IpAddressParamDTO globalAddress,
                                                     ServiceParamDTO[] insideServiceParam, ServiceParamDTO[] globalServiceParam,
                                                     ZoneParamDTO srcZone, ZoneParamDTO dstZone,
                                                     InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                     String[] insideRefIpAddressObject, String[] insideRefIpAddressObjectGroup,
                                                     String[] globalRefIpAddressObject, String[] globalRefIpAddressObjectGroup,
                                                     Map<String, Object> map, String[] args) throws Exception {
        return StringUtils.EMPTY;
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
    @Override
    public String generateSNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
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
        StringBuffer sNatPolicyCl = new StringBuffer();
        Map<String, String> srcMap = buildNatIpAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup);
        String srcAddress = srcMap.get("addressName");
        String srcAddressCl = srcMap.get("addressCl");
        Map<String, String> dstMap = buildNatIpAddress(statusTypeEnum, dstIp, dstRefIpAddressObject, dstRefIpAddressObjectGroup);
        String dstAddress = dstMap.get("addressName");
        String dstAddressCl = dstMap.get("addressCl");
        Map<String, String> postSrcMap = buildNatIpAddress(statusTypeEnum, postSrcIpAddress, postSrcRefIpAddressObject, postSrcRefIpAddressObjectGroup);
        String postSrcAddress = postSrcMap.get("addressName");
        String postSrcAddressCl = postSrcMap.get("addressCl");
        Map<String, String> serviceMap = buildNatService(statusTypeEnum,serviceParam, refServiceObject, refServiceObjectGroup);
        String serviceName = serviceMap.get("serviceName");
        String serviceCl = serviceMap.get("serviceCl");

        if(StringUtils.isNotEmpty(srcAddressCl)){
            sNatPolicyCl.append(srcAddressCl);
        }
        if(StringUtils.isNotEmpty(dstAddressCl)){
            sNatPolicyCl.append(dstAddressCl);
        }
        if(StringUtils.isNotEmpty(postSrcAddressCl)){
            sNatPolicyCl.append(postSrcAddressCl);
        }
        if(StringUtils.isNotEmpty(serviceCl)){
            sNatPolicyCl.append(serviceCl);
        }
        sNatPolicyCl.append("nat \n");
        sNatPolicyCl.append("snatrule ");
        if(StringUtils.isNotEmpty(id)){
            sNatPolicyCl.append(String.format("id %s ",id));
        }
        if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() == moveSeatEnum.getCode()){
            sNatPolicyCl.append("top ");
        } else if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() !=moveSeatEnum.getCode()
                && MoveSeatEnum.LAST.getCode() != moveSeatEnum.getCode()
                && StringUtils.isNotBlank(swapRuleNameId)){
            sNatPolicyCl.append(String.format("%s %s ",moveSeatEnum.getKey(), swapRuleNameId));
        }
        if(StringUtils.isNotEmpty(srcAddress)){
            sNatPolicyCl.append(String.format("from %s ",srcAddress));
        }
        if(StringUtils.isNotEmpty(dstAddress)){
            sNatPolicyCl.append(String.format("to %s ",dstAddress));
        }
        if(StringUtils.isNotEmpty(serviceName)){
            sNatPolicyCl.append(String.format("service %s ",serviceName));
        }
        if(ObjectUtils.isNotEmpty(outInterface) && StringUtils.isNotEmpty(outInterface.getName())){
            sNatPolicyCl.append(String.format("eif %s ",outInterface.getName()));
        } else if(StringUtils.isNotEmpty(eVr)){
            sNatPolicyCl.append(String.format("evr %s ",eVr));
        }
        if(StringUtils.isNotEmpty(postSrcAddress)){
            if(TotemsIpUtils.isIP(postSrcAddress)){
                sNatPolicyCl.append(String.format("trans-to %s ",postSrcAddress));
            }else{
                sNatPolicyCl.append(String.format("trans-to address-book %s ",postSrcAddress));
            }
        }
        sNatPolicyCl.append("mode dynamicport ");
        if(StringUtils.isNotEmpty(description)){
            sNatPolicyCl.append(String.format("description %s ",description));
        }
        sNatPolicyCl.append(StringUtils.LF).append("exit \n");
        return sNatPolicyCl.toString();
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
    @Override
    public String generateDNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
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
        StringBuffer dNatPolicyCl = new StringBuffer();
        Map<String, String> srcMap = buildNatIpAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup);
        String srcAddress = srcMap.get("addressName");
        String srcAddressCl = srcMap.get("addressCl");
        Map<String, String> dstMap = buildNatIpAddress(statusTypeEnum, dstIp, dstRefIpAddressObject, dstRefIpAddressObjectGroup);
        String dstAddress = dstMap.get("addressName");
        String dstAddressCl = dstMap.get("addressCl");
        Map<String, String> postDstMap = buildNatIpAddress(statusTypeEnum, postDstIpAddress, postDstRefIpAddressObject, postDstRefIpAddressObjectGroup);
        String postDstAddress = postDstMap.get("addressName");
        String postDstAddressCl = postDstMap.get("addressCl");
        Map<String, String> serviceMap = buildNatService(statusTypeEnum,serviceParam, refServiceObject, refServiceObjectGroup);
        String serviceName = serviceMap.get("serviceName");
        String serviceCl = serviceMap.get("serviceCl");

        if(StringUtils.isNotEmpty(srcAddressCl)){
            dNatPolicyCl.append(srcAddressCl);
        }
        if(StringUtils.isNotEmpty(dstAddressCl)){
            dNatPolicyCl.append(dstAddressCl);
        }
        if(StringUtils.isNotEmpty(postDstAddressCl)){
            dNatPolicyCl.append(postDstAddressCl);
        }
        if(StringUtils.isNotEmpty(serviceCl)){
            dNatPolicyCl.append(serviceCl);
        }
        dNatPolicyCl.append("nat \n");
        dNatPolicyCl.append("dnatrule ");
        if(StringUtils.isNotEmpty(id)){
            dNatPolicyCl.append(String.format("id %s ",id));
        }
        if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() == moveSeatEnum.getCode()){
            dNatPolicyCl.append("top ");
        } else if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() != moveSeatEnum.getCode()
                && MoveSeatEnum.LAST.getCode() != moveSeatEnum.getCode()
                && StringUtils.isNotBlank(swapRuleNameId)){
            dNatPolicyCl.append(String.format("%s %s ",moveSeatEnum.getKey(), swapRuleNameId));
        }
        if(inInterface != null && StringUtils.isNotEmpty(inInterface.getName())){
            dNatPolicyCl.append(String.format("ingress-interface %s ",inInterface.getName()));
        }
        if(StringUtils.isNotEmpty(srcAddress)){
            dNatPolicyCl.append(String.format("from %s ",srcAddress));
        }
        if(StringUtils.isNotEmpty(dstAddress)){
            dNatPolicyCl.append(String.format("to %s ",dstAddress));
        }
        if(StringUtils.isNotEmpty(serviceName)){
            dNatPolicyCl.append(String.format("service %s ",serviceName));
        }

        if(StringUtils.isNotEmpty(postDstAddress)){
            dNatPolicyCl.append(String.format("trans-to %s ",postDstAddress));
        }
        if(ArrayUtils.isNotEmpty(postServiceParam)){
            StringBuffer portStrSb = new StringBuffer();
            for (ServiceParamDTO serviceParamDTO : postServiceParam) {
                if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortArray())){
                    portStrSb.append(StringUtils.join(serviceParamDTO.getDstSinglePortArray(), SymbolsEnum.COMMA.getValue())).append(",");
                }
                if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortStrArray())){
                    portStrSb.append(StringUtils.join(serviceParamDTO.getDstSinglePortStrArray(), SymbolsEnum.COMMA.getValue())).append(",");
                }
                if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstRangePortArray())){
                    for (PortRangeDTO portRangeDTO : serviceParamDTO.getDstRangePortArray()) {
                        portStrSb.append(String.format("%s-%s",portRangeDTO.getStart(),portRangeDTO.getEnd())).append(SymbolsEnum.COMMA.getValue());
                    }
                }
            }
            String portStr = portStrSb.toString();
            if(portStr.endsWith(SymbolsEnum.COMMA.getValue())){
                portStr = portStr.substring(0,portStr.length()-1);
            }
            if(StringUtils.isNotBlank(portStr)){
                dNatPolicyCl.append(String.format("port %s ",portStr));
            }
        }
        if(StringUtils.isNotEmpty(description)){
            dNatPolicyCl.append(String.format("description %s ",description));
        }
        dNatPolicyCl.append(StringUtils.LF).append("exit \n");
        return dNatPolicyCl.toString();
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
     * @param postSrcRefIpAddressObject 转换后源地址对象名
     * @param postSrcRefIpAddressObjectGroup 转换后源地址组对象名
     * @param postDstRefIpAddressObject 转换后目的地址对象名
     * @param postDstRefIpAddressObjectGroup 转换后目的地址对象名
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    @Override
    public String generateBothNatPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
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
        StringBuffer bothNatPolicyCl = new StringBuffer();
        Map<String, String> srcMap = buildNatIpAddress(statusTypeEnum, srcIp, srcRefIpAddressObject, srcRefIpAddressObjectGroup);
        String srcAddress = srcMap.get("addressName");
        String srcAddressCl = srcMap.get("addressCl");
        Map<String, String> dstMap = buildNatIpAddress(statusTypeEnum, dstIp, dstRefIpAddressObject, dstRefIpAddressObjectGroup);
        String dstAddress = dstMap.get("addressName");
        String dstAddressCl = dstMap.get("addressCl");
        Map<String, String> postSrcMap = buildNatIpAddress(statusTypeEnum, postSrcIpAddress, postSrcRefIpAddressObject, postSrcRefIpAddressObjectGroup);
        String postSrcAddress = postSrcMap.get("addressName");
        String postSrcAddressCl = postSrcMap.get("addressCl");
        Map<String, String> postDstMap = buildNatIpAddress(statusTypeEnum, postDstIpAddress, postDstRefIpAddressObject, postDstRefIpAddressObjectGroup);
        String postDstAddress = postDstMap.get("addressName");
        String postDstAddressCl = postDstMap.get("addressCl");
        Map<String, String> serviceMap = buildNatService(statusTypeEnum,serviceParam, refServiceObject, refServiceObjectGroup);
        String serviceName = serviceMap.get("serviceName");
        String serviceCl = serviceMap.get("serviceCl");

        if(StringUtils.isNotEmpty(srcAddressCl)){
            bothNatPolicyCl.append(srcAddressCl);
        }
        if(StringUtils.isNotEmpty(dstAddressCl)){
            bothNatPolicyCl.append(dstAddressCl);
        }
        if(StringUtils.isNotEmpty(postSrcAddressCl)){
            bothNatPolicyCl.append(postSrcAddressCl);
        }
        if(StringUtils.isNotEmpty(postDstAddressCl)){
            bothNatPolicyCl.append(postDstAddressCl);
        }
        if(StringUtils.isNotEmpty(serviceCl)){
            bothNatPolicyCl.append(serviceCl);
        }
        bothNatPolicyCl.append("nat \n");
        // snatrule
        bothNatPolicyCl.append("snatrule ");
//        if(StringUtils.isNotEmpty(bothNatPolicyParamDTO.getId())){
//            bothNatPolicyCl.append(String.format("id %s ",bothNatPolicyParamDTO.getId()));
//        }
        if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() == moveSeatEnum.getCode()){
            bothNatPolicyCl.append("top ");
        } else if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() != moveSeatEnum.getCode()
                && MoveSeatEnum.LAST.getCode() != moveSeatEnum.getCode()
                && StringUtils.isNotBlank(swapRuleNameId)){
            bothNatPolicyCl.append(String.format("%s %s ",moveSeatEnum.getKey(), swapRuleNameId));
        }
        if(StringUtils.isNotEmpty(srcAddress)){
            bothNatPolicyCl.append(String.format("from %s ",srcAddress));
        }
        if(StringUtils.isNotEmpty(dstAddress)){
            bothNatPolicyCl.append(String.format("to %s ",dstAddress));
        }
        if(StringUtils.isNotEmpty(serviceName)){
            bothNatPolicyCl.append(String.format("service %s ",serviceName));
        }
        if(ObjectUtils.isNotEmpty(outInterface) &&  StringUtils.isNotEmpty(outInterface.getName())){
            bothNatPolicyCl.append(String.format("eif %s ",outInterface.getName()));
        } else if(StringUtils.isNotEmpty(eVr)){
            bothNatPolicyCl.append(String.format("evr %s ",eVr));
        }
        if(StringUtils.isNotEmpty(postSrcAddress)){
            if(TotemsIpUtils.isIP(postSrcAddress)){
                bothNatPolicyCl.append(String.format("trans-to %s ",postSrcAddress));
            }else{
                bothNatPolicyCl.append(String.format("trans-to address-book %s ",postSrcAddress));
            }
        }
        bothNatPolicyCl.append("mode dynamicport ");
        if(StringUtils.isNotEmpty(description)){
            bothNatPolicyCl.append(String.format("description %s ",description));
        }
        bothNatPolicyCl.append(StringUtils.LF);
        bothNatPolicyCl.append(StringUtils.LF);

        bothNatPolicyCl.append("dnatrule ");
//        if(StringUtils.isNotEmpty(bothNatPolicyParamDTO.getId())){
//            bothNatPolicyCl.append(String.format("id %s ",bothNatPolicyParamDTO.getId()));
//        }
        if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() == moveSeatEnum.getCode()){
            bothNatPolicyCl.append("top ");
        } else if(moveSeatEnum != null && MoveSeatEnum.FIRST.getCode() != moveSeatEnum.getCode()
                && MoveSeatEnum.LAST.getCode() != moveSeatEnum.getCode()
                && StringUtils.isNotBlank(swapRuleNameId)){
            bothNatPolicyCl.append(String.format("%s %s ",moveSeatEnum.getKey(), swapRuleNameId));
        }
        if(inInterface != null && StringUtils.isNotEmpty(inInterface.getName())){
            bothNatPolicyCl.append(String.format("ingress-interface %s ",inInterface.getName()));
        }
        if(StringUtils.isNotEmpty(srcAddress)){
            bothNatPolicyCl.append(String.format("from %s ",srcAddress));
        }
        if(StringUtils.isNotEmpty(dstAddress)){
            bothNatPolicyCl.append(String.format("to %s ",dstAddress));
        }
        if(StringUtils.isNotEmpty(serviceName)){
            bothNatPolicyCl.append(String.format("service %s ",serviceName));
        }

        if(StringUtils.isNotEmpty(postDstAddress)){
            bothNatPolicyCl.append(String.format("trans-to %s ",postDstAddress));
        }
        if(ArrayUtils.isNotEmpty(postServiceParam)){
            StringBuffer portStrSb = new StringBuffer();
            for (ServiceParamDTO serviceParamDTO : postServiceParam) {
                if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortArray())){
                    portStrSb.append(StringUtils.join(serviceParamDTO.getDstSinglePortArray(), SymbolsEnum.COMMA.getValue())).append(",");
                }
                if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstSinglePortStrArray())){
                    portStrSb.append(StringUtils.join(serviceParamDTO.getDstSinglePortStrArray(), SymbolsEnum.COMMA.getValue())).append(",");
                }
                if(ArrayUtils.isNotEmpty(serviceParamDTO.getDstRangePortArray())){
                    for (PortRangeDTO portRangeDTO : serviceParamDTO.getDstRangePortArray()) {
                        portStrSb.append(String.format("%s-%s",portRangeDTO.getStart(),portRangeDTO.getEnd())).append(SymbolsEnum.COMMA.getValue());
                    }
                }
            }
            String portStr = portStrSb.toString();
            if(portStr.endsWith(SymbolsEnum.COMMA.getValue())){
                portStr = portStr.substring(0,portStr.length()-1);
            }
            if(StringUtils.isNotBlank(portStr)){
                bothNatPolicyCl.append(String.format("port %s ",portStr));
            }
        }
        if(StringUtils.isNotEmpty(description)){
            bothNatPolicyCl.append(String.format("description %s ",description));
        }
        bothNatPolicyCl.append(StringUtils.LF).append("exit \n");
        return bothNatPolicyCl.toString();
    }

    private Map<String,String> buildNatIpAddress(StatusTypeEnum statusTypeEnum, IpAddressParamDTO ipAddressParamDTO, String[] refIpAddressObject, String[] refIpAddressObjectGroup) throws Exception {
        String addressName =  "any";
        StringBuffer stringBuffer = new StringBuffer();
        if(ipAddressParamDTO  != null){
            if(ipAddressParamDTO.getSingleIpArray() != null && ipAddressParamDTO.getSingleIpArray().length == 1
                    && ArrayUtils.isEmpty(ipAddressParamDTO.getRangIpArray()) && ArrayUtils.isEmpty(ipAddressParamDTO.getSubnetIntIpArray()) && ArrayUtils.isEmpty(ipAddressParamDTO.getSubnetStrIpArray())
                    && ArrayUtils.isEmpty(ipAddressParamDTO.getHosts()) && ArrayUtils.isEmpty(refIpAddressObject) && ArrayUtils.isEmpty(refIpAddressObjectGroup)){
                // 只有一个ip,直接使用
                addressName = ipAddressParamDTO.getSingleIpArray()[0];
            } else {
                if(ArrayUtils.isNotEmpty(refIpAddressObjectGroup)){
                    addressName = this.createIpAddressObjectGroupName(ipAddressParamDTO.getSingleIpArray(), ipAddressParamDTO.getRangIpArray(), ipAddressParamDTO.getSubnetIntIpArray(), ipAddressParamDTO.getSubnetStrIpArray(),
                            null, ipAddressParamDTO.getHosts(), refIpAddressObject, refIpAddressObjectGroup, null, null);
                    stringBuffer.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipAddressParamDTO.getIpTypeEnum(),addressName,null,ipAddressParamDTO.getSingleIpArray(),ipAddressParamDTO.getRangIpArray(),ipAddressParamDTO.getSubnetIntIpArray(),ipAddressParamDTO.getSubnetStrIpArray(),
                            null,ipAddressParamDTO.getHosts(),refIpAddressObject, refIpAddressObjectGroup,null,null,null,null,null));
                }else{
                    addressName = this.createIpAddressObjectNameByParamDTO(ipAddressParamDTO.getSingleIpArray(), ipAddressParamDTO.getRangIpArray(), ipAddressParamDTO.getSubnetIntIpArray(), ipAddressParamDTO.getSubnetStrIpArray(),
                            ipAddressParamDTO.getHosts(), refIpAddressObject, null, null);
                    stringBuffer.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,ipAddressParamDTO.getIpTypeEnum(),addressName,null,ipAddressParamDTO.getSingleIpArray(),ipAddressParamDTO.getRangIpArray(),ipAddressParamDTO.getSubnetIntIpArray(),ipAddressParamDTO.getSubnetStrIpArray(),
                            null,ipAddressParamDTO.getHosts(),refIpAddressObject, null,null,null,null,null));
                }
            }
        } else {
            if(ArrayUtils.getLength(refIpAddressObject) + ArrayUtils.getLength(refIpAddressObjectGroup) == 1){
                if(ArrayUtils.getLength(refIpAddressObject) == 1){
                    addressName =  refIpAddressObject[0];
                } else {
                    addressName =  refIpAddressObjectGroup[0];
                }
            } else if(ArrayUtils.getLength(refIpAddressObject) + ArrayUtils.getLength(refIpAddressObjectGroup) > 1){
                addressName = this.createIpAddressObjectGroupName(null, null, null, null,
                        null, null, refIpAddressObject, refIpAddressObjectGroup, null, null);
                stringBuffer.append(this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,null,addressName,null,null,null,null,null,
                        null,null,refIpAddressObject, refIpAddressObjectGroup,null,null,null,null,null));
            }
        }
        Map<String,String> map = new HashMap<>();
        map.put("addressName",addressName);
        map.put("addressCl",stringBuffer.toString());
        return map;
    }

    private Map<String,String> buildNatService(StatusTypeEnum statusTypeEnum,ServiceParamDTO[] serviceParam,String[] serviceObjectNameRefArray,String[] serviceObjectGroupNameRefArray) throws Exception {
        String serviceName = "any";
        StringBuffer stringBuffer = new StringBuffer();
        if(ArrayUtils.isNotEmpty(serviceParam)){
            if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray) || ArrayUtils.isNotEmpty(serviceObjectGroupNameRefArray) || ArrayUtils.getLength(serviceParam)>1){
                serviceName = this.createServiceObjectGroupName(Arrays.asList(serviceParam), serviceObjectNameRefArray, serviceObjectGroupNameRefArray, null, null);
                stringBuffer.append(this.generateServiceObjectGroupCommandLine(statusTypeEnum,serviceName,null,null,Arrays.asList(serviceParam),null,serviceObjectNameRefArray, serviceObjectGroupNameRefArray,null,null));
            } else{
                serviceName = this.createServiceObjectName(Arrays.asList(serviceParam), null, null);
                stringBuffer.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceName, null, null, Arrays.asList(serviceParam), null, null, null));
            }
        } else {
            if(ArrayUtils.getLength(serviceObjectNameRefArray) + ArrayUtils.getLength(serviceObjectGroupNameRefArray) == 1){
                if(ArrayUtils.isNotEmpty(serviceObjectNameRefArray)){
                    serviceName = serviceObjectNameRefArray[0];
                }else{
                    serviceName = serviceObjectGroupNameRefArray[0];
                }
            } else if(ArrayUtils.getLength(serviceObjectNameRefArray) + ArrayUtils.getLength(serviceObjectGroupNameRefArray) > 1){
                serviceName = this.createServiceObjectGroupName(null,serviceObjectNameRefArray,serviceObjectGroupNameRefArray,null,null);
                stringBuffer.append(this.generateServiceObjectGroupCommandLine(statusTypeEnum, serviceName, null, null, null, null, serviceObjectNameRefArray, serviceObjectGroupNameRefArray, null, null));
            }
        }
        Map<String,String> map = new HashMap<>();
        map.put("serviceName",serviceName);
        map.put("serviceCl",stringBuffer.toString());
        return map;
    }

    @Override
    public String generatePreCommandline(Boolean isVsys, String vsysName, Map<String, Object> map, String[] args) {
        StringBuffer preCommandline = new StringBuffer();
        String s = super.generatePreCommandline(isVsys, vsysName, map, args);
        preCommandline.append(s);

        return preCommandline.toString();
    }

    @Override
    public String deleteNatPolicyByIdOrName(NatTypeEnum natTypeEnum, String id, String name, Map<String, Object> map, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("nat").append(StringUtils.LF);
        if(StringUtils.isNotBlank(id)){
            String natKey =  "";
            if(DST.equals(natTypeEnum)){
                natKey = "dnatrule";
            }else if(SRC.equals(natTypeEnum)){
                natKey = "snatrule";
            }
            if(StringUtils.isNotBlank(natKey)){
                sb.append(String.format("no %s id %s ",natKey,id)).append(StringUtils.LF);
                return sb.toString();
            }

        }

        return null;
    }

}
