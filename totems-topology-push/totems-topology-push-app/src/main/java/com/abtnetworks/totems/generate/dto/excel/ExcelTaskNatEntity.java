package com.abtnetworks.totems.generate.dto.excel;


import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiModel("Nat策略Excel工单模型")
public class ExcelTaskNatEntity {
    private static Logger logger = LoggerFactory.getLogger(ExcelTaskNatEntity.class);

    @ApiModelProperty("ID")
    String id;

    @ApiModelProperty("主题（工单号）")
    String theme;

    @ApiModelProperty("设备IP")
    String deviceIp;

    @ApiModelProperty("Nat类型")
    String natType;

    @ApiModelProperty("ip类型 0:ipv4 1:ipv6 2:url")
    String ipType;

    @ApiModelProperty("源地址转换前")
    String preSrcAddress;

    @ApiModelProperty("源地址转换后")
    String postSrcAddress;

    @ApiModelProperty("目的地址转换前")
    String preDstAddress;

    @ApiModelProperty("目的地址转换后")
    String postDstAddress;

    @ApiModelProperty("协议")
    String protocol;

    @ApiModelProperty("端口转换前")
    String preDstPorts;

    @ApiModelProperty("端口转换后")
    String postDstPorts;

    @ApiModelProperty("入接口安全域")
    String srcZone;

    @ApiModelProperty("出接口安全域")
    String dstZone;

    @ApiModelProperty("入接口")
    String inDevItf;

    @ApiModelProperty("出接口")
    String outDevItf;

    @ApiModelProperty("申请人")
    String userName;

    @ApiModelProperty("解析后转换前服务对象")
    List<ServiceDTO> preServiceList;

    @ApiModelProperty("解析后转换后服务对象")
    List<ServiceDTO> postServiceList;

    @ApiModelProperty("策略描述")
    String description;

    @ApiModelProperty("工单备注")
    String remark;

    @ApiModelProperty("源地址描述")
    String srcIpSystem;

    @ApiModelProperty("目的地址描述")
    String dstIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postSrcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    String postDstIpSystem;



    @ExcelField(title="序号", type=2, align = 2, sort= 1)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ExcelField(title="主题（工单号）", type=2, align = 2, sort= 10)
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @ExcelField(title="Nat防火墙IP", type = 2, align = 2, sort = 15)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @ExcelField(title="Nat类型", type=2, align = 2, sort= 16)
    public String getNatType() {
        return natType;
    }

    public void setNatType(String natType) {
        this.natType = natType;
    }

    @ExcelField(title="IP类型",type = 2,align=2, sort=20)
    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    @ExcelField(title="转换前源地址", type=2, align = 2, sort= 30)
    public String getPreSrcAddress() {
        return preSrcAddress;
    }

    public void setPreSrcAddress(String preSrcAddress) {
        this.preSrcAddress = preSrcAddress;
    }

    @ExcelField(title="源地址描述",type = 2,align=2, sort=35)
    public String getSrcIpSystem() {
        return srcIpSystem;
    }

    public void setSrcIpSystem(String srcIpSystem) {
        this.srcIpSystem = srcIpSystem;
    }

    @ExcelField(title="转换后源地址", type=2, align = 2, sort= 40)
    public String getPostSrcAddress() {
        return postSrcAddress;
    }

    public void setPostSrcAddress(String postSrcAddress) {
        this.postSrcAddress = postSrcAddress;
    }


    @ExcelField(title="目的地址描述",type = 2,align=2, sort=45)
    public String getPostSrcIpSystem() {
        return postSrcIpSystem;
    }

    public void setPostSrcIpSystem(String postSrcIpSystem) {
        this.postSrcIpSystem = postSrcIpSystem;
    }


    @ExcelField(title="转换前目的地址", type=2, align = 2, sort= 50)
    public String getPreDstAddress() {
        return preDstAddress;
    }

    public void setPreDstAddress(String preDstAddress) {
        this.preDstAddress = preDstAddress;
    }


    @ExcelField(title="目的地址描述",type = 2,align=2, sort=55)
    public String getDstIpSystem() {
        return dstIpSystem;
    }

    public void setDstIpSystem(String dstIpSystem) {
        this.dstIpSystem = dstIpSystem;
    }

    @ExcelField(title="转换后目的地址", type=2, align = 2, sort= 60)
    public String getPostDstAddress() {
        return postDstAddress;
    }

    public void setPostDstAddress(String postDstAddress) {
        this.postDstAddress = postDstAddress;
    }

    @ExcelField(title="转换后目的描述", type=2, align = 2, sort= 62)
    public String getPostDstIpSystem() {
        return postDstIpSystem;
    }

    public void setPostDstIpSystem(String postDstIpSystem) {
        this.postDstIpSystem = postDstIpSystem;
    }

