package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.service.ParticularParamCommonService;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.abtnetworks.totems.common.constants.CommonConstants.*;
import static com.abtnetworks.totems.common.constants.PolicyConstants.*;


/**
 * @author Administrator
 * @Title:
 * @Description: 作为下发前参数特例的准备
 * @date 2021/7/30
 */
@Service
@Log4j2
public class ParticularParamCommonServiceImpl implements ParticularParamCommonService {

    @Value("${HillStone.nat.command-match-id}")
    private String hillPatternId;

    @Value("${HillStone.security.command-match-id}")
    private String hillSecurityPatternId;

    @Value("${FortV5.command-match-id}")
    private String fortPatternId;

    @Value("${Cisco.command-match-id}")
    private String cisCoPatternId;

    @Value("${H3CV5.command-match-id}")
    private String h3cv5PatternId;





    @Override
    public void getPolicyIdForRollback(CommandTaskEditableEntity commandTaskEntity, PushCmdDTO pushCmdDTO) {
        DeviceModelNumberEnum deviceModelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        Boolean rollbackType = pushCmdDTO.getRevert();
        Integer taskType = commandTaskEntity.getTaskType();
        // 山石nat策略
        boolean rangeHillStoneCode = DeviceModelNumberEnum.isRangeHillStoneCode(deviceModelNumberEnum.getCode());
        boolean isNatTaskType = ObjectUtils.isNotEmpty(taskType) && (POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType.intValue() || POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType.intValue()
                || POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT == taskType.intValue());
        boolean isSecurityTaskType = ObjectUtils.isNotEmpty(taskType) && (POLICY_INT_PUSH_TASK_TYPE_POLICY_RECOMMEND == taskType.intValue() || POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATED == taskType.intValue());

        // 山石回滚获取下发明细里里面的策略id
        hillstoneRollbackGetPolicyId(commandTaskEntity, rollbackType, taskType, rangeHillStoneCode, isNatTaskType, isSecurityTaskType);
        // 飞塔回滚获取下发明细里面的策略id
        fortRollbackGetPolicyId(commandTaskEntity, deviceModelNumberEnum, rollbackType);
        // 思科acl命令行回滚从下发明细中获取id，将id更新到回滚命令行
        ciscoRollbackGetPolicyId(commandTaskEntity, deviceModelNumberEnum, rollbackType);
        // 华三v5回滚获取下发明细里面的策略id
        h3cv5RollbackGetPolicyId(commandTaskEntity, deviceModelNumberEnum, rollbackType);
    }


