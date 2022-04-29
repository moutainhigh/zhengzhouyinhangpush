package com.abtnetworks.totems.vender.h3c.security;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.common.constants.TimeConstants;
import com.abtnetworks.totems.common.lang.TotemsTimeUtils;
import com.abtnetworks.totems.common.network.TotemsIpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author: WangCan
 * @Description h3cV7 OP 命令行生成
 * @Date: 2021/5/10
 */
public class SecurityH3cSecPathV7OPImpl extends SecurityH3cSecPathV7Impl {

    /**
     * 生成安全策略
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
     * @param srcIpDto
     * @param dstIpDto
     * @param serviceParam
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
     * rule [ rule-id ]
     * { drop | pass | inspect app-profile-name }
     * [
     * [ source-ip { object-group-name | any } ]
     * [ destination-ip { object-group-name | any } ]
     * [ service { object-group-name | any } ]
     * [vrf vrf-name ]
     * [ application application-name ]
     * [ app-group app-group-name ]
     * [ counting ]
     * [ disable ]
     * [ logging ]
     * [ track [ negative ] track-entry-number ]
     * [ time-range time-range-name ]
     * ] *
     * @throws Exception
     */
    @Override
    public String generateSecurityPolicyCommandLine(StatusTypeEnum statusTypeEnum, String groupName, String name, String id, String action, String description,
                                                    String logFlag, String ageingTime, String refVirusLibrary, MoveSeatEnum moveSeatEnum,
                                                    String swapRuleNameId, IpAddressParamDTO srcIpDto, IpAddressParamDTO dstIpDto, ServiceParamDTO[] serviceParam,
                                                    AbsoluteTimeParamDTO absoluteTimeParamDTO,PeriodicTimeParamDTO periodicTimeParamDTO,
                                                    ZoneParamDTO srcZone, ZoneParamDTO dstZone, InterfaceParamDTO inInterface, InterfaceParamDTO outInterface,
                                                    String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                                    String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,
                                                    String[] refServiceObject, String[] refServiceObjectGroup,
                                                    String[] refTimeObject,
                                                    Map<String, Object> map, String[] args) throws Exception {
        //处理时间对象(是允许指定一个时间对象)
        String newTimeObjectName = null;
        String newTimeCommandLine = null;
        if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(newTimeObjectName,null,absoluteTimeParamDTO,map,null);
        } else if(ObjectUtils.isNotEmpty(periodicTimeParamDTO)){
            newTimeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(newTimeObjectName,null,periodicTimeParamDTO,map,null);
        }

