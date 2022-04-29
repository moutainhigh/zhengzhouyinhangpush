package com.abtnetworks.totems.common.atomcommandline.base;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 抽出山石在生成命令行公共部分的业务
 * @date 2021/7/30
 */
@Slf4j
public class BaseHillStoneCommonBuss {


    /**
     * 山石对象回滚公共的类
     * @param sb
     * @param generatorBean
     * @param cmdDTO
     */
    public void rollbackHillStoneObject(StringBuilder sb, OverAllGeneratorAbstractBean generatorBean, CmdDTO cmdDTO){

        try {
            // 地址组对象拼接命令行
            GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
            DeviceDTO deviceDTO =  cmdDTO.getDevice();
            List<String> addressGroupNames = generatedObjectDTO.getAddressObjectGroupNameList();
            if (CollectionUtils.isNotEmpty(addressGroupNames)) {
                sb.append(StringUtils.LF);
                sb.append(
                        generatorBean.generatePreCommandline(deviceDTO.isVsys(), deviceDTO.getVsysName(), null, null));
                for (String addressName : addressGroupNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    sb.append(generatorBean.deleteIpAddressObjectGroupCommandLine(null, null, addressName, null, null));
                }
                sb.append(generatorBean.generatePostCommandline(null, null));
            }

            // 地址对象拼接命令行
            List<String> addressNames = generatedObjectDTO.getAddressObjectNameList();
            if (CollectionUtils.isNotEmpty(addressNames)) {
                sb.append(StringUtils.LF);
                sb.append(generatorBean.generatePreCommandline(cmdDTO.getDevice().isVsys(),
                        cmdDTO.getDevice().getVsysName(), null, null));
                for (String addressName : addressNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(addressName)) {
                        continue;
                    }
                    sb.append(generatorBean.deleteIpAddressObjectCommandLine(null, null, addressName, null, null));
                }
                sb.append(generatorBean.generatePostCommandline(null, null));
            }

            // 服务组对象拼接命令行
            List<String> serviceGroupNames = generatedObjectDTO.getServiceObjectGroupNameList();
            commonDeleteObject( serviceGroupNames, generatorBean, sb, cmdDTO);

            // 服务对象拼接命令行
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
            commonDeleteObject( serviceNames, generatorBean, sb, cmdDTO);

            // 时间对象拼接命令行
            List<String> timeNames = generatedObjectDTO.getTimeObjectNameList();
            deleteTimeObject( timeNames, generatorBean, sb, cmdDTO);


        } catch (Exception e) {
            log.error("原子化命令行创建回滚对象命令行异常,异常原因:{}", e);
        }
    }

    /**
     *
     * @param serviceNames
     * @param generatorBean
     * @param sb
     * @param cmdDTO
     * @throws Exception
     */
    private void commonDeleteObject(List<String> serviceNames,OverAllGeneratorAbstractBean generatorBean,StringBuilder sb,CmdDTO cmdDTO) throws Exception {
        if (CollectionUtils.isNotEmpty(serviceNames)) {
            sb.append(StringUtils.LF);
            sb.append(generatorBean.generatePreCommandline(cmdDTO.getDevice().isVsys(),
                    cmdDTO.getDevice().getVsysName(), null, null));
            for (String serviceName : serviceNames) {
                // 拼接对象回滚cmd
                if (StringUtils.isBlank(serviceName)) {
                    continue;
                }
                sb.append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
            }
            sb.append(generatorBean.generatePostCommandline(null, null));
        }
    }

    /**
     *
     * @param timeNames
     * @param generatorBean
     * @param sb
     * @param cmdDTO
     * @throws Exception
     */
    private void deleteTimeObject(List<String> timeNames,OverAllGeneratorAbstractBean generatorBean,StringBuilder sb,CmdDTO cmdDTO) throws Exception {
        if (CollectionUtils.isNotEmpty(timeNames)) {
            sb.append(StringUtils.LF);
            sb.append(generatorBean.generatePreCommandline(cmdDTO.getDevice().isVsys(),
                    cmdDTO.getDevice().getVsysName(), null, null));
            for (String timeName : timeNames) {
                // 拼接对象回滚cmd
                if (StringUtils.isBlank(timeName)) {
                    continue;
                }
                sb.append(generatorBean.deleteAbsoluteTimeCommandLine(timeName, null, null));
            }
            sb.append(generatorBean.generatePostCommandline(null, null));
        }
    }


}