    /**
     * 山石回滚获取下发明细里里面的策略id[
     *
     * @param commandTaskEntity
     * @param rollbackType
     * @param taskType
     * @param rangeHillStoneCode
     * @param isNatTaskType
     * @param isSecurityTaskType
     */
    private void hillstoneRollbackGetPolicyId(CommandTaskEditableEntity commandTaskEntity, Boolean rollbackType, Integer taskType, boolean rangeHillStoneCode, boolean isNatTaskType, boolean isSecurityTaskType) {
        String commandlineEcho = commandTaskEntity.getCommandlineEcho();
        if (rangeHillStoneCode && ObjectUtils.isNotEmpty(rollbackType) && rollbackType) {
            if (ObjectUtils.isNotEmpty(commandTaskEntity) && isNatTaskType) {
                Matcher matcher = Pattern.compile(hillPatternId).matcher(commandlineEcho);
                // 对于bothNat，有两个id需要去匹配
                List<String> ruleIds = new ArrayList<>();
                while (matcher.find()) {
                    String ruleId = matcher.group("id");
                    ruleIds.add(ruleId);
                }
                String commandlineRevert = commandTaskEntity.getCommandlineRevert();

                if (CollectionUtils.isNotEmpty(ruleIds) && StringUtils.isNotBlank(commandlineRevert)) {
                    // snat 和dnat的情况
                    if (commandlineRevert.contains(PLACE_HOLDER) && POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT != taskType.intValue()) {
                        commandlineRevert = commandlineRevert.replace(PLACE_HOLDER, ruleIds.get(0));
                        commandTaskEntity.setCommandlineRevert(commandlineRevert);
                    }

                    // bothNat的情况
                    if (commandlineRevert.contains(PLACE_HOLDER) && commandlineRevert.contains(PLACE_HOLDER_2) && POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT == taskType.intValue()) {
                        commandlineRevert = commandlineRevert.replace(PLACE_HOLDER, ruleIds.get(0));
                        if (ruleIds.size() > 1) {
                            commandlineRevert = commandlineRevert.replace(PLACE_HOLDER_2, ruleIds.get(1));
                        }
                        commandTaskEntity.setCommandlineRevert(commandlineRevert);
                    }

                }
            } else if (ObjectUtils.isNotEmpty(commandTaskEntity) && isSecurityTaskType) {
                // 山石安全策略回滚
                Matcher matcher = Pattern.compile(hillSecurityPatternId).matcher(commandlineEcho);
                // 对于那种一个工单多条策略的情况(且是同一台设备的情况) 会有多个ruleid的回显,需要去循环匹配
                List<String> ruleIds = new ArrayList<>();
                while (matcher.find()) {
                    String ruleId = matcher.group("id");
                    ruleIds.add(ruleId);
                }
                String commandlineRevert = commandTaskEntity.getCommandlineRevert();

                if (CollectionUtils.isNotEmpty(ruleIds) && StringUtils.isNotBlank(commandlineRevert)) {
                    String[] revertCommandLines = commandlineRevert.split(LINE_BREAK);
                    int index = 0;
                    StringBuffer revertSb = new StringBuffer();
                    for (String revertCommandLine : revertCommandLines) {
                        if (revertCommandLine.contains(PLACE_HOLDER)) {
                            revertCommandLine = revertCommandLine.replace(PLACE_HOLDER, ruleIds.get(index));
                            revertSb.append(revertCommandLine).append(LINE_BREAK);
                            index++;
                        } else {
                            revertSb.append(revertCommandLine).append(LINE_BREAK);
                        }
                    }
                    commandTaskEntity.setCommandlineRevert(revertSb.toString());
                }
            }
        }
    }


    /**
     * 飞塔 安全策略，nat策略。获取下发的时候的回显用正则匹配然后，将id更新到回滚命令行
     *
     * @param commandTaskEntity
     * @param deviceModelNumberEnum
     * @param rollbackType
     */
    private void fortRollbackGetPolicyId(CommandTaskEditableEntity commandTaskEntity, DeviceModelNumberEnum deviceModelNumberEnum, Boolean rollbackType) {
        boolean isFortinet = DeviceModelNumberEnum.FORTINET.getCode() == deviceModelNumberEnum.getCode() ||
                DeviceModelNumberEnum.FORTINET_V5.getCode() == deviceModelNumberEnum.getCode() ||
                DeviceModelNumberEnum.FORTINET_V5_2.getCode() == deviceModelNumberEnum.getCode();
        if (isFortinet && ObjectUtils.isNotEmpty(rollbackType) && rollbackType) {
            if (ObjectUtils.isNotEmpty(commandTaskEntity)) {
                String commandlineEcho = commandTaskEntity.getCommandlineEcho();
                Matcher matcher = Pattern.compile(fortPatternId).matcher(commandlineEcho);
                List<String> ruleIds = new ArrayList<>();

                while (matcher.find()) {
                    String ruleId = matcher.group("id");
                    ruleIds.add(ruleId);
                }

                String commandlineRevert = commandTaskEntity.getCommandlineRevert();
                if (CollectionUtils.isNotEmpty(ruleIds) && StringUtils.isNotBlank(commandlineRevert)) {
                    // 统计回滚命令行中占位符的数量，如果和下发回显正则匹配到的数量不一样抛出异常
                    Matcher countMatcher = Pattern.compile(FORTINET_PLACE_HOLDER).matcher(commandlineRevert);
                    int count = 0;
                    while (countMatcher.find()) {
                        count++;
                    }
                    if(ruleIds.size() != count){
                        log.error("当前飞塔设备回滚匹配到的下发明细策略id的数量和回滚命令行中需要id的数量不一致,跳过获取id操作");
                        return;
                    }


                    String[] commandlineReverts = commandlineRevert.split(StringUtils.LF);
                    int index = 0;
                    // 替换掉所有回滚命令行中的占位符
                    StringBuffer sb = new StringBuffer();
                    for (String itemCommandLine : commandlineReverts) {
                        if (itemCommandLine.contains(FORTINET_PLACE_HOLDER)) {
                            itemCommandLine = itemCommandLine.replace(FORTINET_PLACE_HOLDER, ruleIds.get(index));
                            index++;
                        }
                        sb.append(itemCommandLine).append(StringUtils.LF);
                    }
                    commandTaskEntity.setCommandlineRevert(sb.toString());
                }
            }
        }
    }

