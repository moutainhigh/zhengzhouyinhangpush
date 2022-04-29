package com.abtnetworks.totems.common.commandline.rollback;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.common.dto.PolicyGeneratorDTO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.GeneratedObjectDTO;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoASA99Impl;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author luwei
 * @date 2020/7/21
 */
@Slf4j
@Service
public class RollbackSecurityCiscoASA implements PolicyGenerator {

    protected OverAllGeneratorAbstractBean generatorBean;


    public RollbackSecurityCiscoASA(){
        generatorBean = new SecurityCiscoASA99Impl();
    }


    @Override
    public String generate(CmdDTO cmdDTO) {
        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        String securityCommandLine = generatedDto.getPolicyName();
        if (StringUtils.isBlank(securityCommandLine)) {
            log.error("参数错误，无法生成思科安全策略回滚命令行");
            return "";
        } else if (!securityCommandLine.contains("access-list")) {
            log.error("命令行没有关键字access-list");
            return "";
        } else {
            //截取
            StringBuffer sb = new StringBuffer();
            sb.append("configure terminal\n");

            StringBuffer idelTimeRollbackLine = new StringBuffer();
            StringBuffer policyRollbackLine = new StringBuffer();

            String[] lineArr = securityCommandLine.split("\n");
            for (String oneLine : lineArr) {
                if (oneLine.length() == 0) {
                    continue;
                }

                if(oneLine.startsWith("class-map")){
                    idelTimeRollbackLine.append(String.format("%s%s",oneLine.replaceAll("class-map", "no class-map"),StringUtils.LF));
                }

                if(oneLine.startsWith("policy-map")){
                    idelTimeRollbackLine.insert(0,String.format("%s%s",oneLine.replaceAll("policy-map", "no policy-map"),StringUtils.LF));
                }

                if (!oneLine.startsWith("access-list")) {
                    continue;
                }

                oneLine = oneLine.replaceAll("access-list", "no access-list");
                //回滚命令行中不能有line 1 行号信息
                int lineIndex = oneLine.indexOf("line");
                int extended = oneLine.indexOf("extended");
                if (lineIndex != -1 && extended != -1) {
                    String lineStr = oneLine.substring(lineIndex, extended);
                    oneLine = oneLine.replace(lineStr, "");
                }
                policyRollbackLine.append(oneLine).append(StringUtils.LF);
            }
            sb.append(idelTimeRollbackLine.toString());
            sb.append(policyRollbackLine.toString());
            sb.append("\n");
            sb.append("end\n");
            return sb.toString();
        }

    }