        StringBuffer securityPolicyCl = new StringBuffer();
        // 处理地址对象
        String srcIpObjectName = null;
        if(ObjectUtils.isNotEmpty(srcIpDto) || (ArrayUtils.getLength(srcRefIpAddressObject) + ArrayUtils.getLength(srcRefIpAddressObjectGroup) > 1)){
            //生成src地址对象命令行
            srcIpObjectName = this.createIpAddressObjectNameByParamDTO(srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),srcIpDto.getHosts(),ArrayUtils.addAll(srcRefIpAddressObject,srcRefIpAddressObjectGroup),map,args);
            securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,srcIpDto.getIpTypeEnum(),srcIpObjectName,null,srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),
                    null,srcIpDto.getHosts(),ArrayUtils.addAll(srcRefIpAddressObject,srcRefIpAddressObjectGroup),null,null,null,map,args));

        }

        String dstIpObjectName = null;
        if(ObjectUtils.isNotEmpty(dstIpDto) || (ArrayUtils.getLength(dstRefIpAddressObject) + ArrayUtils.getLength(dstRefIpAddressObjectGroup) > 1)){
            //生成dst地址对象命令行
            dstIpObjectName = this.createIpAddressObjectNameByParamDTO(dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),dstIpDto.getHosts(),ArrayUtils.addAll(dstRefIpAddressObject,dstRefIpAddressObjectGroup),map,args);
            securityPolicyCl.append(this.generateIpAddressObjectCommandLine(statusTypeEnum,dstIpDto.getIpTypeEnum(),dstIpObjectName,null,dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),
                    null,dstIpDto.getHosts(),ArrayUtils.addAll(dstRefIpAddressObject,dstRefIpAddressObjectGroup),null,null,null,map,args));

        }

        // 处理服务对象
        String serviceObjectName = null;
        if(ArrayUtils.isNotEmpty(serviceParam)){
            List<ServiceParamDTO> serviceList = Arrays.asList(serviceParam);
            serviceObjectName = this.createServiceObjectName(Arrays.asList(serviceParam), null, null);
            if(map == null){
                map = new HashMap<>();
            }
            map.put("serviceObjectNameRefArray",refServiceObject);
            map.put("serviceObjectGroupNameRefArray",refServiceObjectGroup);
            securityPolicyCl.append(this.generateServiceObjectCommandLine(statusTypeEnum, serviceObjectName, null, null, serviceList, null, map,args));
        }

        // 时间对象命令行
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            securityPolicyCl.append(newTimeCommandLine);
        }
        securityPolicyCl.append(StringUtils.LF);
        securityPolicyCl.append(String.format("object-policy ip %s\n", CommonEnums.H3C_OP_OBJECT_POLICY));
        securityPolicyCl.append("security-policy ip \n");
        if ("deny".equalsIgnoreCase(action)) {
            securityPolicyCl.append("rule drop ");
        } else {
            securityPolicyCl.append("rule pass ");
        }
        if(StringUtils.isNotBlank(srcIpObjectName)){
            securityPolicyCl.append(String.format("source-ip %s ",srcIpObjectName));
        }
        if(StringUtils.isNotBlank(dstIpObjectName)){
            securityPolicyCl.append(String.format("destination-ip %s ",dstIpObjectName));
        }
        if(StringUtils.isNotBlank(serviceObjectName)){
            securityPolicyCl.append(String.format("service %s ",serviceObjectName));
        }

        if(StringUtils.isNotBlank(newTimeObjectName)){
            securityPolicyCl.append(String.format("time-range %s ",newTimeObjectName));
        } else if(ArrayUtils.isNotEmpty(refTimeObject)){
            // 只能使用一个时间对象
            securityPolicyCl.append(String.format("time-range %s ",refTimeObject[0]));
        }
        securityPolicyCl.append(StringUtils.LF);
        securityPolicyCl.append("quit \n");
        String srcZoneName = "any";
        String dstZoneName = "any";
        if(ObjectUtils.isNotEmpty(srcZone)){
            if(StringUtils.isNotBlank(srcZone.getName())){
                srcZoneName = srcZone.getName();
            } else if(ArrayUtils.isNotEmpty(srcZone.getNameArray())){
                srcZoneName = srcZone.getNameArray()[0];
            }
        }
        if(ObjectUtils.isNotEmpty(dstZone)){
            if(StringUtils.isNotBlank(dstZone.getName())){
                dstZoneName = dstZone.getName();
            } else if(ArrayUtils.isNotEmpty(dstZone.getNameArray())){
                dstZoneName = dstZone.getNameArray()[0];
            }
        }
        securityPolicyCl.append("zone-pair security").append(String.format("source %s ",srcZoneName));
        securityPolicyCl.append(String.format("destination %s \n",dstZoneName));

        if(StringUtils.isNotBlank(name)){
            securityPolicyCl.append(String.format("object-policy apply ip %s \n",name));
        }
        securityPolicyCl.append("quit");
        securityPolicyCl.append(StringUtils.LF);
        return securityPolicyCl.toString();
    }

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        StringBuffer deleteStr = new StringBuffer();
        if(RuleIPTypeEnum.IP6.name().equalsIgnoreCase(ipTypeEnum.name())){
            deleteStr.append("security-policy ipv6\n");
        }else{
            deleteStr.append("security-policy ip\n");
        }
        if(StringUtils.isNotBlank(name)){
            deleteStr.append(String.format(" undo rule name %s \n",name));
        } else if(StringUtils.isNotBlank(id)){
            return String.format(" undo rule %s \n",id);
        }
        deleteStr.append("quit\n");
        return deleteStr.toString();
    }

    @Override
    public String generateIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("ip route-static  %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String deleteIpv4RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description,Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 32;
        }
        return String.format("undo ip route-static %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String generateIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("ipv6 route-static %s %s %s\n",ip,mask,"NULL0");
    }

    @Override
    public String deleteIpv6RoutingCommandLine(String ip,Integer mask,String nextHop,String netDoor,String distance,String weight,String description, Map<String, Object> map, String[] args) throws Exception {
        if(mask == null){
            mask = 128;
        }
        return String.format("undo ipv6 route %s %s %s\n",ip,mask,"NULL0");
    }

}
