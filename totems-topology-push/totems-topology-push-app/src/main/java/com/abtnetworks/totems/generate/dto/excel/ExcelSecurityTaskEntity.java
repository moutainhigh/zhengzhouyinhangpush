package com.abtnetworks.totems.generate.dto.excel;

import static com.abtnetworks.totems.common.constants.CommonConstants.HOUR_SECOND;
import static com.abtnetworks.totems.common.enums.ProtocolEnum.ICMPV6;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.alibaba.fastjson.JSON;

import io.swagger.annotations.ApiModelProperty;

public class ExcelSecurityTaskEntity {

    private static Logger logger = LoggerFactory.getLogger(ExcelSecurityTaskEntity.class);

    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm";

    private static final String ANY_VALUE_STRING = "0";

    private static final String ICMP_VALUE_STRING = "1";

    private static final String TCP_VALUE_STRING = "6";

    private static final String UDP_VALUE_STRING = "17";
    String id;

    String orderNO;

    String name;

    String ipType;

   String srcIp;

   String srcIpDescription;

    String dstIp;

    String dstIpDescription;

    String service;

    /**读取excel的时间范围***/
    String timeRange;

    String user;

    String description;

    /**读取的service解析后的，转换的**/
    List<ServiceDTO> serviceList;

    /**开始时间**/
    private String startTime;

    /**结束时间**/
    private String endTime;

    /**动作**/
    private String action;


    /** 开通备注 **/
    private String remark;

    private String deviceIp;

    private String srcZone;

    private String dstZone;

    private String inDevItf;

    private String outDevItf;

    @ApiModelProperty("范围过滤")
    private String rangeFilter;
    @ApiModelProperty("合并检查")
    private String mergeCheck;
    @ApiModelProperty("移动到冲突前")
    private String beforeConflict;

    /**长连接**/
    private String idleTimeout;


    @ExcelField(title = "序号", type = 2, align = 2, sort = 0)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNO() {
        return orderNO;
    }

    public void setOrderNO(String orderNO) {
        this.orderNO = orderNO;
    }