    public static void main(String[] args) {
//        String  commandlineEcho  ="\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall address\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # edit \"10.10.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (10.10.1.55) # set subnet 10.10.1.55/32\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (10.10.1.55) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall address\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # edit \"30.30.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (30.30.1.55) # set subnet 30.30.1.55/32\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (30.30.1.55) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall service custom\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (custom) # edit \"tcp222\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (tcp222) # set protocol TCP/UDP/SCTP\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (tcp222) # set tcp-portrange 222\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (tcp222) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (custom) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall policy\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (policy) # edit 0 \n" +
//                "\n" +
//                "new entry '0' added\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set name ft2101_1_AO_1636\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set srcintf port2 \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set dstintf port4 \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set srcaddr \"10.10.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set dstaddr \"30.30.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set service  \"tcp222\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set schedule \"always\"\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set action accept\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set logtraffic all\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set comments miaoshu\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # show\n" +
//                "\n" +
//                "config firewall policy\n" +
//                "    edit 3\n" +
//                "        set name \"ft2101_1_AO_1636\"\n" +
//                "        set uuid 46ebd344-92bc-51ec-c1ca-dc5e6b7efe46\n" +
//                "        set srcintf \"port2\"\n" +
//                "        set dstintf \"port4\"\n" +
//                "        set srcaddr \"10.10.1.55\"\n" +
//                "        set dstaddr \"30.30.1.55\"\n" +
//                "        set action accept\n" +
//                "        set status enable\n" +
//                "        set schedule \"always\"\n" +
//                "        set service \"tcp222\"\n" +
//                "        set logtraffic all\n" +
//                "        set comments \"miaoshu\"\n" +
//                "    next\n" +
//                "end\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (policy) # move 3 before 2\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (policy) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall address\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # edit \"20.20.1.55\" \n" +
//                "\n" +
//                "new entry '20.20.1.55' added\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (20.20.1.55) # set subnet 20.20.1.55/32\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (20.20.1.55) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall address\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # edit \"30.30.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (30.30.1.55) # set subnet 30.30.1.55/32\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (30.30.1.55) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (address) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall service custom\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (custom) # edit \"tcp222\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (tcp222) # set protocol TCP/UDP/SCTP\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (tcp222) # set tcp-portrange 222\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (tcp222) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (custom) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM # config firewall policy\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (policy) # edit 0 \n" +
//                "\n" +
//                "new entry '0' added\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set name ft2101_2_AO_8302\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set srcintf port3 \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set dstintf port4 \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set srcaddr \"20.20.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set dstaddr \"30.30.1.55\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set service  \"tcp222\" \n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set schedule \"always\"\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set action accept\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set logtraffic all\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # set comments miaoshu\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # show\n" +
//                "\n" +
//                "config firewall policy\n" +
//                "    edit 4\n" +
//                "        set name \"ft2101_2_AO_8302\"\n" +
//                "        set uuid 46fb3f46-92bc-51ec-9b58-ce294db0660c\n" +
//                "        set srcintf \"port3\"\n" +
//                "        set dstintf \"port4\"\n" +
//                "        set srcaddr \"20.20.1.55\"\n" +
//                "        set dstaddr \"30.30.1.55\"\n" +
//                "        set action accept\n" +
//                "        set status enable\n" +
//                "        set schedule \"always\"\n" +
//                "        set service \"tcp222\"\n" +
//                "        set logtraffic all\n" +
//                "        set comments \"miaoshu\"\n" +
//                "    next\n" +
//                "end\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # \n" +
//                "\n" +
//                "FortiGate-VM64-KVM (0) # next\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (policy) # move 4 before 2\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM (policy) # end\n" +
//                "\n" +
//                "\n" +
//                "FortiGate-VM64-KVM #";
//        Matcher matcher = Pattern.compile("edit\\s+((?!0)(?<id>\\d+))").matcher(commandlineEcho);
//        List<String> ruleIds = new ArrayList<>();
//
//        while (matcher.find()) {
//            String ruleId = matcher.group("id");
//            ruleIds.add(ruleId);
//        }
//        System.out.println(ruleIds);
    }

