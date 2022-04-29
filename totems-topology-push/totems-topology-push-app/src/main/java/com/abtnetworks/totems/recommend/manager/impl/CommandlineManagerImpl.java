package com.abtnetworks.totems.recommend.manager.impl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.enums.TaskTypeEnum;
import com.abtnetworks.totems.generate.service.platform.GeneratePlatformApiCmdService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.CommandlineManager;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.WESTONE;

@Slf4j
@Service
public class CommandlineManagerImpl implements CommandlineManager {

    private static Logger logger = LoggerFactory.getLogger(CommandlineManagerImpl.class);

    @Autowired
    Map<String, PolicyGenerator> policyGeneratorMap;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private GeneratePlatformApiCmdService generatePlatformApiCmdService;

    @Value("${push.generateObjRollbackCommandLine:false}")
    private Boolean generateObjRollbackCommandLine;


    private static final String PLATFORM_API_REVERT_COMMANDLINE = "API接口不支持命令行显示";

    private static final String PLATFORM_API_NSFOCUS_REVERT_COMMANDLINE = "API接口暂不支持回滚";

    private ProcedureDTO procedure;

    @Override
    public String generate(CmdDTO cmdDTO) {

        // 检查是否适配管理平台API对接
        String checkApiResult = checkManagementPlatformApi(cmdDTO,false);
        if(StringUtils.isNotEmpty(checkApiResult)){
            return checkApiResult;
        }

        String result = checkAdaptIPV6(cmdDTO);
        if(StringUtils.isNotEmpty(result)){
            return result;
        }

        //获取命令行生成流程数据
        ProcedureDTO procedure = cmdDTO.getProcedure();

        String generator = procedure.getGenerator();
        //生成命令行
        String commandLine = String.format("命令行生成器[%s]暂不支持", generator);
        if (policyGeneratorMap.containsKey(generator)) {
            commandLine = policyGeneratorMap.get(generator).generate(cmdDTO);
        }

        return commandLine;
    }

    @Override
    public String generateRollback(CmdDTO cmdDTO) {
        DeviceDTO device = cmdDTO.getDevice();
        DeviceModelNumberEnum modelNumber = device.getModelNumber();

        // 检查是否适配管理平台API对接
        String checkApiResult = checkManagementPlatformApi(cmdDTO,true);
        if(StringUtils.isNotEmpty(checkApiResult)){
            return checkApiResult;
        }

        ProcedureDTO procedure = cmdDTO.getProcedure();
        String rollbackGenerator = procedure.getRollbackGenerator();

        String commandLine = String.format("命令行生成器[%s]暂不支持", rollbackGenerator);

        if (policyGeneratorMap.containsKey(rollbackGenerator)) {

            switch (modelNumber) {
                case USG6000:
                case USG6000_NO_TOP:
                case CISCO:
                case CISCO_ASA_86:
                case CISCO_ASA_99:
                case SRX:
                case SRX_NoCli:
                case HILLSTONE:
                case HILLSTONE_R5:
                case HILLSTONE_V5:
                case H3CV7:
                case H3CV7_OP:
                case TOPSEC_TOS_010_020:
                case FORTINET:
                case FORTINET_V5_2:
                case ABTNETWORKS:
                case SDNWARE:
                case ANHENG:
                case WESTONE:
                case MAIPUMSGFIREWALL:
                    PolicyGeneratorDTO policyGeneratorDTO = policyGeneratorMap.get(rollbackGenerator).generateV2(cmdDTO);
                    String policyRollbackCommandLine = StringUtils.isNotBlank(policyGeneratorDTO.getPolicyRollbackCommandLine()) ? policyGeneratorDTO.getPolicyRollbackCommandLine() : "";
                    String objRollbackCommandLine = StringUtils.isNotBlank(policyGeneratorDTO.getObjectRollbackCommandLine()) ? policyGeneratorDTO.getObjectRollbackCommandLine() : "";
                    if(generateObjRollbackCommandLine){
                        commandLine =  policyRollbackCommandLine + objRollbackCommandLine;
                    }else {
                        commandLine = policyRollbackCommandLine;
                    }
                    return commandLine;
                default:
                    return policyGeneratorMap.get(rollbackGenerator).generate(cmdDTO);
            }

        }
        return commandLine;
    }

