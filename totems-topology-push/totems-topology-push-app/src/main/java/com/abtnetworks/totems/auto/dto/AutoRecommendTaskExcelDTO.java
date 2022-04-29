package com.abtnetworks.totems.auto.dto;

import com.abtnetworks.totems.auto.enums.InputTypeEnum;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.tools.excel.ExcelField;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.ImportExcelVerUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class AutoRecommendTaskExcelDTO {

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("uuid")
    private String uuid;

    @ApiModelProperty("主题")
    private String theme;

    @ApiModelProperty("流水号")
    private String orderNumber;

    @ApiModelProperty("申请人")
    private String userName;

    @ApiModelProperty("申请描述")
    private String description;

    @ApiModelProperty("源IP组")
    private String srcIp;

    @ApiModelProperty("源地址所属系统")
    private String srcIpSystem;

    @ApiModelProperty("转换后源地址所属系统")
    private String postSrcIpSystem;

    @ApiModelProperty("目的IP组")
    private String dstIp;

    @ApiModelProperty("目的地址所属系统")
    private String dstIpSystem;

    @ApiModelProperty("转换后目的地址所属系统")
    private String postDstIpSystem;

    @ApiModelProperty("服务组")
    private String service;

    @ApiModelProperty("服务组")
    private List<ServiceDTO> serviceList = new ArrayList<>();

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("开始时间-时间戳格式")
    private Long startTime;

    @ApiModelProperty("结束时间-时间戳格式")
    private Long endTime;

    @ApiModelProperty("任务状态")
    private Integer status;

    @ApiModelProperty("任务类型")
    private String taskType;

    @ApiModelProperty("附加信息")
    private String additionInfo;

    @ApiModelProperty("工单开始时间")
    private String timeRange;

    @ApiModelProperty("批量开通任务id")
    private Integer batchId;

    @ApiModelProperty("模拟变更场景ID")
    private String whatIfCase;

    @ApiModelProperty("长链接超时时间")
    private Integer idleTimeout;

    @ApiModelProperty("关联nat信息")
    private String relevancyNat;

    @ApiModelProperty("组")
    private String branchLevel;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url")
    private Integer ipType;

    @ApiModelProperty("转换后源地址")
    private String postSrcIp;

    @ApiModelProperty("转换后目的地址")
    private String postDstIp;

    @ApiModelProperty("合并检查")
    private Boolean mergeCheck;

    @ApiModelProperty("策略分成允许流和禁止流两部分")
    private Boolean rangeFilter;

    @ApiModelProperty("移动到冲突前")
    private Boolean beforeConflict;

    @ApiModelProperty("申请人")
    private String applicant;

    @ApiModelProperty("申请人邮箱")
    private String applicantEmail;

    @ApiModelProperty("源地址输入类型")
    private String srcInputType;

    @ApiModelProperty("目的地址输入类型")
    private String dstInputType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @ExcelField(title = "主题/工单号", type = 2, align = 2, sort = 5)
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @ExcelField(title = "申请描述", type = 2, align = 2, sort = 10)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ExcelField(title = "源地址", type = 2, align = 2, sort = 25)
    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getSrcIpSystem() {
        return srcIpSystem;
    }

    public void setSrcIpSystem(String srcIpSystem) {
        this.srcIpSystem = srcIpSystem;
    }

    public String getPostSrcIpSystem() {
        return postSrcIpSystem;
    }

    public void setPostSrcIpSystem(String postSrcIpSystem) {
        this.postSrcIpSystem = postSrcIpSystem;
    }

    @ExcelField(title = "目的地址", type = 2, align = 2, sort = 30)
    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public String getDstIpSystem() {
        return dstIpSystem;
    }

    public void setDstIpSystem(String dstIpSystem) {
        this.dstIpSystem = dstIpSystem;
    }

    public String getPostDstIpSystem() {
        return postDstIpSystem;
    }

    public void setPostDstIpSystem(String postDstIpSystem) {
        this.postDstIpSystem = postDstIpSystem;
    }

    @ExcelField(title = "服务", type = 2, align = 2, sort = 35)
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<ServiceDTO> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<ServiceDTO> serviceList) {
        this.serviceList = serviceList;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @ExcelField(title = "访问类型", type = 2, align = 2, sort = 20)
    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getAdditionInfo() {
        return additionInfo;
    }

    public void setAdditionInfo(String additionInfo) {
        this.additionInfo = additionInfo;
    }

    @ExcelField(title = "生效时间", type = 2, align = 2, sort = 40)
    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    @ExcelField(title = "源地址输入类型", type = 2, align = 2, sort = 45)
    public String getSrcInputType() {
        return srcInputType;
    }

    public void setSrcInputType(String srcInputType) {
        this.srcInputType = srcInputType;
    }

    @ExcelField(title = "目的地址输入类型", type = 2, align = 2, sort = 50)
    public String getDstInputType() {
        return dstInputType;
    }

    public void setDstInputType(String dstInputType) {
        this.dstInputType = dstInputType;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public String getWhatIfCase() {
        return whatIfCase;
    }

    public void setWhatIfCase(String whatIfCase) {
        this.whatIfCase = whatIfCase;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getRelevancyNat() {
        return relevancyNat;
    }

    public void setRelevancyNat(String relevancyNat) {
        this.relevancyNat = relevancyNat;
    }

    public String getBranchLevel() {
        return branchLevel;
    }

    public void setBranchLevel(String branchLevel) {
        this.branchLevel = branchLevel;
    }

    public Integer getIpType() {
        return ipType;
    }

    public void setIpType(Integer ipType) {
        this.ipType = ipType;
    }

    public String getPostSrcIp() {
        return postSrcIp;
    }

    public void setPostSrcIp(String postSrcIp) {
        this.postSrcIp = postSrcIp;
    }

    public String getPostDstIp() {
        return postDstIp;
    }

    public void setPostDstIp(String postDstIp) {
        this.postDstIp = postDstIp;
    }

    public Boolean getMergeCheck() {
        return mergeCheck;
    }

    public void setMergeCheck(Boolean mergeCheck) {
        this.mergeCheck = mergeCheck;
    }

    public Boolean getRangeFilter() {
        return rangeFilter;
    }

    public void setRangeFilter(Boolean rangeFilter) {
        this.rangeFilter = rangeFilter;
    }

    public Boolean getBeforeConflict() {
        return beforeConflict;
    }

    public void setBeforeConflict(Boolean beforeConflict) {
        this.beforeConflict = beforeConflict;
    }

    @ExcelField(title = "申请人", type = 2, align = 2, sort = 15)
    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public boolean isEmpty() {
        if(AliStringUtils.isEmpty(theme) && AliStringUtils.isEmpty(srcIp)) {
            return true;
        }
        return false;
    }

    /**
     * 进行数据合法性检查，并给出提示。
     * 进行srcIp,dstIp,service,timeRange格式的校验并进行处理
     *
     * @return 检查结果
     */
    public int validation() {

        int rc = 0;
        // 数据校验
        if (StringUtils.isNotEmpty(srcIp)) {
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
            if (InputTypeEnum.SRC_INPUT_TYPE_IP.getCode().equals(InputTypeEnum.getCodeByDesc("源地址输入"+srcInputType))) {
                rc = InputValueUtils.checkIp(srcIp);
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                    log.info("源地址不正确：" + srcIp);
                    return ReturnCode.SRC_IP_FORMAT_ERROR;
                }
            }
        }

        if (StringUtils.isNotEmpty(dstIp)) {
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

            if (InputTypeEnum.DST_INPUT_TYPE_IP.getCode().equals(InputTypeEnum.getCodeByDesc("目的地址输入"+dstInputType))) {
                rc = InputValueUtils.checkIp(dstIp);
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                    log.info("目的地址不正确：" + dstIp);
                    return ReturnCode.SRC_IP_FORMAT_ERROR;
                }
            }
        }

        //服务校验 any时，serviceList [{"dstPorts":"any","protocol":"0","srcPorts":"any"}]
        if (StringUtils.isNotBlank(service) && !service.toUpperCase().trim().contains(PolicyConstants.POLICY_STR_VALUE_ANY.toUpperCase())) {
            service = service.trim();

            Map<String, String> serviceMap = new HashMap<>();
            //具体的协议、端口
            String[] serviceArr = service.split("\n");
            List<ServiceDTO> serviceDTOList = new ArrayList<>();
            for (String ser : serviceArr) {
                ser = ser.trim();
                if (StringUtils.isBlank(ser)) {
                    continue;
                }
                ServiceDTO serviceDTO = new ServiceDTO();
                String[] protocolPortArr = ser.split(":");
                String serviceName = protocolPortArr[0].trim();
                if (!ImportExcelVerUtils.isValidProtocol(serviceName)) {
                    log.info("服务错误：" + serviceName);
                    return ReturnCode.PROTOCOL_FORMAT_ERROR;
                }
                if(serviceMap.get(serviceName) != null){
                    log.error("服务错误，存在重复的协议,protocol:{},service:{} ", serviceName, service);
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
                            log.error("服务格式错误:" + ser);
                            return ReturnCode.SERVICE_FORMAT_ERROR;
                        }
                    } else if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                        log.error("服务格式错误:" + ser + ",icmp不需要端口信息");
                        return ReturnCode.SERVICE_FORMAT_ERROR;
                    }
                    if(!PortUtils.isValidPortString(ports)) {
                        return ReturnCode.INVALID_PORT_FORMAT;
                    }
                    serviceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(ports));
                }
                serviceDTOList.add(serviceDTO);
            }
            this.serviceList = serviceDTOList;
        }

        //时间范围校验
        if (StringUtils.isNotBlank(timeRange) && timeRange.indexOf("-") == -1) {
            log.info("生效时间格式错误timeRange:{}", timeRange);
            return ReturnCode.EFFECTIVE_TIME_ERROR;
        }
        return rc;
    }

}