    @ExcelField(title="主题/工单号",type = 2,align=2, sort=10)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title="防火墙IP",type = 2,align=2, sort=15)
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @ExcelField(title="IP类型",type = 2,align=2, sort=16)
    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    @ExcelField(title="源IP地址",type = 2,align=2, sort=20)
    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    @ExcelField(title="源地址描述",type = 2,align=2, sort=25)
    public String getSrcIpDescription() {
        return srcIpDescription;
    }

    public void setSrcIpDescription(String srcIpDescription) {
        this.srcIpDescription = srcIpDescription;
    }

    @ExcelField(title="目的主机地址",type = 2,align=2, sort=30)
    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    @ExcelField(title="目的地址描述",type = 2,align=2, sort=35)
    public String getDstIpDescription() {
        return dstIpDescription;
    }

    public void setDstIpDescription(String dstIpDescription) {
        this.dstIpDescription = dstIpDescription;
    }

    @ExcelField(title = "服务", type = 2, align = 2, sort = 40)
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }


    @ExcelField(title="源域",type = 2,align=2, sort=42)
    public String getSrcZone() {
        return srcZone;
    }

    public void setSrcZone(String srcZone) {
        this.srcZone = srcZone;
    }

    @ExcelField(title="目的域",type = 2,align=2, sort=44)
    public String getDstZone() {
        return dstZone;
    }

    public void setDstZone(String dstZone) {
        this.dstZone = dstZone;
    }

    @ExcelField(title="入接口",type = 2,align=2, sort=46)
    public String getInDevItf() {
        return inDevItf;
    }

    public void setInDevItf(String inDevItf) {
        this.inDevItf = inDevItf;
    }

    @ExcelField(title="出接口",type = 2,align=2, sort=48)
    public String getOutDevItf() {
        return outDevItf;
    }

    public void setOutDevItf(String outDevItf) {
        this.outDevItf = outDevItf;
    }
    @ExcelField(title="生效时间",type = 2,align=2, sort=50)
    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    @ExcelField(title="动作",type = 2,align=2, sort=55)
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @ExcelField(title="长连接(小时)",type = 2,align=2, sort=58)
    public String getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @ExcelField(title="申请人",type = 2,align=2, sort=60)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @ExcelField(title="备注",type = 2,align=2, sort=70)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ExcelField(title="开通备注",type = 2,align=2, sort=80)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }




    @ExcelField(title="范围过滤",type = 2,align=2, sort=82)
    public String getRangeFilter() {
        return rangeFilter;
    }

    public void setRangeFilter(String rangeFilter) {
        this.rangeFilter = rangeFilter;
    }
    @ExcelField(title="合并检查",type = 2,align=2, sort=84)
    public String getMergeCheck() {
        return mergeCheck;
    }

    public void setMergeCheck(String mergeCheck) {
        this.mergeCheck = mergeCheck;
    }
    @ExcelField(title="移动到冲突前",type = 2,align=2, sort=86)
    public String getBeforeConflict() {
        return beforeConflict;
    }

    public void setBeforeConflict(String beforeConflict) {
        this.beforeConflict = beforeConflict;
    }

    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(id) && AliStringUtils.isEmpty(srcIp)
                && AliStringUtils.isEmpty(dstIp) && AliStringUtils.isEmpty(service)) {
            return true;
        }
        return false;
    }

    /**
     * 进行数据合法性检查，并给出提示。
     * 暂不增加对导入IP的子网检测，路径分析时检测，与在界面增加不同，则路径分析中需要增加相应检测和相应状态
     *
     * @return 检查结果
     */
    public int validation() {
//        logger.info("validate " + name);

        if(AliStringUtils.isEmpty(id)) {
            return ReturnCode.EMPTY_ID;
        } else if (!StringUtils.isNumeric(id)) {
            return ReturnCode.INVALID_NUMBER;
        }
        int rc = 0;
        if(StringUtils.isNotEmpty(srcIp)){
        //源ip校验
        String[] srcIpAddresses = srcIp.split("\n");
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
        srcIp = srcIpSb.toString();

        srcIpDescription = StringUtils.isNotBlank(srcIpDescription) ? srcIpDescription.replace("\n", "") : null;
        dstIpDescription = StringUtils.isNotBlank(dstIpDescription) ? dstIpDescription.replace("\n", "") : null;

        if (StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV4.getDesc())) {
            rc = InputValueUtils.checkIp(srcIp);
        } else if (StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV6.getDesc())) {
            rc = InputValueUtils.checkIpV6(srcIp);
        } else if (StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.URL.getDesc())) {
            logger.info("url的校验放过");
        } else {
            logger.info("ipType类型不对：" + ipType);
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            return rc;
        }

        if( rc != ReturnCode.POLICY_MSG_OK) {
            logger.info("源地址不正确：" + srcIp);
            if(rc == ReturnCode.INVALID_IP_RANGE) {
                return ReturnCode.INVALID_SRC_IP_RANGE;
            }
            return ReturnCode.SRC_IP_FORMAT_ERROR;
        }
        }
        if(StringUtils.isNotEmpty(dstIp)){
        //目的IP校验
        String[] dstIpAddresses = dstIp.split("\n");
        StringBuilder dstIpSb = new StringBuilder();
        for(String dstIpAddress: dstIpAddresses) {
            dstIpAddress = dstIpAddress.trim();
            if(StringUtils.isBlank(dstIpAddress)){
                continue;
            }
            dstIpSb.append(",");
            dstIpSb.append(dstIpAddress);
        }
        if(dstIpSb.length() > 0) {
            dstIpSb.deleteCharAt(0);
        }

        dstIp = dstIpSb.toString();
        if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV4.getDesc())){
            rc = InputValueUtils.checkIp(dstIp);
        } else if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV6.getDesc())){
            rc = InputValueUtils.checkIpV6(dstIp);
        }else if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.URL.getDesc())){
            logger.info("url的校验放过");
        }else{
            logger.info("ipType类型不对：" + ipType);
            rc = ReturnCode.POLICY_MSG_INVALID_FORMAT;
            return rc;
        }
        if(rc != ReturnCode.POLICY_MSG_OK) {
            logger.info("目的地址不正确：" + dstIp);
            if(rc == ReturnCode.INVALID_IP_RANGE ) {
                return ReturnCode.INVALID_DST_IP_RANGE;
            }
            return ReturnCode.DST_IP_FORMAT_ERROR;
        }
        }