    @ExcelField(title="协议", type=2, align = 2, sort= 65)
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @ExcelField(title="端口转换前", type=2, align = 2, sort= 70)
    public String getPrePorts() {
        return preDstPorts;
    }

    public void setPrePorts(String prePorts) {
        this.preDstPorts = prePorts;
    }

    @ExcelField(title="端口转换后", type=2, align = 2, sort= 80)
    public String getPostPorts() {
        return postDstPorts;
    }

    public void setPostPorts(String postPorts) {
        this.postDstPorts = postPorts;
    }

    @ExcelField(title="源域", type=2, align = 2, sort= 90)
    public String getSrcZone() {
        return srcZone;
    }

    public void setSrcZone(String srcZone) {
        this.srcZone = srcZone;
    }

    @ExcelField(title="目的域", type=2, align = 2, sort= 100)
    public String getDstZone() {
        return dstZone;
    }

    public void setDstZone(String dstZone) {
        this.dstZone = dstZone;
    }

    @ExcelField(title="入接口", type=2, align = 2, sort= 110)
    public String getInDevItf() {
        return inDevItf;
    }

    public void setInDevItf(String inDevItf) {
        this.inDevItf = inDevItf;
    }

    @ExcelField(title="出接口", type=2, align = 2, sort= 120)
    public String getOutDevItf() {
        return outDevItf;
    }

    public void setOutDevItf(String outDevItf) {
        this.outDevItf = outDevItf;
    }

    @ExcelField(title="申请人", type=2, align = 2, sort= 130)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @ExcelField(title="策略描述",type = 2,align=2, sort=140)
    public String getDescription() {
        return description;
    }

    @ExcelField(title="工单备注",type = 2,align=2, sort=150)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ServiceDTO> getPreServiceList() {
        return preServiceList;
    }

    public void setPreServiceList(List<ServiceDTO> preServiceList) {
        this.preServiceList = preServiceList;
    }

    public List<ServiceDTO> getPostServiceList() {
        return postServiceList;
    }

    public void setPostServiceList(List<ServiceDTO> postServiceList) {
        this.postServiceList = postServiceList;
    }

    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(id) && AliStringUtils.isEmpty(deviceIp) && AliStringUtils.isEmpty(natType)
                && AliStringUtils.isEmpty(postSrcAddress) && AliStringUtils.isEmpty(postDstAddress) && AliStringUtils.isEmpty(srcZone)
                && AliStringUtils.isEmpty(dstZone) && AliStringUtils.isEmpty(inDevItf) && AliStringUtils.isEmpty(outDevItf)) {
            return true;
        }
        return false;
    }

    public int validation() {
        logger.debug("validate " + theme);

        if(AliStringUtils.isEmpty(id)) {
            return ReturnCode.EMPTY_ID;
        } else if (!StringUtils.isNumeric(id)) {
            return ReturnCode.INVALID_NUMBER;
        }

        if(!natType.equalsIgnoreCase("SNAT") && !natType.equalsIgnoreCase("DNAT")
        && !natType.equalsIgnoreCase("STATIC") && !natType.equalsIgnoreCase("BOTH")) {
            logger.error("NAT协议类型错误！" + natType);
            return ReturnCode.INVALID_NAT_TYPE;
        }

        Integer ipTypeNumber = 0;


        if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV4.getDesc())){
            ipTypeNumber = IpTypeEnum.IPV4.getCode();
        } else if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV6.getDesc())){
            ipTypeNumber = IpTypeEnum.IPV6.getCode();
        } else {
            ipTypeNumber = IpTypeEnum.URL.getCode();
        }


        int rc = 0;
        if (IpTypeEnum.IPV6.getCode().equals(ipTypeNumber)) {
            //源地址校验
            preSrcAddress = formatAddress(preSrcAddress);
            rc = InputValueUtils.checkIpV6(preSrcAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换前源地址不正确：" + preSrcAddress);
                return ReturnCode.PRE_SRC_ADDRESS_FORMAT_ERROR;
            }
            //转换后源地址校验
            postSrcAddress = formatAddress(postSrcAddress);
            rc = InputValueUtils.checkIpV6(postSrcAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换后源地址不正确：" + postSrcAddress);
                return ReturnCode.POST_SRC_ADDRESS_FORMAT_ERROR;
            }
            //目的地址校验
            preDstAddress = formatAddress(preDstAddress);
            rc = InputValueUtils.checkIpV6(preDstAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换前目的地址不正确：" + preDstAddress);
                return ReturnCode.PRE_DST_ADDRESS_FORMAT_ERROR;
            }
            //转换后目的地址校验
            postDstAddress = formatAddress(postDstAddress);
            rc = InputValueUtils.checkIpV6(postDstAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换后目的地址不正确：" + postDstAddress);
                return ReturnCode.POST_DST_ADDRESS_FORMAT_ERROR;
            }
        } else if (IpTypeEnum.IPV4.getCode().equals(ipTypeNumber)) {
            //源地址校验
            preSrcAddress = formatAddress(preSrcAddress);
            rc = InputValueUtils.checkIp(preSrcAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换前源地址不正确：" + preSrcAddress);
                return ReturnCode.PRE_SRC_ADDRESS_FORMAT_ERROR;
            }
            //转换后源地址校验
            postSrcAddress = formatAddress(postSrcAddress);
            rc = InputValueUtils.checkIp(postSrcAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换后源地址不正确：" + postSrcAddress);
                return ReturnCode.POST_SRC_ADDRESS_FORMAT_ERROR;
            }
            //目的地址校验
            preDstAddress = formatAddress(preDstAddress);
            rc = InputValueUtils.checkIp(preDstAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换前目的地址不正确：" + preDstAddress);
                return ReturnCode.PRE_DST_ADDRESS_FORMAT_ERROR;
            }
            //转换后目的地址校验
            postDstAddress = formatAddress(postDstAddress);
            rc = InputValueUtils.checkIp(postDstAddress);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                logger.error("转换后目的地址不正确：" + postDstAddress);
                return ReturnCode.POST_DST_ADDRESS_FORMAT_ERROR;
            }
        } else if (IpTypeEnum.URL.getCode().equals(ipTypeNumber)) {
            logger.info("url的校验放过");
        } else {
            logger.info("ipType类型不对：" + ipType);
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            return rc;
        }

