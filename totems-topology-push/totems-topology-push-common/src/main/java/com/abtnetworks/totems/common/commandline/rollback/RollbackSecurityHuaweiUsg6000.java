package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class RollbackSecurityHuaweiUsg6000  implements PolicyGenerator {
    
    protected OverAllGeneratorAbstractBean generatorBean;


    public RollbackSecurityHuaweiUsg6000(){
        generatorBean = new SecurityUsg6000Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO){

        StringBuilder sb = new StringBuilder();
        judgeIsVsy( cmdDTO,  sb);
        sb.append("security-policy\n");

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String name = generatedObjectDTO.getPolicyName().replace("-","_");
        sb.append(String.format("undo rule name %s\n", name));

        sb.append("return\n");

        return sb.toString();
    }

    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new  PolicyGeneratorDTO();

        StringBuilder sb = new StringBuilder();
        judgeIsVsy( cmdDTO,  sb);
        sb.append("security-policy\n");

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        String name = generatedObjectDTO.getPolicyName().replace("-","_");
        sb.append(String.format("undo rule name %s\n", name));
        sb.append("return\n");


        // 对象回滚命令行
        StringBuilder objRollbackCommandLine = new StringBuilder();
        try {
            log.info("生成对象回滚命令行的参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
            List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
            List<String> timeNames = generatedObjectDTO.getTimeObjectNameList();
            // 如果没有新建的情况 则不创建对象回滚命令行
            if(CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceNames) &&CollectionUtils.isEmpty(timeNames)){
                policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                return policyGeneratorDTO;
            }
            objRollbackCommandLine.append(StringUtils.LF);
            judgeIsVsy(cmdDTO, objRollbackCommandLine);

            if (CollectionUtils.isNotEmpty(addressNames)) {
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectCommandLine(null, null, addressName, null, null));
                }
            }
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
            }

            if(CollectionUtils.isNotEmpty(timeNames)){
                for (String time :timeNames){
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(time)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteAbsoluteTimeCommandLine(time, null, null));
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

    public static void main(String[] args) {
        RollbackSecurityHuaweiUsg6000 usg6000 = new RollbackSecurityHuaweiUsg6000();
        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setVsys(true);
        deviceDTO.setVsysName("test1");
        cmdDTO.setDevice(deviceDTO);
        GeneratedObjectDTO generatedObjectDTO = new GeneratedObjectDTO();
        generatedObjectDTO.setPolicyName("policy-test1");
        List<String> serviceObjectNameList = new ArrayList<>();
        List<String> addressObjectNameList = new ArrayList<>();
        List<String> timeObjectNameList = new ArrayList<>();
        serviceObjectNameList.add("service1");
        addressObjectNameList.add("address1");
        timeObjectNameList.add("time1");
        generatedObjectDTO.setServiceObjectNameList(serviceObjectNameList);
        generatedObjectDTO.setAddressObjectNameList(addressObjectNameList);
        generatedObjectDTO.setTimeObjectNameList(timeObjectNameList);

        cmdDTO.setGeneratedObject(generatedObjectDTO);


        String commandLine = usg6000.generate(cmdDTO);
        System.out.println("commandline:\n" + commandLine);
    }
}