//        if(AliStringUtils.isEmpty(service) || service.toUpperCase().trim().contains(PolicyConstants.POLICY_STR_VALUE_ANY.toUpperCase())) {
//            logger.info("服务能为空或者包含any");
//            return ReturnCode.POLICY_MSG_OK;
//        }

        //服务校验 any时，serviceList [{"dstPorts":"any","protocol":"0","srcPorts":"any"}]
        if (StringUtils.isNotBlank(service) && !service.toUpperCase().trim().contains(PolicyConstants.POLICY_STR_VALUE_ANY.toUpperCase())) {
            service = service.trim();

            Map<String, String> serviceMap = new HashMap<>();
            //具体的协议、端口
            String[] serviceArr = service.split("\n");
            List<ServiceDTO> serviceList = new ArrayList<>();
            for (String ser : serviceArr) {
                ser = ser.trim();
                if (StringUtils.isBlank(ser)) {
                    continue;
                }
                ServiceDTO serviceDTO = new ServiceDTO();
                String[] protocolPortArr = ser.split(":");
                String serviceName = protocolPortArr[0].trim();
                if (!ImportExcelVerUtils.isValidProtocol(serviceName)) {
                    logger.info("服务错误：" + serviceName);
                    return ReturnCode.PROTOCOL_FORMAT_ERROR;
                }
                if(serviceMap.get(serviceName) != null){
                    logger.error("服务错误，存在重复的协议,protocol:{},service:{} ", serviceName, service);
                    return ReturnCode.PROTOCOL_REPEAT_ERROR;
                }else{
                    serviceMap.put(serviceName, "");
                }
                if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ICMP);
                } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_TCP);
                } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                    serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_UDP);
                }
                if (protocolPortArr.length > 1) {
                    String ports = protocolPortArr[1].trim();
                    //校验
                    if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP) || serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                        boolean serviceFlag = ImportExcelVerUtils.serviceReg(ser);
                        if (!serviceFlag) {
                            logger.error("服务格式错误:" + ser);
                            return ReturnCode.SERVICE_FORMAT_ERROR;
                        }
                    } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                        logger.error("服务格式错误:" + ser + ",icmp不需要端口信息");
                        return ReturnCode.SERVICE_FORMAT_ERROR;
                    }
                    if(!PortUtils.isValidPortString(ports)) {
                        return ReturnCode.INVALID_PORT_FORMAT;
                    }
                    serviceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(ports));
                }
                serviceList.add(serviceDTO);
            }
            this.serviceList = serviceList;
        }else{

        }


        //时间范围校验
        if (StringUtils.isNotBlank(timeRange)) {
            if (timeRange.indexOf("-") == -1) {
                logger.info("生效时间格式错误timeRange:{}", timeRange);
                return ReturnCode.EFFECTIVE_TIME_ERROR;
            }
            String[] timeArr = timeRange.split("-");
            this.startTime = timeArr[0].trim();
            this.endTime = timeArr[1].trim();
        }
        return rc;
    }

    public RecommendTaskEntity toTaskEntity() {
        RecommendTaskEntity taskEntity = new RecommendTaskEntity();

        taskEntity.setCreateTime(new Date(DateUtil.uniqueCurrentTimeMS()));
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        taskEntity.setTheme(name);
        taskEntity.setOrderNumber(orderNO);
        taskEntity.setUserName(user);
        taskEntity.setDescription(description);
        taskEntity.setRemarks(remark);
        taskEntity.setSrcIp(srcIp);
        taskEntity.setSrcIpSystem(StringUtils.isBlank(srcIpDescription) ? null : srcIpDescription.replace("\n", ""));
        taskEntity.setDstIpSystem(StringUtils.isBlank(dstIpDescription) ? null : dstIpDescription.replace("\n", ""));
        taskEntity.setDstIp(dstIp);
        if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV4.getDesc())){
            taskEntity.setIpType(IpTypeEnum.IPV4.getCode());
        } else if(StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV6.getDesc())){
            taskEntity.setIpType(IpTypeEnum.IPV6.getCode());
        } else {
            taskEntity.setIpType(IpTypeEnum.URL.getCode());
        }
        if (serviceList != null && !serviceList.isEmpty()) {
            List<ServiceDTO> newServiceList = new ArrayList<>();
            for(ServiceDTO serviceDTO : serviceList){
                ServiceDTO newServiceDTO = new ServiceDTO();
                BeanUtils.copyProperties(serviceDTO, newServiceDTO);
                if(StringUtils.equalsAnyIgnoreCase(serviceDTO.getProtocol(),PolicyConstants.POLICY_NUM_VALUE_ICMP) && StringUtils.equalsAnyIgnoreCase(ipType, IpTypeEnum.IPV6.getDesc())){
                    newServiceDTO.setProtocol(ICMPV6.getCode());
                }
                newServiceList.add(newServiceDTO);
            }
            taskEntity.setServiceList(JSON.toJSONString(newServiceList));
        }else{
            logger.info("新建策略服务为空");
            serviceList = new LinkedList<>();
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ANY);
            serviceDTO.setSrcPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceDTO.setDstPorts(PolicyConstants.POLICY_STR_VALUE_ANY);
            serviceList.add(serviceDTO);
            taskEntity.setServiceList(JSON.toJSONString(serviceList));
        }

        try {
            if(!AliStringUtils.isEmpty(startTime) && !AliStringUtils.isEmpty(endTime)) {
                taskEntity.setStartTime(sdf.parse(startTime));
                taskEntity.setEndTime(sdf.parse(endTime));
            }

        }catch(Exception e) {
            logger.error("解析日期出错！", e);
        }
        taskEntity.setStatus(0);
        taskEntity.setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND);
        taskEntity.setMergeCheck(StringUtils.isEmpty(mergeCheck)?false:Boolean.parseBoolean(mergeCheck));
        taskEntity.setBeforeConflict(StringUtils.isEmpty(beforeConflict)?false:Boolean.parseBoolean(beforeConflict));
        taskEntity.setRangeFilter(StringUtils.isEmpty(rangeFilter)?false:Boolean.parseBoolean(rangeFilter));

        if (ObjectUtils.isNotEmpty(idleTimeout)) {
            taskEntity.setIdleTimeout(Integer.parseInt(idleTimeout) * HOUR_SECOND);
        } else {
            taskEntity.setIdleTimeout(null);
        }
        return taskEntity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id = ").append(id);
        sb.append(", name = ").append(name);
        sb.append(", user = ").append(user);
        sb.append(", description = ").append(description);
        sb.append(", srcIp = ").append(srcIp);
        sb.append(", dstIp = ").append(dstIp);
        sb.append(", timeRange = ").append(timeRange);
        sb.append(", service = ").append(service);

        sb.append("]");

        return sb.toString();
    }






    public boolean isBigRange(String ipAddress) {
        if(IpUtils.isIPRange(ipAddress)) {
            String startIp = IpUtils.getStartIpFromIpAddress(ipAddress);
            String endIp = IpUtils.getEndIpFromIpAddress(ipAddress);
            Long startIpLong = IpUtils.IPv4StringToNum(startIp);
            Long endIpLong = IpUtils.IPv4StringToNum(endIp);
            if((endIpLong - startIpLong) >= 65535) {
                return true;
            }
        } else if (IpUtils.isIPSegment(ipAddress)) {
            String maskbitString = IpUtils.getMaskBitFromIpSegment(ipAddress);
            int maskbit = Integer.valueOf(maskbitString);
            if(maskbit <= 16) {
                return true;
            }
        }

        return false;
    }


}
