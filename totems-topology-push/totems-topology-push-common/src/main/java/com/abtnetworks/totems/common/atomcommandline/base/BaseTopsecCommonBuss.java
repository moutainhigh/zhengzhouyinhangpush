package com.abtnetworks.totems.common.atomcommandline.base;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author lifei
 * @desc 抽出天融信在生成命令行公共部分的业务
 * @date 2021/10/19 10:23
 */
@Slf4j
public class BaseTopsecCommonBuss {


    /**
     * 山石对象回滚公共的类
     * @param objRollbackCommandLine
     * @param generatorBean
     * @param cmdDTO
     */
    public void rollbackTopsecObject(StringBuilder objRollbackCommandLine, OverAllGeneratorAbstractBean generatorBean, CmdDTO cmdDTO){
        try {
            // 地址组对象拼接命令行
            GeneratedObjectDTO generatedObjectDTO = cmdDTO.getGeneratedObject();
            DeviceDTO deviceDTO =  cmdDTO.getDevice();

            Map<String,String> addressMap = generatedObjectDTO.getAddressTypeMap();
            List<String> serviceNames = generatedObjectDTO.getServiceObjectNameList();
            List<String> timeObjectNameList = generatedObjectDTO.getTimeObjectNameList();

            // 在天融信设备上测试过，不用重新进入试图 就能执行删除对象命令行操作
            RuleIPTypeEnum ipTypeEnum = null;
            if (IpTypeEnum.IPV6.getCode().equals(generatedObjectDTO.getIpType())) {
                ipTypeEnum = RuleIPTypeEnum.IP6;
            } else {
                ipTypeEnum = RuleIPTypeEnum.IP4;
            }

            // 地址对象拼接对象回滚命令行
            if(!addressMap.isEmpty()){
                for (String key :addressMap.keySet()){
                    objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectCommandLine(ipTypeEnum,addressMap.get(key),key,null,null));
                }
                objRollbackCommandLine.append(StringUtils.LF);
            }

            // 服务对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(serviceNames)) {
                for (String serviceName : serviceNames) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(serviceName)) {
                        continue;
                    }
                    objRollbackCommandLine
                            .append(generatorBean.deleteServiceObjectCommandLine(null, null, serviceName, null, null));
                }
                objRollbackCommandLine.append(StringUtils.LF);
            }


            // 时间对象拼接对象回滚命令行
            if (CollectionUtils.isNotEmpty(timeObjectNameList)) {
                for (String timeName : timeObjectNameList) {
                    // 拼接对象回滚cmd
                    if (StringUtils.isBlank(timeName)) {
                        continue;
                    }
                    objRollbackCommandLine.append(generatorBean.deleteAbsoluteTimeCommandLine(timeName, null, null));
                }
                objRollbackCommandLine.append(StringUtils.LF);
            }
            objRollbackCommandLine.append("end\n");

        } catch (Exception e) {
            log.error("原子化命令行创建回滚对象命令行异常,异常原因:{}", e);
        }
    }

}
