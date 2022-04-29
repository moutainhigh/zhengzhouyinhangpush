package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.SpecialParamDTO;
import com.abtnetworks.totems.issued.send.SendCommandBeforeBuilderService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushForbidDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 对下发命令针对服务端型号不同特例补全
 */
@Service
public class SendCommandBeforeBuilderServiceImpl implements SendCommandBeforeBuilderService {

    @Value("${issued.command-before-builder.usg6000noTop:save\ny\n}")
    private String usgNoTop;
    @Value("${issued.command-before-builder.cisco:''}")
    private String ciscoEnable;
    @Value("${issued.command-before-builder.h3cV7:''}")
    private String h3cV7PostBuild;
    @Value("${issued.command-before-builder.ssg:''}")
    private String ssgPostBuild;


    @Override
    public String ciscoCommandBuild(String commandLine, String password) {
        StringBuffer command = new StringBuffer();
        if(StringUtils.isNotBlank(ciscoEnable)){
            command.append(ciscoEnable)
                    .append(password.trim()).append(SendCommandStaticAndConstants.LINE_BREAK)
                    .append(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK)
                    .append("write").append(SendCommandStaticAndConstants.LINE_BREAK);
        }else{
            command.append(commandLine);
        }
        return command.toString();
    }

    @Override
    public String u6000CommandBuild(String commandLine, PushForbidDTO pushForbidDTO) {
        if (pushForbidDTO != null && pushForbidDTO.getIsAppendCommandLine() == false) {
            // 华为封禁不拼接以下命令行
            return commandLine;
        }
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK);
        command.append(usgNoTop);
        command.append(SendCommandStaticAndConstants.LINE_BREAK)
                .append(SendCommandStaticAndConstants.LINE_BREAK);

