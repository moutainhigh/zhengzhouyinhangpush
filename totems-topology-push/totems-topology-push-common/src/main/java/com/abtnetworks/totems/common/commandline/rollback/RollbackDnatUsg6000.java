package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.Usg.nat.NatUsg6000Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RollbackDnatUsg6000 implements PolicyGenerator {

    protected OverAllGeneratorAbstractBean generatorBean;

    public RollbackDnatUsg6000() {
        generatorBean = new NatUsg6000Impl();
    }
    
    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append("system-view\n");
        if (cmdDTO.getDevice().isVsys()) {
            sb.append("switch vsys " + cmdDTO.getDevice().getVsysName() + "\n");
            sb.append("system-view\n");
        }
        sb.append("nat-policy\n");

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String name = generatedObjectDTO.getPolicyName().replace("-","_");
        sb.append(String.format("undo rule name %s\n", name));

        sb.append("return\n");
        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        StringBuilder sb = new StringBuilder();
        // 判断是否有虚墙
        judgeIsVsy(cmdDTO,sb);

        sb.append("nat-policy\n");

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String name = generatedObjectDTO.getPolicyName().replace("-","_");
        sb.append(String.format("undo rule name %s\n", name));
        sb.append("return\n");



        StringBuilder objRollbackCommandLine = new StringBuilder();
        try {
            log.info("生成对象回滚命令行 参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
            List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
            // 如果没有新建的情况 则不创建对象回滚命令行
            if(CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames)){
                policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                return policyGeneratorDTO;
            }
            // 判断是否有虚墙
            judgeIsVsy(cmdDTO,objRollbackCommandLine);

            if (CollectionUtils.isNotEmpty(addressNames)) {
                objRollbackCommandLine.append(StringUtils.LF);
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectCommandLine(null, null, addressName, null, null));
                }
            }
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                objRollbackCommandLine.append(StringUtils.LF);
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
            }
            objRollbackCommandLine.append("quit\n");
            objRollbackCommandLine.append("return\n");

        } catch (Exception e) {
            log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
        }
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }

    /**
     * 判断虚墙
     * @param cmdDTO
     * @param sb
     */
    private void judgeIsVsy(CmdDTO cmdDTO, StringBuilder sb){
        sb.append("system-view\n");
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        if(deviceDTO.isVsys()) {
            sb.append("switch vsys " + deviceDTO.getVsysName() + "\n");
            sb.append("system-view\n");
        }
    }
}