    @Override
    public PolicyGeneratorDTO generateV2(CmdDTO cmdDTO) {

        PolicyGeneratorDTO policyGeneratorDTO = new PolicyGeneratorDTO();

        GeneratedObjectDTO generatedDto = cmdDTO.getGeneratedObject();
        String securityCommandLine = generatedDto.getPolicyName();
        if (StringUtils.isBlank(securityCommandLine)) {
            log.error("参数错误，无法生成思科安全策略回滚命令行");
            return policyGeneratorDTO;
        } else if (!securityCommandLine.contains("access-list")) {
            log.error("命令行没有关键字access-list");
            return policyGeneratorDTO;
        } else {
            //截取
            StringBuffer sb = new StringBuffer();
            sb.append("configure terminal\n");

            StringBuffer idelTimeRollbackLine = new StringBuffer();
            StringBuffer policyRollbackLine = new StringBuffer();

            String[] lineArr = securityCommandLine.split("\n");
            for (String oneLine : lineArr) {
                if (oneLine.length() == 0) {
                    continue;
                }

                if(oneLine.startsWith("class-map")){
                    idelTimeRollbackLine.append(String.format("%s%s",oneLine.replaceAll("class-map", "no class-map"),StringUtils.LF));
                }

                if(oneLine.startsWith("policy-map")){
                    idelTimeRollbackLine.insert(0,String.format("%s%s",oneLine.replaceAll("policy-map", "no policy-map"),StringUtils.LF));
                }

                if (!oneLine.startsWith("access-list")) {
                    continue;
                }

                oneLine = oneLine.replaceAll("access-list", "no access-list");
                //回滚命令行中不能有line 1 行号信息
                int lineIndex = oneLine.indexOf("line");
                int extended = oneLine.indexOf("extended");
                if (lineIndex != -1 && extended != -1) {
                    String lineStr = oneLine.substring(lineIndex, extended);
                    oneLine = oneLine.replace(lineStr, "");
                }
                policyRollbackLine.append(oneLine).append(StringUtils.LF);

            }
            sb.append(idelTimeRollbackLine.toString());
            sb.append(policyRollbackLine.toString());
            sb.append("\n");
            sb.append("end\n");


            StringBuilder objRollbackCommandLine = new StringBuilder();
            try {
                log.info("生成对象回滚命令行 参数为:{}", JSONObject.toJSONString(generatedDto));
                List<String> addressGroupNames = generatedDto.getAddressObjectGroupNameList();
                List<String> addressNames = generatedDto.getAddressObjectNameList();
                List<String> serviceGroupNames = generatedDto.getServiceObjectGroupNameList();
                List<String> serviceNames = generatedDto.getServiceObjectNameList();
                List<String> timeNames = generatedDto.getTimeObjectNameList();

                // 如果新建对象都为空 则不创建对象回滚命令行
                if (CollectionUtils.isEmpty(addressGroupNames) && CollectionUtils.isEmpty(addressNames) && CollectionUtils.isEmpty(serviceGroupNames)
                        && CollectionUtils.isEmpty(serviceNames) && CollectionUtils.isEmpty(timeNames)) {
                    policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
                    return policyGeneratorDTO;
                }

                objRollbackCommandLine.append("configure terminal\n");
                // 地址组对象拼接对象回滚命令行
                if (CollectionUtils.isNotEmpty(addressGroupNames)) {
                    objRollbackCommandLine.append(StringUtils.LF);
                    for (String addressName : addressGroupNames) {
                        // 拼接对象回滚cmd
                        if (StringUtils.isBlank(addressName)) {
                            continue;
                        }
                        objRollbackCommandLine.append(generatorBean.deleteIpAddressObjectGroupCommandLine(null, null, addressName, null, null));
                    }
                }

                // 地址对象拼接对象回滚命令行
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

                // 服务组对象拼接对象回滚命令行
                if (CollectionUtils.isNotEmpty(serviceGroupNames)) {
                    objRollbackCommandLine.append(StringUtils.LF);
                    for (String serviceName : serviceGroupNames) {
                        // 拼接对象回滚cmd
                        if (StringUtils.isBlank(serviceName)) {
                            continue;
                        }
                        objRollbackCommandLine.append(generatorBean.deleteServiceObjectGroupCommandLine(null, null, serviceName, null, null));
                    }
                }

                // 服务对象拼接对象回滚命令行
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

                // 时间对象拼接对象回滚命令行
                if (CollectionUtils.isNotEmpty(timeNames)) {
                    objRollbackCommandLine.append(StringUtils.LF);
                    for (String timeName : timeNames) {
                        // 拼接对象回滚cmd
                        if (StringUtils.isBlank(timeName)) {
                            continue;
                        }
                        objRollbackCommandLine.append(generatorBean.deleteAbsoluteTimeCommandLine(timeName, null, null));
                    }
                }
                objRollbackCommandLine.append("end\n");
                objRollbackCommandLine.append("write\n");
            } catch (Exception e) {
                log.error("调用原子化命令行拼接命令行失败,失败原因:{}", e);
            }
            policyGeneratorDTO.setPolicyRollbackCommandLine(sb.toString());
            policyGeneratorDTO.setObjectRollbackCommandLine(objRollbackCommandLine.toString());
            return policyGeneratorDTO;
        }

    }


    public static void main(String[] args) {
        String str = "configure terminal\n" +
                "object network sk204_AO_8797\n" +
                "host 3.2.1.1\n" +
                "\n" +
                "\n" +
                "object network sk204_AO_1879\n" +
                "host 3.2.1.2\n" +
                "\n" +
                "\n" +
                "object-group service tcp_231 \n" +
                "service-object tcp destination eq 231 \n" +
                "exit\n" +
                "\n" +
                "access-list sk204  extended permit object-group tcp_231  object sk204_AO_8797 object sk204_AO_1879\n" +
                "exit\n" +
                "\n" +
                "end\n" +
                "write\n";

        CmdDTO cmdDTO = new CmdDTO();
        cmdDTO.getGeneratedObject().setPolicyName(str);

        RollbackSecurityCiscoASA ciscoASA = new RollbackSecurityCiscoASA();
        System.out.println(ciscoASA.generate(cmdDTO));
    }

}
