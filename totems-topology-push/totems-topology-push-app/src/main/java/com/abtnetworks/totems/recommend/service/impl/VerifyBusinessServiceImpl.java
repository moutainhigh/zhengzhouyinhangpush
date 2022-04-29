package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.RecommendTypeEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.ImportExcelVerUtils;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.recommend.dto.excel.ExcelBigInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.verify.VerifyBussExcelDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendTaskDTO;
import com.abtnetworks.totems.recommend.service.VerifyBusinessService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.abtnetworks.totems.common.constants.CommonConstants.IDLE_TIMEOUT_PATTERN;
import static com.abtnetworks.totems.common.constants.CommonConstants.IPV4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV6;
import static com.abtnetworks.totems.common.enums.ProtocolEnum.ICMPV6;
import static com.abtnetworks.totems.recommend.service.RecommendExcelAndDownloadService.DATE_FORMAT;

/**
 * @author Administrator
 * @Title:
 * @Description: 验证业务数据
 * @date 2021/1/25
 */
@Slf4j
@Service
public class VerifyBusinessServiceImpl implements VerifyBusinessService {

    @Override
    public VerifyBussExcelDTO verifyRecommendBussExcel(ExcelRecommendTaskDTO entity) {
        log.debug("开始校验业务开通的参数{}", JSONObject.toJSONString(entity));
        VerifyBussExcelDTO verifyBussExcelDTO = new VerifyBussExcelDTO();
        commonParamVerify(verifyBussExcelDTO, entity, null);
        return verifyBussExcelDTO;

    }