        return command.toString();
    }

    @Override
    public String zteRouterCommandBuild(String commandLine, String password) {
        StringBuffer command = new StringBuffer("enable").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append(password).append(SendCommandStaticAndConstants.LINE_BREAK)
                .append(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK).append("end");
        return command.toString();
    }

    @Override
    public String hillStoneCommandBuild(String commandLine, PushForbidDTO pushForbidDTO) {
        if (pushForbidDTO != null && pushForbidDTO.getIsAppendCommandLine() == false) {
            // 山石封禁不拼接以下命令行
            return commandLine;
        }
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("save").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("yy").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("y").append(SendCommandStaticAndConstants.LINE_BREAK);

        return command.toString();
    }

    @Override
    public String topsecCommandBuild(String commandLine) {
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("save").append(SendCommandStaticAndConstants.LINE_BREAK);

        return command.toString();
    }

    @Override
    public String h3cCommandBuild(String commandLine) {
        //随机生成文件名使用"CONF"+时间戳+".cfg"的形式
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK);
        if (StringUtils.isNotBlank(h3cV7PostBuild)) {
            command.append(h3cV7PostBuild);
        } else {
            command.append("save force").append(SendCommandStaticAndConstants.LINE_BREAK)
                    .append("y").append(SendCommandStaticAndConstants.LINE_BREAK).append(SendCommandStaticAndConstants.LINE_BREAK)
                    .append("y").append(SendCommandStaticAndConstants.LINE_BREAK);
        }
        return command.toString();
    }

    @Override
    public String dpTechCommandBuild(String commandLine) {
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("end").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("write file").append(SendCommandStaticAndConstants.LINE_BREAK);

        return command.toString();
    }

    @Override
    public String u2000BuildCommand(String commandLine) {
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("quit").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("return").append(SendCommandStaticAndConstants.LINE_BREAK);
        return command.toString();
    }

    @Override
    public String netPowerSaveCommand(PushCmdDTO pushCmdDTO) {
        String commandLine = pushCmdDTO.getCommandline();
        StringBuffer command = new StringBuffer(commandLine)
                .append("save configuration").append(SendCommandStaticAndConstants.LINE_BREAK);
        return command.toString();
    }

    @Override
    public String getSshBuildCommand(PushCmdDTO pushCmdDTO) {

        String commandLine = pushCmdDTO.getCommandline();

        if (null != pushCmdDTO.getTaskType() && PolicyConstants.CUSTOMIZE_CMD_PUSH == pushCmdDTO.getTaskType()) {
            //如果是自定义命令行，虚墙添加进入虚墙命令，其他设备不需要对下发命令行做处理
            if (null != pushCmdDTO.getIsVSys() && pushCmdDTO.getIsVSys() && DeviceModelNumberEnum.FORTINET == pushCmdDTO.getDeviceModelNumberEnum()) {
                //暂时只支持飞塔虚墙， 后续需要别的墙再添加
                return addVSysDeviceCmdByCustomize(commandLine, pushCmdDTO.getVSysName());
            }
            return commandLine;
        }

        switch (pushCmdDTO.getDeviceModelNumberEnum()) {
            case ZTE_ROUTER:
            case ZTE_SWITCH:
                return zteRouterCommandBuild(commandLine, pushCmdDTO.getEnablePassword());
            case SSG:
                return junSsgBuildCommand(commandLine);
            case SRX:
                return junSrxBuildCommand(commandLine);
            case HILLSTONE:
            case HILLSTONE_R5:
            case HILLSTONE_V5:
                return hillStoneCommandBuild(commandLine, pushCmdDTO.getPushForbidDTO());
            case CISCO:
            case CISCO_S:
            case CISCO_IOS:
            case CISCO_NX_OS:
            case CISCO_ASA_86:
            case CISCO_ASA_99:
                return ciscoCommandBuild(commandLine, pushCmdDTO.getEnablePassword());
            case USG6000:
            case USG6000_NO_TOP:
                return u6000CommandBuild(commandLine, pushCmdDTO.getPushForbidDTO());
            case USG2000:
                return u2000BuildCommand(commandLine);
            case TOPSEC_NG:
            case TOPSEC_NG2:
            case TOPSEC_NG3:
            case TOPSEC_NG4:
            case TOPSEC_TOS_005:
            case TOPSEC_TOS_010_020:
                return topsecCommandBuild(commandLine);
            case H3CV7:
            case H3CV5:
            case H3CV7_ZONE_PAIR_ACL:
                return h3cCommandBuild(commandLine);
            case DPTECHR003:
            case DPTECHR004:
                return dpTechCommandBuild(commandLine);
            case VENUSTECHVSOS:
            case VENUSTECHVSOS_V263:
                return getVenusTchBuildCommand(commandLine);
            case CHECK_POINT:
                return checkPointBuildCommand(pushCmdDTO);
            case NET_POWER:
            case NET_POWER_JM:
                return netPowerSaveCommand(pushCmdDTO);
            default:
                return commandLine;
        }
    }

    @Override
    public String delSshCommand(DeviceModelNumberEnum lastDeviceModelNumberEnum, String lastCommandLine, String enablePassword, PushForbidDTO pushForbidDTO) {
        switch (lastDeviceModelNumberEnum) {
            case CISCO:
            case CISCO_S:
            case CISCO_IOS:
            case CISCO_NX_OS:
            case CISCO_ASA_86:
            case CISCO_ASA_99:
                return delCiscoCommandBuild(lastCommandLine, enablePassword);
            case HILLSTONE:
                return delHillstoneCommandBuild(lastCommandLine);
            case USG6000:
            case USG6000_NO_TOP:
                return delHuaweiCommandBuild(lastCommandLine, pushForbidDTO);
            case SRX:
                return delSrxCommandBuild(lastCommandLine);
            case SSG:
                return delSsgCommandBuild(lastCommandLine);
            case DPTECHR003:
            case DPTECHR004:
                return delDptechrCommandBuild(lastCommandLine);
            case TOPSEC_TOS_010_020:
            case TOPSEC_NG:
                return delTopSecCommandBuild(lastCommandLine);
            default:
                return lastCommandLine;
        }
    }

    private String delCiscoCommandBuild(String commandLine, String password) {
        StringBuffer beforeCommand = new StringBuffer();
        beforeCommand.append("enable").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append(AliStringUtils.isEmpty(password) ? "" : password.trim()).append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(beforeCommand.toString(), "");
        StringBuffer afterCommand = new StringBuffer();

        afterCommand.append("write").append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(afterCommand.toString(), "");

        return commandLine;
    }

    private String delHillstoneCommandBuild(String commandLine) {
        StringBuffer command = new StringBuffer();
        command.append("save").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("yy").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("y").append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(command.toString(), "");
        return commandLine;
    }

    private String delHuaweiCommandBuild(String commandLine, PushForbidDTO pushForbidDTO) {
        if (pushForbidDTO != null && pushForbidDTO.getIsAppendCommandLine() == false) {
            // 华为封禁不拼接以下命令行
            return commandLine;
        }
        StringBuffer command = new StringBuffer().append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("save").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("y").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append(SendCommandStaticAndConstants.LINE_BREAK)
                .append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(command.toString(), "");
        return commandLine;
    }

    private String delSrxCommandBuild(String commandLine) {
        StringBuffer command = new StringBuffer("cli").append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(command.toString(), "");
        return commandLine;
    }

    private String delSsgCommandBuild(String commandLine) {
        return commandLine;
    }

    private String delDptechrCommandBuild(String commandLine) {
        StringBuffer command = new StringBuffer().append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("end").append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("write file").append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(command.toString(), "");
        return commandLine;
    }

    private String delTopSecCommandBuild(String commandLine) {
        StringBuffer command = new StringBuffer().append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("save").append(SendCommandStaticAndConstants.LINE_BREAK);
        commandLine = commandLine.replace(command.toString(), "");
        return commandLine;
    }

    private String addVSysDeviceCmdByCustomize(String commandLine, String vsyName) {
        StringBuffer sb = new StringBuffer();
        sb.append("config vdom").append(SendCommandStaticAndConstants.LINE_BREAK);
        sb.append("edit " + vsyName).append(SendCommandStaticAndConstants.LINE_BREAK);
        sb.append(commandLine);
        return sb.toString();
    }


    @Override
    public String junSsgBuildCommand(String commandLine) {
        StringBuffer command = new StringBuffer(commandLine);
        //湖北联通现场测试时需要去掉
//                .append("exit").append(SendCommandStaticAndConstants.LINE_BREAK)
//                .append("y").append(SendCommandStaticAndConstants.LINE_BREAK);
        command.append(SendCommandStaticAndConstants.LINE_BREAK);
        command.append(StringUtils.isNotBlank(ssgPostBuild) ? ssgPostBuild : StringUtils.LF);
        return command.toString();
    }

    @Override
    public String junSrxBuildCommand(String commandLine) {
        StringBuffer command = new StringBuffer("cli").append(SendCommandStaticAndConstants.LINE_BREAK).append(commandLine);
        return command.toString();
    }

    @Override
    public String checkPointBuildCommand(PushCmdDTO pushCmdDTO) {
        String commandline = pushCmdDTO.getCommandline();


        String cpMiGatewayClusterName = pushCmdDTO.getSpecialParamDTO().getCpMiGatewayClusterName();
        String policyPackage = "";
        SpecialParamDTO specialParamDTO = pushCmdDTO.getSpecialParamDTO();
        if (specialParamDTO != null) {
            policyPackage = specialParamDTO.getPolicyPackage();
        }
        StringBuffer command = new StringBuffer();
        if (StringUtils.isNotEmpty(cpMiGatewayClusterName)) {
            // 20210113 oppo实施时需要这个
            String installOn = String.format("install-on \"%s\"", cpMiGatewayClusterName);
            commandline = commandline.replace(CommonConstants.INSTALL_LAYER, installOn);
            command.append(commandline);
            if (StringUtils.isNotEmpty(policyPackage)) {
                command.append("mgmt publish\n");
                command.append(" \n");
                String packageTarget = SendCommandStaticAndConstants.CHECK_POINT_PACKAGE_TARGET.replace("#1", policyPackage).replace("#2", cpMiGatewayClusterName);
                command.append(packageTarget);
                command.append(" \n");
            }
        } else {
            commandline = commandline.replace(CommonConstants.INSTALL_LAYER, "");
            command.append(commandline);
        }
        return command.toString();
    }

    @Override
    public String getTelnetBuildCommand(PushCmdDTO pushCmdDTO) {
        StringBuffer command = new StringBuffer();
        switch (pushCmdDTO.getDeviceModelNumberEnum()) {
            case SRX:
                command.append(pushCmdDTO.getUsername().trim()).append(SendCommandStaticAndConstants.LINE_BREAK)
                        .append(pushCmdDTO.getPassword().trim()).append(SendCommandStaticAndConstants.LINE_BREAK)
                        .append(pushCmdDTO.getCommandline());
                String strCommand = command.toString().replace("cli\n", "");
                return strCommand;
            case CISCO:
            case CISCO_S:
            case CISCO_IOS:
            case CISCO_NX_OS:
            case CISCO_ASA_86:
            case CISCO_ASA_99:
                return pushCmdDTO.getCommandline();
            case DPTECHR003:
                command.append(pushCmdDTO.getPassword().trim()).append(SendCommandStaticAndConstants.LINE_BREAK)
                        .append(pushCmdDTO.getCommandline());
                return command.toString();
            default:
                command.append(pushCmdDTO.getUsername().trim()).append(SendCommandStaticAndConstants.LINE_BREAK)
                        .append(pushCmdDTO.getPassword().trim()).append(SendCommandStaticAndConstants.LINE_BREAK)
                        .append(pushCmdDTO.getCommandline());
                return command.toString();
        }
    }


    @Override
    public String getVenusTchBuildCommand(String commandLine) {
        //  移动的sb.append(String.format("policy move %s before %s\n", dto.getName(), dto.getFirstPolicyName()));
        //        sb.append("write me\n");
        StringBuffer command = new StringBuffer(commandLine).append(SendCommandStaticAndConstants.LINE_BREAK)
                .append("write me").append(SendCommandStaticAndConstants.LINE_BREAK);
        return command.toString();
    }
}
