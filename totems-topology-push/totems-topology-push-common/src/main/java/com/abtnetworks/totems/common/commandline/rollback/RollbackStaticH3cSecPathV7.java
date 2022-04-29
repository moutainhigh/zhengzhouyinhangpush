package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.h3c.nat.NatH3cSecPathV7Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @desc 华3V7 回滚NAT策略
 * @author zhoumuhua
 * @date 2021-6-17
 */
@Slf4j
@Service
public class RollbackStaticH3cSecPathV7 implements PolicyGenerator {

    private OverAllGeneratorAbstractBean generatorBean;

    public RollbackStaticH3cSecPathV7(){
        generatorBean = new NatH3cSecPathV7Impl();
    }

    @Override
    public String generate(CmdDTO cmdDTO) {
        StringBuilder sb = new StringBuilder();
        // 判断是否有虚墙
        judgeIsVsy(cmdDTO, sb);

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        List<String> addressNameList = generatedObjectDTO.getAddressNameList();

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        String postAddress = StringUtils.isEmpty(existObjectDTO.getPostDstAddressObjectName()) ? addressNameList.get(0) : existObjectDTO.getPostDstAddressObjectName();
//        String dstAddressName =  StringUtils.isEmpty(existObjectDTO.getDstAddressObjectName()) ? addressNameList.get(1) : existObjectDTO.getDstAddressObjectName();

        sb.append("undo nat static outbound object-group ").append(postAddress).append("\n");

        sb.append("quit\n");
        return sb.toString();
    }


    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {
        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        StringBuilder sb = new StringBuilder();
        // 判断是否有虚墙
        judgeIsVsy(cmdDTO, sb);

        GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
        List<String> addressNameList = generatedObjectDTO.getAddressNameList();

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();
        String postAddress = StringUtils.isEmpty(existObjectDTO.getPostDstAddressObjectName()) ? addressNameList.get(0) : existObjectDTO.getPostDstAddressObjectName();
//        String dstAddressName =  StringUtils.isEmpty(existObjectDTO.getDstAddressObjectName()) ? addressNameList.get(1) : existObjectDTO.getDstAddressObjectName();

        sb.append("undo nat static outbound object-group ").append(postAddress).append(" ").append("\n");

        sb.append("quit\n");


        StringBuilder objRollbackCommandLine = new StringBuilder();
        try {
            log.info("生成对象回滚命令行的参数为:{}", JSONObject.toJSONString(generatedObjectDTO));
            List<String> addressGroupNames = generatedObjectDTO.getAddressObjectGroupNameList();
            List<String> serviceGroupNames = generatedObjectDTO.getServiceObjectGroupNameList();
            if(CollectionUtils.isEmpty(addressGroupNames) && CollectionUtils.isEmpty(serviceGroupNames)){
                policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                return policyGeneratorDTO;
            }
            // 判断是否有虚墙
            judgeIsVsy(cmdDTO, objRollbackCommandLine);

            if (CollectionUtils.isNotEmpty(addressGroupNames)) {
                for (String addressGroupName : addressGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressGroupName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectGroupCommandLine(null, null, addressGroupName, null, null));
                }
                objRollbackCommandLine.append(StringUtils.LF);

            }
            if (CollectionUtils.isNotEmpty(serviceGroupNames)) {
                for (String serviceGroupName : serviceGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceGroupName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceGroupName, null, null));
                }
                objRollbackCommandLine.append(StringUtils.LF);
            }
            objRollbackCommandLine.append("quit\n");

        } catch (Exception e) {
            log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
        }
        policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
        policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
        return policyGeneratorDTO;
    }

    /**
     * 是否有虚墙
     * @param cmdDTO
     * @param sb
     */
    private void judgeIsVsy(CmdDTO cmdDTO, StringBuilder sb) {
        sb.append("system-view\n");
        if (cmdDTO.getDevice().isVsys()) {
            sb.append("switchto context " + cmdDTO.getDevice().getVsysName() + "\n");
            sb.append("system-view\n");
        }
    }
}
