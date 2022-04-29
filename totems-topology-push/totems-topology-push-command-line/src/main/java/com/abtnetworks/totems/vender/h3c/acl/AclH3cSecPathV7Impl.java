package com.abtnetworks.totems.vender.h3c.acl;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV7Impl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description h3cV5 命令行生成
 * @Date: 2021/4/27
 */
public class AclH3cSecPathV7Impl extends SecurityH3cSecPathV7Impl {

    /**
     * 查找源域，目的域间策略的ACL策略集
     * @param strName
     * @param dstName
     * @return
     */
    @Override
    public String getDomainAcl(String strName, String dstName){
        return String.format("display packet-filter zone-pair security source %s destination %s \n",strName,dstName);
    }

    /**
     * 应用名 组装
     * @param aclType basic/advanced
     * @param name
     * @return
     */
    @Override
    public String generateAclName(String aclType,String name,RuleIPTypeEnum ruleIPTypeEnum){
        StringBuffer preCommandline = new StringBuffer();
        if(RuleIPTypeEnum.IP6.getName().equalsIgnoreCase(ruleIPTypeEnum.getName())){
            preCommandline.append(String.format("access-list ipv6 %s %s \n",aclType,name));
        }else {
            preCommandline.append(String.format("access-list %s %s \n",aclType,name));
        }
        return preCommandline.toString();
    }

    /**
     * 应用名 组装
     * @param description
     * @return
     */
    @Override
    public String generateAclDescription(String description ){
        if(StringUtils.isBlank(description)){
            return StringUtils.EMPTY;
        }
        return String.format(" description %s \n",description);
    }

    /**
     * acl 后置命令行
     * @return
     */
    @Override
    public String generateAclPost(){
        return "quit \n";
    }

