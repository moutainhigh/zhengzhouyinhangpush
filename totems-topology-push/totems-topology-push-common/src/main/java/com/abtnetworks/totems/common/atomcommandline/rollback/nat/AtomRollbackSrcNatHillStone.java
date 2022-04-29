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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abtnetworks.totems.common.constants.CommonConstants.PLACE_HOLDER;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/7/30
 */
@Service
@Log4j2
public class AtomRollbackSrcNatHillStone extends BaseHillStoneCommonBuss implements PolicyGenerator {

    private NatHillStoneR5Impl generatorBean;

    public AtomRollbackSrcNatHillStone() {
        generatorBean = new NatHillStoneR5Impl();
    }


    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        sb.append(generatorBean.generatePreCommandline(deviceDTO.isVsys(),deviceDTO.getVsysName(),null,null));
        Map<String, Object> map = new HashMap<>(5);
        map.put("rollbackType",cmdDTO.getSetting().getRollbackType());
        sb.append(generatorBean.deleteNatPolicyByIdOrName(NatTypeEnum.SRC,PLACE_HOLDER,generatedObjectDTO.getPolicyName(),map,null));
        sb.append(generatorBean.generatePostCommandline(null,null));
        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        StringBuilder sb = new StringBuilder();
        DeviceDTO deviceDTO =  cmdDTO.getDevice();
        sb.append(generatorBean.generatePreCommandline(deviceDTO.isVsys(),deviceDTO.getVsysName(),null,null));
        Map<String, Object> map = new HashMap<>(5);
        map.put("rollbackType",cmdDTO.getSetting().getRollbackType());
        sb.append(generatorBean.deleteNatPolicyByIdOrName(NatTypeEnum.SRC,PLACE_HOLDER,generatedObjectDTO.getPolicyName(),map,null));
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