    /**
     * 检查是否已适配IPV6
     * @return
     */
    private String checkAdaptIPV6(CmdDTO cmdDTO){
        //获取命令行生成流程数据
        ProcedureDTO procedure = cmdDTO.getProcedure();
        DeviceDTO device = cmdDTO.getDevice();
        DeviceModelNumberEnum modelNumber = device.getModelNumber();
        String generator = procedure.getGenerator();
        //生成命令行
        String commandLine = String.format("命令行生成器[%s]暂不支持", generator);
        PolicyDTO policy = cmdDTO.getPolicy();
        if(ObjectUtils.isEmpty(policy.getIpType()) || policy.getIpType().intValue() == IpTypeEnum.IPV4.getCode()){
            return null;
        } else if( policy.getIpType().intValue() == IpTypeEnum.IPV6.getCode()){
            // 如果是IPV6（已支持 安博通、华为、华三、山石、天融信、深信服、思科、迪普004,JuniperSRX,JuniperSRX NoCli）
            switch (modelNumber) {
                case LEGEND_SEC_NSG_V40:
                case ABTNETWORKS:
                case SDNWARE:
                case ANHENG:
                case WESTONE:
                case MAIPUMSGFIREWALL:
                case USG6000:
                case HILLSTONE:
                case HILLSTONE_R5:
                case HILLSTONE_V5:
                case TOPSEC_TOS_005:
                case TOPSEC_TOS_010_020:
                case TOPSEC_NG:
                case H3CV7:
                case SANG_FOR_IMAGE:
                case CISCO_ASA_99:
                case CISCO_IOS:
                case CISCO_NX_OS:
                case PALO_ALTO:
                case CHECK_POINT:
                case DPTECHR004:
                case FORTINET:
                case FORTINET_V5_2:
                case SRX:
                case SRX_NoCli:
                case JUNIPER_ROUTER:
                    return null;
                default:
                    return commandLine;
            }
        } else {
            // URL类型（已支持安博通、山石、飞塔、思科99,JuniperSRX,JuniperSRX NoCli）
            switch (modelNumber) {
                case ABTNETWORKS:
                case SDNWARE:
                case ANHENG:
                case MAIPUMSGFIREWALL:
                case HILLSTONE:
                case HILLSTONE_R5:
                case FORTINET:
                case FORTINET_V5_2:
                case H3CV7:
                case CISCO_ASA_99:
                case PALO_ALTO:
                case CHECK_POINT:
                case SRX:
                case SRX_NoCli:
                case HILLSTONE_V5:
                case SANG_FOR_IMAGE:
                    return null;
                default:
                    return commandLine;
            }
        }
    }

    /**
     * 检查是否适配管理平台下发
     * 适配则生成策略信息命令行
     * @param cmdDTO
     * @return
     */
    private String checkManagementPlatformApi(CmdDTO cmdDTO, boolean isRevert) {
        TaskDTO task = cmdDTO.getTask();
        DeviceDTO device = cmdDTO.getDevice();
        logger.info("------构建完的cmdDTO：{}------", JSON.toJSONString(cmdDTO));
        if (null == task || null == task.getTaskTypeEnum()) {
            return null;
        }
        // 查询设备详情
        NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(device.getDeviceUuid());
        logger.info("------查到的设备信息为：{}------", JSON.toJSONString(nodeEntity));
        if (nodeEntity == null || StringUtils.isBlank(nodeEntity.getWebUrl())) {
            return null;
        }
        // 管理平台IP有值，走API下发流程
        switch (device.getModelNumber()) {
            case FORTINET:
            case FORTINET_V5_2:
                // 飞塔管理平台仅支持业务开通和策略生成的安全策略
                if (TaskTypeEnum.SERVICE_TYPE == task.getTaskTypeEnum() || TaskTypeEnum.SECURITY_TYPE == task.getTaskTypeEnum()) {
                    if (isRevert) {
                        // 回滚命令行
                        return PLATFORM_API_REVERT_COMMANDLINE;
                    } else {
                        // 下发命令行
                        return generatePlatformApiCmdService.generateFortinetApiParams(cmdDTO);
                    }
                }
                return null;
            case NSFOCUS:
                // 绿盟支持业务开通和策略生成的安全策略,源NAT,目的NAT
                if (TaskTypeEnum.SERVICE_TYPE == task.getTaskTypeEnum() || TaskTypeEnum.SECURITY_TYPE == task.getTaskTypeEnum() ||
                        TaskTypeEnum.SNAT_TYPE == task.getTaskTypeEnum() || TaskTypeEnum.DNAT_TYPE == task.getTaskTypeEnum()) {
                    if (isRevert) {
                        // 回滚命令行
                        return PLATFORM_API_NSFOCUS_REVERT_COMMANDLINE;
                    } else {
                        // 下发命令行
                        return generatePlatformApiCmdService.generateNsfocusApiParams(cmdDTO);
                    }
                }
                return null;
            default:
                return null;
        }
    }

}