    /**
     *
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
    public String generateAclPolicyCommandLine(StatusTypeEnum statusTypeEnum, String aclType,String name,RuleIPTypeEnum ipTypeEnum,
                                               String action,String description,
                                               IpAddressParamDTO srcIpDto, IpAddressParamDTO dstIpDto,
                                               ServiceParamDTO serviceParam,AbsoluteTimeParamDTO absoluteTimeParamDTO,PeriodicTimeParamDTO periodicTimeParamDTO,
                                               String[] srcRefIpAddressObject, String[] srcRefIpAddressObjectGroup,
                                               String[] dstRefIpAddressObject, String[] dstRefIpAddressObjectGroup,String[] refTimeObject,
                                               Map<String, Object> map, String[] args) throws Exception {
        StringBuffer aclCommandline = new StringBuffer();
        String srcName = null;
        String srcLine = null;
        if(ObjectUtils.isNotEmpty(srcIpDto)){
            if(srcIpDto == null){
                srcIpDto = new IpAddressParamDTO();
            }
            srcName =this.createIpAddressObjectNameByParamDTO(srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),
                    srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),srcIpDto.getHosts(),ArrayUtils.addAll(srcRefIpAddressObject,srcRefIpAddressObjectGroup),map,args);
            if(StatusTypeEnum.ADD.getCode().equalsIgnoreCase(statusTypeEnum.getCode())){
                srcLine = this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,srcName,null,
                        srcIpDto.getSingleIpArray(),srcIpDto.getRangIpArray(),srcIpDto.getSubnetIntIpArray(),srcIpDto.getSubnetStrIpArray(),
                        null,null,srcRefIpAddressObject,srcRefIpAddressObjectGroup,description,null,null, map,args);
            }
        } else {
            srcName = createIpAddressObjectGroupName(null, null, null, null, null, null, srcRefIpAddressObject, srcRefIpAddressObjectGroup, null, null);
        }

        String dstName = null;
        String dstLine = null;
        if(ObjectUtils.isNotEmpty(dstIpDto)){
            if(dstIpDto == null){
                dstIpDto = new IpAddressParamDTO();
            }
            dstName=this.createIpAddressObjectNameByParamDTO(dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),
                    dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),dstIpDto.getHosts(),ArrayUtils.addAll(dstRefIpAddressObject,dstRefIpAddressObjectGroup),map,args);
            if(StatusTypeEnum.ADD.getCode().equalsIgnoreCase(statusTypeEnum.getCode())){
                dstLine = this.generateIpAddressObjectGroupCommandLine(statusTypeEnum,ipTypeEnum,dstName,null,
                        dstIpDto.getSingleIpArray(),dstIpDto.getRangIpArray(),dstIpDto.getSubnetIntIpArray(),dstIpDto.getSubnetStrIpArray(),
                        null,null,dstRefIpAddressObject,dstRefIpAddressObjectGroup,description,null,null,map,args);
            }
        } else {
            dstName = createIpAddressObjectGroupName(null, null, null, null, null, null, dstRefIpAddressObject, dstRefIpAddressObjectGroup, null, null);
        }
        //处理时间对象(是允许指定一个时间对象)
        String timeObjectName = null;
        String newTimeCommandLine = null;
        if(ArrayUtils.isNotEmpty(refTimeObject)){
            timeObjectName = refTimeObject[0];
        } else if(ObjectUtils.isNotEmpty(absoluteTimeParamDTO)){
            timeObjectName = this.createTimeObjectNameByAbsolute(absoluteTimeParamDTO,map,args);
            newTimeCommandLine = this.generateAbsoluteTimeCommandLine(timeObjectName,null,absoluteTimeParamDTO,map,null);
        } else if(ObjectUtils.isNotEmpty(periodicTimeParamDTO)){
            timeObjectName = this.createTimeObjectNameByPeriodic(periodicTimeParamDTO,map,args);
            newTimeCommandLine = this.generatePeriodicTimeCommandLine(timeObjectName,null,periodicTimeParamDTO,map,null);
        }
        if(StringUtils.isNotBlank(srcLine)){
            aclCommandline.append(srcLine);
        }
        if(StringUtils.isNotBlank(dstLine)){
            aclCommandline.append(dstLine);
        }
        if(StringUtils.isNotBlank(newTimeCommandLine)){
            aclCommandline.append(newTimeCommandLine);
        }
        aclCommandline.append(this.createAclRuleCommandLine(statusTypeEnum,action,srcName,dstName,serviceParam,timeObjectName));
        return aclCommandline.toString();
    }

    /**
     * 生产添加的ACLRule命令
     * @param permit 拒绝 允许
     * @param srcName  源Name
     * @param dstName 目的Name
     * @return
     */
    public String createAclRuleCommandLine( StatusTypeEnum statusTypeEnum,String permit ,String srcName, String dstName,ServiceParamDTO serviceParam,String timeObjectName){
        StringBuffer preCommandline = new StringBuffer();
        String protocolType = "";
        if(serviceParam == null || serviceParam.getProtocol() == null || ProtocolTypeEnum.ANY.getType().equalsIgnoreCase(serviceParam.getProtocol().getType()) || ProtocolTypeEnum.PROTOCOL.getType().equalsIgnoreCase(serviceParam.getProtocol().getType())){
            protocolType = "ip";
        } else {
            protocolType = serviceParam.getProtocol().getType();
        }
        //对象组
        if(StatusTypeEnum.ADD.getCode().equals(statusTypeEnum.getCode())){
            preCommandline.append(String.format(" rule %s %s ",permit.toLowerCase(),protocolType));
        }
        if(StatusTypeEnum.DELETE.getCode().equals(statusTypeEnum.getCode())){
            preCommandline.append(String.format(" undo rule %s %s ",permit.toLowerCase(),protocolType));
        }

        if(StringUtils.isNotBlank(srcName) && !"any".equalsIgnoreCase(srcName)){
            preCommandline.append(String.format(" source object-group %s",srcName));
        }else {
            preCommandline.append(" source any");
        }
        if(StringUtils.isNotBlank(dstName) && !"any".equalsIgnoreCase(dstName)){
            preCommandline.append(String.format(" destination object-group %s",dstName));
        }else {
            preCommandline.append(" destination any");
        }
        if(serviceParam == null){
            serviceParam = new ServiceParamDTO();
        }
        //端口判断添加
        if (ArrayUtils.isNotEmpty(serviceParam.getDstRangePortArray())) {
            preCommandline.append(String.format(" destination-port range %s %s",serviceParam.getDstRangePortArray()[0].getStart(),serviceParam.getDstRangePortArray()[0].getEnd()));
        }else if(ArrayUtils.isNotEmpty(serviceParam.getDstSinglePortArray())){
            preCommandline.append(String.format(" destination-port eq %s",serviceParam.getDstSinglePortArray()[0]) );
        }else if(ArrayUtils.isNotEmpty(serviceParam.getDstSinglePortStrArray())){
            preCommandline.append(String.format(" destination-port eq %s",serviceParam.getDstSinglePortStrArray()[0]) );
        }
        if(StringUtils.isNotBlank(timeObjectName)){
            preCommandline.append(String.format(" time-range %s ",timeObjectName));
        }

        preCommandline.append(StringUtils.LF);
        return preCommandline.toString();
    }
}
