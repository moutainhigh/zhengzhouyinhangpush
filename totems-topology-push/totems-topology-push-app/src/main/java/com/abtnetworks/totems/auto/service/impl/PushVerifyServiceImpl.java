package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.constants.DeviceModelNumberConstants;
import com.abtnetworks.totems.auto.service.PushAutoTaskEmailService;
import com.abtnetworks.totems.auto.service.PushVerifyService;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @desc 自动开通下发验证相关接口
 * @author zhoumuhua
 * @date 2021-07-20
 */
@Service
@Slf4j
public class PushVerifyServiceImpl implements PushVerifyService {

    @Autowired
    PushAutoTaskEmailService pushAutoTaskEmailService;

    /**
     * 获取验证的命令行
     * @param pushCmdDTO
     * @param pushResultDTO
     * @return
     */
    @Override
    public String getVerifyCmd (PushCmdDTO pushCmdDTO, PushResultDTO pushResultDTO) {
        //如果下发成功才进行验证
        String commandline = pushCmdDTO.getCommandline();
        DeviceModelNumberEnum deviceModelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        Integer taskType = pushCmdDTO.getTaskType();
        switch (deviceModelNumberEnum) {
            case H3CV7:
                return getShowH3CCmd(commandline, taskType);
            case HILLSTONE:
            case HILLSTONE_R5:
                /*if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == pushCmdDTO.getTaskType()
                        || PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT ==pushCmdDTO.getTaskType()) {
                    //如果是源nat和目的nat用下发回显来抓取策略id
                }*/
                //山石设备安全设备
                commandline = pushResultDTO.getCmdEcho();
                return getShowHillSTONECmd(commandline, taskType);
            case FORTINET:
                commandline = pushResultDTO.getCmdEcho();
                return getShowFORTINETCmd(commandline, taskType, pushCmdDTO.getPolicyIdList());
            case DPTECHR004:
                return getShowDPTECHRCmd(commandline, taskType);
            default:
                return null;
        }

    }

    /**
     * 获取H3V7的验证命令行
     * @param commandline
     * @param taskType
     * @return
     */
    private String getShowH3CCmd(String commandline, Integer taskType) {
        String modelNumber = DeviceModelNumberConstants.H3CV7_MODELNUMBER;

        Set<String> policyNameList = pushAutoTaskEmailService.getPolicyName(commandline, modelNumber);

        StringBuffer sb = new StringBuffer();
        for (String name : policyNameList) {
            if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType
                    || PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType) {
                sb.append("display nat global-policy | begin ").append(name).append(CommonConstants.LINE_BREAK);
            } else {
                sb.append("dis security-policy ip | begin ").append(name).append(CommonConstants.LINE_BREAK);
            }
        }
        return sb.toString();
    }

    /**
     * 获取山石的验证命令行
     * @param commandline
     * @param taskType
     * @return
     */
    private String getShowHillSTONECmd(String commandline, Integer taskType) {
        String modelNumber = DeviceModelNumberConstants.HILLSTONER5_SECURITY_ID;

        if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType
                || PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT ==taskType) {
            modelNumber = DeviceModelNumberConstants.HILLSTONER5_MODELNUMBER_ID;
        }

        Set<String> policyNameList = pushAutoTaskEmailService.getPolicyName(commandline, modelNumber);
        StringBuffer sb = new StringBuffer();
        if (ObjectUtils.isNotEmpty(policyNameList)) {
            for (String name : policyNameList) {
                if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType) {
                    sb.append("show snat id ").append(name).append(CommonConstants.LINE_BREAK);
                } else if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType) {
                    sb.append("show dnat id ").append(name).append(CommonConstants.LINE_BREAK);
                } else {
                    String policyId = getHillstoneSecurityPolicyId(name);
                    if (StringUtils.isNotEmpty(policyId)) {
                        //匹配到策略id不为空才进行验证命令行拼接
                        sb.append("show policy id ").append(policyId).append(CommonConstants.LINE_BREAK);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取飞塔的验证命令行
     * @param commandline
     * @param taskType
     * @return
     */
    private String getShowFORTINETCmd(String commandline, Integer taskType, List<String> policyIdList) {
        String modelNumber = DeviceModelNumberConstants.FORTINET_MODELNUMBER_ID;

        Set<String> policyNameList = pushAutoTaskEmailService.getPolicyName(commandline, modelNumber);
        log.info("飞塔命令行解析出来的name为：{},taskType为{}",policyNameList,taskType);
        StringBuffer sb = new StringBuffer();
        if (ObjectUtils.isNotEmpty(policyNameList)) {
            for (String name : policyNameList) {
                if (PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType) {
                    if (com.abtnetworks.totems.common.utils.StringUtils.isNumeric(name) && policyIdList.contains(name)) {
                        sb.append("show firewall policy ").append(name).append(CommonConstants.LINE_BREAK);
                    } else {
                        String secuirtyPolicyName = getFortinetSecuirtyPolicyName(name);
                        if (StringUtils.isNotEmpty(secuirtyPolicyName)) {
                            //匹配到策略名称不为空才进行验证命令行拼接
                            sb.append("show firewall vip ").append(name).append(CommonConstants.LINE_BREAK);
                        }
                    }
                } else {
                    if (com.abtnetworks.totems.common.utils.StringUtils.isNumeric(name) && policyIdList.contains(name)) {
                        sb.append("show firewall policy ").append(name).append(CommonConstants.LINE_BREAK);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取迪普的验证命令行
     * @param commandline
     * @param taskType
     * @return
     */
    private String getShowDPTECHRCmd(String commandline, Integer taskType) {
        String modelNumber = DeviceModelNumberConstants.DPTECHR004_MODELNUMBER;

        Set<String> policyNameList = pushAutoTaskEmailService.getPolicyName(commandline, modelNumber);

        StringBuffer sb = new StringBuffer();
        for (String name : policyNameList) {
            sb.append("show policy name ").append(name).append(CommonConstants.LINE_BREAK);
        }
        return sb.toString();
    }

    /**
     * 从山石安全策略回显中抓取策略id
     * @param name
     * @return
     */
    public String getHillstoneSecurityPolicyId(String name) {
        String[] names = name.split(" ");
        String str = "";
        for (String id : names) {
            if (com.abtnetworks.totems.common.utils.StringUtils.isNumeric(id)) {
                str = id;
                break;
            }
        }
        return str;
    }

    /**
     * 从飞塔安全策略命令行中抓取策略名称
     * @param name
     * @return
     */
    private String getFortinetSecuirtyPolicyName(String name) {
        if (name.contains("mip_")) {
            return name;
        }
        return "";
    }


    public static void main(String[] args) {
        PushVerifyServiceImpl impl = new PushVerifyServiceImpl();
        String hillstoneSecurityPolicyId = impl.getHillstoneSecurityPolicyId("13 is created");

        System.out.println(hillstoneSecurityPolicyId);
    }
}
