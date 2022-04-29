package com.abtnetworks.totems.generate.dto.excel;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lifei
 * @desc 特殊Nat策略Excel工单模型
 * @date 2021/12/28 15:40
 */
@ApiModel("特殊Nat策略Excel工单模型")
public class ExcelTaskSpecialNatEntity {

    private static Logger logger = LoggerFactory.getLogger(ExcelTaskSpecialNatEntity.class);


    @ApiModelProperty("ID")
    String id;

    @ApiModelProperty("名称")
    String name;

    @ApiModelProperty("设备IP")
    String deviceIp;

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


    @ExcelField(title="序号", type=2, align = 2, sort= 1)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ExcelField(title="名称", type=2, align = 2, sort= 10)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title="Nat防火墙IP", type = 2, align = 2, sort = 15)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
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

    @ExcelField(title="转换后源地址", type=2, align = 2, sort= 40)
    public String getPostSrcAddress() {
        return postSrcAddress;
    }

    public void setPostSrcAddress(String postSrcAddress) {
        this.postSrcAddress = postSrcAddress;
    }

    @ExcelField(title="转换前目的地址", type=2, align = 2, sort= 50)
    public String getPreDstAddress() {
        return preDstAddress;
    }

    public void setPreDstAddress(String preDstAddress) {
        this.preDstAddress = preDstAddress;
    }

    @ExcelField(title="转换后目的地址", type=2, align = 2, sort= 60)
    public String getPostDstAddress() {
        return postDstAddress;
    }

    public void setPostDstAddress(String postDstAddress) {
        this.postDstAddress = postDstAddress;
    }

    @ExcelField(title="协议", type=2, align = 2, sort= 65)
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @ExcelField(title="端口转换前", type=2, align = 2, sort= 70)
    public String getPreDstPorts() {
        return preDstPorts;
    }

    public void setPreDstPorts(String preDstPorts) {
        this.preDstPorts = preDstPorts;
    }

    @ExcelField(title="端口转换后", type=2, align = 2, sort= 75)
    public String getPostDstPorts() {
        return postDstPorts;
    }

    public void setPostDstPorts(String postDstPorts) {
        this.postDstPorts = postDstPorts;
    }

    @ExcelField(title="源域", type=2, align = 2, sort= 80)
    public String getSrcZone() {
        return srcZone;
    }

    public void setSrcZone(String srcZone) {
        this.srcZone = srcZone;
    }

    @ExcelField(title="目的域", type=2, align = 2, sort= 85)
    public String getDstZone() {
        return dstZone;
    }

    public void setDstZone(String dstZone) {
        this.dstZone = dstZone;
    }

    @ExcelField(title="入接口", type=2, align = 2, sort= 90)
    public String getInDevItf() {
        return inDevItf;
    }

    public void setInDevItf(String inDevItf) {
        this.inDevItf = inDevItf;
    }

    @ExcelField(title="出接口", type=2, align = 2, sort= 95)
    public String getOutDevItf() {
        return outDevItf;
    }

    public void setOutDevItf(String outDevItf) {
        this.outDevItf = outDevItf;
    }

    @ExcelField(title="申请人", type=2, align = 2, sort= 100)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(id) && AliStringUtils.isEmpty(deviceIp)
                && AliStringUtils.isEmpty(postSrcAddress) && AliStringUtils.isEmpty(postDstAddress) && AliStringUtils.isEmpty(srcZone)
                && AliStringUtils.isEmpty(dstZone) && AliStringUtils.isEmpty(inDevItf) && AliStringUtils.isEmpty(outDevItf)) {
            return true;
        }
        return false;
    }



    public int validation() {
        logger.debug("validate " + name);

        if(AliStringUtils.isEmpty(id)) {
            return ReturnCode.EMPTY_ID;
        } else if (!StringUtils.isNumeric(id)) {
            return ReturnCode.INVALID_NUMBER;
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


        if(StringUtils.isNotBlank(preDstPorts) && (!PortUtils.isValidPortString(preDstPorts.trim()))) {
            return ReturnCode.INVALID_PORT_FORMAT;
        }
        if(StringUtils.isNotBlank(postDstPorts) && !PortUtils.isValidPortString(postDstPorts.trim())) {
            return ReturnCode.INVALID_PORT_FORMAT;
        }

        //服务校验 any时，serviceList 置空即可
        String protocolString = protocol.trim();
        if(!AliStringUtils.isEmpty(protocolString) && !protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)
                && !protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP) && !protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
            return ReturnCode.PROTOCOL_FORMAT_ERROR;
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
}