//        if((natType.equalsIgnoreCase("SNAT")) && rc == ReturnCode.POLICY_MSG_EMPTY_VALUE){
//            logger.error("SNAT NAT转换后源地址不能为空！");
//            return ReturnCode.POST_SRC_ADDRESS_IS_EMPTY;
//        }

        if(rc == ReturnCode.POLICY_MSG_EMPTY_VALUE && (natType.equalsIgnoreCase("DNAT"))) {
            logger.error("DNAT NAT转换后目的地址不能为空！");
            return ReturnCode.POST_DST_ADDRESS_IS_EMPTY;
        }

        if(StringUtils.isNotBlank(preDstPorts) && (!PortUtils.isValidPortString(preDstPorts.trim()))) {
            return ReturnCode.INVALID_PORT_FORMAT;
        }
        if(StringUtils.isNotBlank(postDstPorts) && !PortUtils.isValidPortString(postDstPorts.trim())) {
            return ReturnCode.INVALID_PORT_FORMAT;
        }
        //SNAT时需要去掉转换后端口
        if(natType.equalsIgnoreCase("SNAT")) {
            postDstPorts = "";
        }

        //服务校验 any时，serviceList 置空即可
        String protocolString = protocol.trim();
        if(!AliStringUtils.isEmpty(protocolString) && !protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)
        && !protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP) && !protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
            return ReturnCode.PROTOCOL_FORMAT_ERROR;
        }

        if (StringUtils.isNotBlank(protocolString)) {
            List<ServiceDTO> preServiceList = new ArrayList<>();
            List<ServiceDTO> postServiceList = new ArrayList<>();
            ServiceDTO preServiceDTO = new ServiceDTO();
            ServiceDTO postServiceDTO = new ServiceDTO();
            if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                preServiceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ICMP);
                postServiceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ICMP);
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                preServiceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_TCP);
                postServiceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_TCP);
            } else if (protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                preServiceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_UDP);
                postServiceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_UDP);
            }

            if (!protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                preServiceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(preDstPorts.trim()));
                postServiceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(postDstPorts.trim()));
            }

            preServiceList.add(preServiceDTO);
            postServiceList.add(postServiceDTO);
            this.preServiceList = preServiceList;
            this.postServiceList = postServiceList;

            //源NAT转换后服务为空
            if(natType.equalsIgnoreCase("SNAT")) {
                this.postServiceList = null;
            }
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    String formatAddress(String address) {
        String[] srcIpAddresses = address.split("\n");
        StringBuilder srcIpSb = new StringBuilder();
        for(String srcIpAddress: srcIpAddresses) {
            srcIpAddress = srcIpAddress.trim();
            if(StringUtils.isBlank(srcIpAddress)){
                continue;
            }
            srcIpSb.append(",");
            srcIpSb.append(srcIpAddress);
        }
        if(srcIpSb.length() > 0) {
            srcIpSb.deleteCharAt(0);
        }
        return srcIpSb.toString();
    }

    /**
     * 检测协议字符串是否为tcp，udp，icmp或者any之一
     * @param protocol 协议字符串
     * @return 是则返回true，否则返回false
     */
    private boolean isValidProtocol(String protocol) {
        if(protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) ||
                protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)||
                protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) ||
                protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
            return true;
        }
        return false;
    }

    /***导入时，服务正则校验**/
    private boolean serviceReg(String content) {
        content = content.toUpperCase();
        String regex = "^(TCP|UDP):([\\d]*[\\d\\,\\-]*[\\d])$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        return m.matches();
    }
}