    /**
     * 校验id是否填写
     *
     * @param id
     * @return
     */
    private int verifyId(String id) {
        if (AliStringUtils.isEmpty(id)) {
            return ReturnCode.EMPTY_ID;
        } else if (!StringUtils.isNumeric(id)) {
            return ReturnCode.INVALID_NUMBER;
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 长链接校验
     *
     * @param idleTimeOut
     * @return
     */
    private int verifyIdleTimeout(String idleTimeOut) {

        //长连接校验
        if (StringUtils.isNotBlank(idleTimeOut)) {
            Matcher isNum = IDLE_TIMEOUT_PATTERN.matcher(idleTimeOut);
            if (!isNum.matches()) {
                return ReturnCode.IDLE_TIMEOUT_FORMAT_ERROR;
            }
            int timeout = Integer.valueOf(idleTimeOut);
            if (timeout <= 0 || timeout > 24000) {
                return ReturnCode.IDLE_TIMEOUT_FORMAT_ERROR;
            }
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 校验时间范围
     *
     * @param timeRange
     * @return
     */
    private int verifyTimeRange(String timeRange) {
        //时间范围校验
        if (StringUtils.isNotBlank(timeRange)) {
            String[] timeRanges = timeRange.split("-");
            if (timeRanges.length < 2) {
                log.info("生效时间格式错误timeRange:{}", timeRange);
                return ReturnCode.EFFECTIVE_TIME_ERROR;
            }
            if (timeRange.indexOf("-") == -1) {
                log.info("生效时间格式错误timeRange:{}", timeRange);
                return ReturnCode.EFFECTIVE_TIME_ERROR;
            }
            if (!isValidDateFormat(timeRanges[0], timeRanges[1])) {
                log.info("开始结束时间不正确：[" + timeRanges[0] + "] to [" + timeRanges[1] + "]");
                return ReturnCode.TIME_FORMAT_ERROR;
            }
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 检测时间数据是否合法。
     *
     * @param startTime
     * @param endTime
     * @return
     */
    private boolean isValidDateFormat(String startTime, String endTime) {
        boolean valid = true;
        if (AliStringUtils.isEmpty(startTime) && AliStringUtils.isEmpty(endTime)) {

        } else if (startTime != null && endTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date start = sdf.parse(startTime);
                Date end = sdf.parse(endTime);
                if (!start.before(end)) {
                    valid = false;
                }
            } catch (ParseException e) {
                valid = false;
            }
        } else {
            valid = false;
        }
        return valid;
    }

    /**
     * 将excel中字符的类型的service变成数组list对象
     *
     * @param service
     * @param ipType //KSH-5038
     * @param serviceList
     * @return
     */
    @Override
    public int verifyService(String service, List<ServiceDTO> serviceList,Integer ipType) {
        //服务校验 any时，serviceList 置空即可
        if (StringUtils.isBlank(service)) {
            return ReturnCode.SERVICE_CANNOT_BE_ANY;
        } else {
            //具体的协议、端口
            String[] serviceArr = service.split("\n");
            Map<String, String> serviceMap = new HashMap<>();
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
                if (serviceMap.get(serviceName) != null) {
                    log.error("服务错误，存在重复的协议,protocol:{},service:{} ", serviceName, service);
                    return ReturnCode.PROTOCOL_REPEAT_ERROR;
                } else {
                    serviceMap.put(serviceName, "");
                }
                if (serviceName.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                    if(ipType!= null && IPV6.getCode().equals(ipType)){
                        //KSH-5038
                        serviceDTO.setProtocol(ICMPV6.getCode());
                    }else{
                        serviceDTO.setProtocol(PolicyConstants.POLICY_NUM_VALUE_ICMP);
                    }

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
                    if (!PortUtils.isValidPortString(ports)) {
                        return ReturnCode.INVALID_PORT_FORMAT;
                    }
                    serviceDTO.setDstPorts(InputValueUtils.autoCorrectPorts(ports));
                }
                serviceList.add(serviceDTO);
            }
        }
        return ReturnCode.POLICY_MSG_OK;
    }


    /**
     * 校验源和目的的参数组装参数的公共方法
     *
     * @param ipType
     * @param srcOrDstIp
     * @return
     */
    private int verifySrcOrDstIp(String srcOrDstIp, String ipType) {

        int rc;
        if (IPV4.equalsIgnoreCase(ipType)) {
            rc = InputValueUtils.checkIp(srcOrDstIp);
        } else {
            //这里ipv6 实现校验都点不一样
            rc = InputValueUtils.checkIpV6(srcOrDstIp);
            if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.INVALID_IP_RANGE && rc != ReturnCode.POLICY_MSG_EMPTY_VALUE) {
                return rc;
            } else {
                return ReturnCode.POLICY_MSG_OK;
            }
        }
        return rc;
    }

    /**
     * 校验为空的地址和参数组装的公共方法
     *
     * @param dstOrSrcIp
     * @return
     */
    private String verifyNotEmptySrcOrDstIp(String dstOrSrcIp) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(dstOrSrcIp)) {
            //目的IP校验
            String[] dstOrSrcIps = dstOrSrcIp.split("\n");

            for (String dstIpAddress : dstOrSrcIps) {
                dstIpAddress = dstIpAddress.trim();
                if (StringUtils.isBlank(dstIpAddress)) {
                    continue;
                }
                stringBuilder.append(",");
                stringBuilder.append(dstIpAddress);
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(0);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 业务开通和大网段开通校验
     *
     * @param verifyBussExcelDTO
     * @param excelRecommendTaskEntity
     * @param excelBigInternetTaskDTO
     */
    private void commonParamVerify(VerifyBussExcelDTO verifyBussExcelDTO, ExcelRecommendTaskDTO excelRecommendTaskEntity, ExcelBigInternetTaskDTO excelBigInternetTaskDTO) {
        String id, ipType, srcIp, dstIp, service, timeRange, idleTimeout,postSrcIp,postDstIp;
        if (excelRecommendTaskEntity != null) {
            id = excelRecommendTaskEntity.getId();
            ipType = excelRecommendTaskEntity.getIpType();
            srcIp = excelRecommendTaskEntity.getSrcIp();
            dstIp = excelRecommendTaskEntity.getDstIp();
            service = excelRecommendTaskEntity.getService();
            timeRange = excelRecommendTaskEntity.getTimeRange();
            idleTimeout = excelRecommendTaskEntity.getIdleTimeout();
            postSrcIp = excelRecommendTaskEntity.getPostSrcIp();
            postDstIp = excelRecommendTaskEntity.getPostDstIp();
        } else {
            id = excelBigInternetTaskDTO.getId();
            ipType = excelBigInternetTaskDTO.getIpType();
            srcIp = excelBigInternetTaskDTO.getSrcIp();
            dstIp = excelBigInternetTaskDTO.getDstIp();
            service = excelBigInternetTaskDTO.getService();
            timeRange = excelBigInternetTaskDTO.getTimeRange();
            idleTimeout = excelBigInternetTaskDTO.getIdleTimeout();
            postSrcIp = "";
            postDstIp = "";
        }
        //校验
        int rc = verifyId(id);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            verifyBussExcelDTO.setResultCode(ReturnCode.INVALID_NUMBER);
            return;
        }
        //源ip校验
        String srcOrDstIp = verifyNotEmptySrcOrDstIp(srcIp);
        if (StringUtils.isNotBlank(srcOrDstIp)) {
            rc = verifySrcOrDstIp(srcOrDstIp, ipType);
            if (excelRecommendTaskEntity != null) {
                excelRecommendTaskEntity.setSrcIp(srcOrDstIp);
            } else {
                excelBigInternetTaskDTO.setSrcIp(srcOrDstIp);
            }
            if (rc != ReturnCode.POLICY_MSG_OK) {
                log.info("源地址不正确：{}", srcIp);
                verifyBussExcelDTO.setResultCode(rc);
                return;
            }
        } else {
            verifyBussExcelDTO.setResultCode(ReturnCode.EMPTY_PARAMETERS);
            return;
        }


        String srcOrDstIp1 = verifyNotEmptySrcOrDstIp(dstIp);
        if (excelRecommendTaskEntity != null) {
            excelRecommendTaskEntity.setDstIp(srcOrDstIp1);
        } else {
            excelBigInternetTaskDTO.setDstIp(srcOrDstIp1);
        }

        //转换后的源地址校验
        String postSrcIpStr = verifyNotEmptySrcOrDstIp(postSrcIp);
        if (StringUtils.isNotBlank(postSrcIpStr)) {
            rc = verifySrcOrDstIp(postSrcIpStr, ipType);
            if (excelRecommendTaskEntity != null) {
                excelRecommendTaskEntity.setPostSrcIp(postSrcIpStr);
            }
            if (rc != ReturnCode.POLICY_MSG_OK) {
                log.info("转换后的源地址不正确：{}", postSrcIp);
                verifyBussExcelDTO.setResultCode(rc);
                return;
            }
        }

        // 转换后的目的地址校验
        String postDstIpStr = verifyNotEmptySrcOrDstIp(postDstIp);
        if (StringUtils.isNotBlank(postDstIpStr)) {
            rc = verifySrcOrDstIp(postDstIpStr, ipType);
            if (excelRecommendTaskEntity != null) {
                excelRecommendTaskEntity.setPostDstIp(postDstIpStr);
            }
            if (rc != ReturnCode.POLICY_MSG_OK) {
                log.info("转换后的目的地址不正确：{}", postDstIp);
                verifyBussExcelDTO.setResultCode(rc);
                return;
            }
        }


        if (StringUtils.isNotBlank(srcOrDstIp1)) {
            //校验地址中间不能带有空格
            if (ReturnCode.POLICY_MSG_OK < srcOrDstIp1.indexOf(" ")) {
                log.info("目的地址不正确：" + srcOrDstIp1);
                verifyBussExcelDTO.setResultCode(ReturnCode.DST_IP_FORMAT_ERROR_HAS_SPACE);
                return;
            }
        } else {
            verifyBussExcelDTO.setResultCode(ReturnCode.EMPTY_PARAMETERS);
            return;
        }

        if (AliStringUtils.isEmpty(service) || service.toUpperCase().trim().contains(PolicyConstants.POLICY_STR_VALUE_ANY.toUpperCase())) {
            log.info("服务不能为空或者包含any");
            verifyBussExcelDTO.setResultCode(ReturnCode.SERVICE_CANNOT_BE_ANY);
            return;
        }

        List<ServiceDTO> serviceList = new ArrayList<>();
        int ipTypeNum  = IpTypeEnum.IPV4.getCode();
        if (CommonConstants.IPV6.equalsIgnoreCase(ipType)) {
            ipTypeNum = IpTypeEnum.IPV6.getCode();
        }
        rc = verifyService(service, serviceList,ipTypeNum);
        if (rc == ReturnCode.POLICY_MSG_OK) {
            if (excelRecommendTaskEntity != null) {
                excelRecommendTaskEntity.setServiceList(serviceList);
            } else {
                excelBigInternetTaskDTO.setServiceList(serviceList);
            }
        } else {
            verifyBussExcelDTO.setResultCode(rc);
            return;
        }
        rc = verifyIdleTimeout(idleTimeout);
        if(ReturnCode.POLICY_MSG_OK != rc){
            verifyBussExcelDTO.setResultCode(rc);
            return;
        }
        if(StringUtils.isBlank(timeRange)){
            verifyBussExcelDTO.setResultCode(rc);
            return;
        }
        rc = verifyTimeRange(timeRange);
        if (rc == ReturnCode.POLICY_MSG_OK) {
            String[] timeRanges = timeRange.split("-");
            if (timeRanges.length > 1) {
                if(excelRecommendTaskEntity != null){
                    excelRecommendTaskEntity.setStartTime(timeRanges[0].trim());
                    excelRecommendTaskEntity.setEndTime(timeRanges[1].trim());
                }else{
                    excelBigInternetTaskDTO.setStartTime(timeRanges[0].trim());
                    excelBigInternetTaskDTO.setEndTime(timeRanges[1].trim());
                }

            }
        } else {
            log.info("时间校验失败,失败的时间参数为:{}", timeRange);
            verifyBussExcelDTO.setResultCode(rc);
            return;
        }
        verifyBussExcelDTO.setResultCode(rc);
        return;
    }

    @Override
    public VerifyBussExcelDTO verifyRecommendBigInternetExcel(ExcelBigInternetTaskDTO entity) {
        VerifyBussExcelDTO verifyBussExcelDTO = new VerifyBussExcelDTO();
        commonParamVerify(verifyBussExcelDTO, null, entity);
        //校验
        return verifyBussExcelDTO;
    }

    @Override
    public VerifyBussExcelDTO verifyRecommendInternetExcel(ExcelRecommendInternetTaskDTO entity) {
        VerifyBussExcelDTO verifyBussExcelDTO = new VerifyBussExcelDTO();
        String taskType = entity.getTaskType();
        int rc = verifyId(entity.getId());
        if (rc != ReturnCode.POLICY_MSG_OK) {
            verifyBussExcelDTO.setResultCode(ReturnCode.EMPTY_PARAMETERS);
            return verifyBussExcelDTO;
        }
        if (StringUtils.isBlank(taskType) || StringUtils.isBlank(entity.getName())) {
            verifyBussExcelDTO.setResultCode(ReturnCode.EMPTY_PARAMETERS);
            return verifyBussExcelDTO;
        }
        if (StringUtils.isNotBlank(entity.getTaskType())) {
            List<ServiceDTO> serviceList = new ArrayList<>();

            String srcIp = verifyNotEmptySrcOrDstIp(entity.getSrcIp());
            String dstIp = verifyNotEmptySrcOrDstIp(entity.getDstIp());
            String postSrcIp = verifyNotEmptySrcOrDstIp(entity.getPostSrcIp());
            String postDstIp = verifyNotEmptySrcOrDstIp(entity.getPostDstIp());

            if (RecommendTypeEnum.IN_2OUT_RECOMMEND.getDesc().equalsIgnoreCase(entity.getTaskType())) {
                //源地址是必填，目的非必填，服务非必填，标签非必填
                if (StringUtils.isNotBlank(srcIp)) {
                    entity.setSrcIp(srcIp);
                } else {
                    verifyBussExcelDTO.setResultCode(ReturnCode.INVALID_NUMBER);
                    return verifyBussExcelDTO;
                }
                entity.setDstIp(dstIp);
                entity.setPostSrcIp(postSrcIp);
                entity.setPostDstIp(postDstIp);
                //KSH-5038
                rc = verifyService(entity.getService(), serviceList,IpTypeEnum.IPV4.getCode());
                entity.setServiceList(serviceList);
                // 修改互联网开通验证，如果如果不等于空and校验不成功的时候 就提示错误返回给上一级|| 改成&&
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.SERVICE_CANNOT_BE_ANY) {
                    verifyBussExcelDTO.setResultCode(rc);
                    return verifyBussExcelDTO;
                }
            } else {
                //源地址是非必填，目的必填，服务非必填，标签必填,标签模式必填
                entity.setSrcIp(srcIp);
                entity.setPostSrcIp(postSrcIp);
                entity.setPostDstIp(postDstIp);
                if (StringUtils.isNotBlank(dstIp)) {
                    entity.setDstIp(dstIp);
                } else {
                    verifyBussExcelDTO.setResultCode(ReturnCode.INVALID_NUMBER);
                    return verifyBussExcelDTO;
                }
                if (rc != ReturnCode.POLICY_MSG_OK) {
                    verifyBussExcelDTO.setResultCode(ReturnCode.INVALID_NUMBER);
                    return verifyBussExcelDTO;
                }
                rc = verifyService(entity.getService(), serviceList,IpTypeEnum.IPV4.getCode());
                entity.setServiceList(serviceList);
                if (rc != ReturnCode.POLICY_MSG_OK && rc != ReturnCode.SERVICE_CANNOT_BE_ANY) {
                    verifyBussExcelDTO.setResultCode(rc);
                    return verifyBussExcelDTO;
                }
                if (StringUtils.isBlank(entity.getStartLabel()) && StringUtils.isBlank(entity.getLabelModel())) {
                    verifyBussExcelDTO.setResultCode(ReturnCode.EMPTY_PARAMETERS);
                    return verifyBussExcelDTO;
                }

            }

        }
        String timeRange = entity.getTimeRange();
        if(StringUtils.isBlank(timeRange)){
            verifyBussExcelDTO.setResultCode(ReturnCode.POLICY_MSG_OK);
            return verifyBussExcelDTO;
        }
        rc = verifyTimeRange(timeRange);
        if (rc == ReturnCode.POLICY_MSG_OK) {
            String[] timeRanges = timeRange.split("-");
            if (timeRanges.length > 1) {
                entity.setStartTime(timeRanges[0].trim());
                entity.setEndTime(timeRanges[1].trim());
            }
        } else {
            log.info("时间校验失败,失败的时间参数为:{}", timeRange);
            verifyBussExcelDTO.setResultCode(rc);
            return verifyBussExcelDTO;
        }
        verifyBussExcelDTO.setResultCode(ReturnCode.POLICY_MSG_OK);
        return verifyBussExcelDTO;
    }
}