    /**
     * 思科acl命令行回滚从下发明细中获取id
     *
     * @param commandTaskEntity
     * @param deviceModelNumberEnum
     * @param rollbackType
     */
    private void ciscoRollbackGetPolicyId(CommandTaskEditableEntity commandTaskEntity, DeviceModelNumberEnum deviceModelNumberEnum, Boolean rollbackType) {
        boolean isCisCo = DeviceModelNumberEnum.CISCO_IOS.getCode() == deviceModelNumberEnum.getCode() ||
                DeviceModelNumberEnum.CISCO_NX_OS.getCode() == deviceModelNumberEnum.getCode();
        if (isCisCo && ObjectUtils.isNotEmpty(rollbackType) && rollbackType) {
            if (ObjectUtils.isNotEmpty(commandTaskEntity)) {
                String commandlineEcho = commandTaskEntity.getCommandlineEcho();
                Matcher matcher = Pattern.compile(cisCoPatternId).matcher(commandlineEcho);
                List<String> ruleIds = new ArrayList<>();
                while (matcher.find()) {
                    String ruleId = matcher.group("id");
                    ruleIds.add(ruleId);
                }

                String commandlineRevert = commandTaskEntity.getCommandlineRevert();

                if (CollectionUtils.isNotEmpty(ruleIds) && StringUtils.isNotBlank(commandlineRevert)) {
                    String[] revertCommandLines = commandlineRevert.split(LINE_BREAK);
                    int index = 0;
                    StringBuffer revertSb = new StringBuffer();
                    for (String revertCommandLine : revertCommandLines) {
                        if (revertCommandLine.contains(POLICY_ID)) {
                            revertCommandLine = revertCommandLine.replace(POLICY_ID, ruleIds.get(index));
                            revertSb.append(revertCommandLine).append(LINE_BREAK);
                            index++;
                        } else {
                            revertSb.append(revertCommandLine).append(LINE_BREAK);
                        }
                    }
                    commandTaskEntity.setCommandlineRevert(revertSb.toString());
                }
            }
        }
    }

    /**
     * 华三v5命令行回滚从下发明细中获取id
     *
     * @param commandTaskEntity
     * @param deviceModelNumberEnum
     * @param rollbackType
     */
    private void h3cv5RollbackGetPolicyId(CommandTaskEditableEntity commandTaskEntity, DeviceModelNumberEnum deviceModelNumberEnum, Boolean rollbackType) {
        boolean isH3CV5 = DeviceModelNumberEnum.H3CV5.getCode() == deviceModelNumberEnum.getCode();
        if (isH3CV5 && ObjectUtils.isNotEmpty(rollbackType) && rollbackType) {
            if (ObjectUtils.isNotEmpty(commandTaskEntity)) {
                String commandlineEcho = commandTaskEntity.getCommandlineEcho();
                Matcher matcher = Pattern.compile(h3cv5PatternId).matcher(commandlineEcho);
                String ruleId = "";
                if (matcher.find()) {
                    ruleId = matcher.group("id");
                }

                String commandlineRevert = commandTaskEntity.getCommandlineRevert();

                if (StringUtils.isNotEmpty(ruleId) && StringUtils.isNotBlank(commandlineRevert)) {
                    String[] revertCommandLines = commandlineRevert.split(LINE_BREAK);
                    StringBuffer revertSb = new StringBuffer();
                    for (String revertCommandLine : revertCommandLines) {
                        if (revertCommandLine.contains(POLICY_ID)) {
                            revertCommandLine = revertCommandLine.replace(POLICY_ID, ruleId);
                            revertSb.append(revertCommandLine).append(LINE_BREAK);
                        } else {
                            revertSb.append(revertCommandLine).append(LINE_BREAK);
                        }
                    }
                    commandTaskEntity.setCommandlineRevert(revertSb.toString());
                }
            }
        }
    }

}
