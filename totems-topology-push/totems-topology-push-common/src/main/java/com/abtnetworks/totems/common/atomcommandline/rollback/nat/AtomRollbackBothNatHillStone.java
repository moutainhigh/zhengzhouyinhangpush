package com.abtnetworks.totems.common.atomcommandline.rollback.nat;

import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.common.atomcommandline.base.BaseHillStoneCommonBuss;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.hillstone.nat.NatHillStoneR5Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abtnetworks.totems.common.constants.CommonConstants.PLACE_HOLDER;
import static com.abtnetworks.totems.common.constants.CommonConstants.PLACE_HOLDER_2;

/**
 * @author lifei
 * @desc 原子化命令行回滚山石nat命令行执行器
 * @date 2021/10/20 10:52
 */
@Service
@Log4j2
public class AtomRollbackBothNatHillStone extends BaseHillStoneCommonBuss implements PolicyGenerator {

    private NatHillStoneR5Impl generatorBean;

    public AtomRollbackBothNatHillStone() {
        generatorBean = new NatHillStoneR5Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        Map<String, Object> map = new HashMap<>(5);
        map.put("rollbackType",cmdDTO.getSetting().getRollbackType());
        sb.append(generatorBean.generatePreCommandline(deviceDTO.isVsys(),deviceDTO.getVsysName(),null,null));
        sb.append(generatorBean.deleteNatPolicyByIdOrName(NatTypeEnum.SRC,PLACE_HOLDER,generatedObjectDTO.getPolicyName(),map,null));
        // 由于原子化命令行实现前面有nat，导致回滚的时候报错，在这里手动退出一层
        sb.append("exit").append(StringUtils.LF);
        sb.append(generatorBean.deleteNatPolicyByIdOrName(NatTypeEnum.DST,PLACE_HOLDER_2,generatedObjectDTO.getPolicyName(),map,null));
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        Map<String, Object> map = new HashMap<>(5);
        map.put("rollbackType",cmdDTO.getSetting().getRollbackType());
        sb.append(generatorBean.generatePreCommandline(deviceDTO.isVsys(),deviceDTO.getVsysName(),null,null));
        sb.append(generatorBean.deleteNatPolicyByIdOrName(NatTypeEnum.SRC,PLACE_HOLDER,generatedObjectDTO.getPolicyName(),map,null));
        // 由于原子化命令行实现前面有nat，导致回滚的时候报错，在这里手动退出一层
        sb.append("exit").append(StringUtils.LF);
        sb.append(generatorBean.deleteNatPolicyByIdOrName(NatTypeEnum.DST,PLACE_HOLDER_2,generatedObjectDTO.getPolicyName(),map,null));
        sb.append(generatorBean.generatePostCommandline(null,null));


        StringBuilder objRollbackCommandLine = new StringBuilder();
        log.info("生成对象回滚命令行的参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
        List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
        List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
        List<String> serviceObjectGroupNameList = generatedObjectDTO.getServiceObjectGroupNameList();
        List<String> timeNames = generatedObjectDTO.getTimeObjectNameList();
        if (CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames) && CollectionUtils.isEmpty(timeNames)
                && CollectionUtils.isEmpty(serviceObjectGroupNameList)) {
            policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
            return policyGeneratorDTO;
        }

        super.rollbackHillStoneObject(objRollbackCommandLine,generatorBean,cmdDTO);
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }
}
