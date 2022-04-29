package com.abtnetworks.totems.common.commandline.nat;

import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.commandline.security.SecurityLegendSecV40;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 网神nat命令行生成
 * @date 2020/7/2
 */
@Slf4j
@Service(value = "LegendSecNSGNAT40")
public class LegendSecNatV40 implements NatPolicyGenerator {

    private static final String SNAT = "snat ";
    private static final String DIP = " dip ";
    private static final String EGRESS_INTERFACE = " egress-interface ";
    private static final String SERVICE = " service ";
    private static final String ADDR_OBJECT = "addr-object ";
    private static final String TOP_LF = " top\n";
    private static final String END_LF = "end\n";
    private static final String SAVE_CONFIG_LF = "save config\n";
    private static final String ENABLE = " enable ";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("Legendsec NSG nat策略");

        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    private void preCommand(StringBuilder sb, boolean isVs, String vName) {
        sb.append("config terminal\n");
        if (isVs) {
            sb.append("vsys change " + vName + "\n");
            sb.append("config terminal\n");
        }
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {

        StringBuilder sb = new StringBuilder();

        preCommand(sb, policyDTO.isVsys(), policyDTO.getVsysName());

        //调用安全策略的处理对象的方法
        SecurityLegendSecV40 securityLegendSecV40 = new SecurityLegendSecV40();
        PolicyObjectDTO srcAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getInsideAddress(), policyDTO.getTheme(), policyDTO.isCreateObject(), policyDTO.getInsideAddressName(), "inside");
        PolicyObjectDTO dstAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getGlobalAddress(), policyDTO.getTheme(), policyDTO.isCreateObject(), policyDTO.getGlobalAddressName(), "global");
        if (srcAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if (dstAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }

        String insidePort = policyDTO.getInsidePort();
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        List<String> existInsidePortName = new ArrayList<>();
        if(StringUtils.isNotEmpty(policyDTO.getExistInsidePort())){
            existInsidePortName.add(policyDTO.getExistInsidePort());
        }else {
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setProtocol(policyDTO.getProtocol());
            serviceDTO.setDstPorts(insidePort);
            serviceDTOList.add(serviceDTO);
        }
        PolicyObjectDTO policyObjectDTO = securityLegendSecV40.generateServiceObject(serviceDTOList, true, false,existInsidePortName);
        if (policyObjectDTO.isObjectFlag() && !AliStringUtils.isEmpty(policyObjectDTO.getCommandLine())) {
            sb.append(policyObjectDTO.getCommandLine());
        }
        boolean isAnyPostObject = CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin()) && CommonConstants.ANY.equalsIgnoreCase(policyDTO.getOutDevItf());
        String outDevItf = StringUtils.isEmpty(policyDTO.getOutDevItf()) ? "any" : policyDTO.getOutDevItf();

        sb.append(SNAT).append(policyDTO.getTheme() + " ");
        if (isAnyPostObject) {
            //  转换前源目的地址为any，出接口为any，转换后地址为对象
            sb.append("sip ").append(srcAddressObject.getJoin())
                    .append(DIP).append(CommonConstants.ANY).append(EGRESS_INTERFACE).append(outDevItf)
                    .append(SERVICE).append(CommonConstants.ANY).append(" static trans-ip ");
            if (!CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(dstAddressObject.getJoin()).append(" enable").append(CommonConstants.LINE_BREAK);
        } else {
            //转换前源地址/服务为对象，目的地址为any，转换后地址为对象

            sb.append("sip ");
            if (!CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(srcAddressObject.getJoin())
                    .append(DIP).append(CommonConstants.ANY).append(EGRESS_INTERFACE).append(outDevItf)
                    .append(SERVICE).append(policyObjectDTO.getJoin()).append(" static trans-ip ");
            if (!CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(dstAddressObject.getJoin()).append(" enable").append(CommonConstants.LINE_BREAK);
        }
        sb.append(SNAT).append(policyDTO.getTheme()).append(TOP_LF);
        policyDTO.setPolicyName(policyDTO.getTheme());
        sb.append(END_LF);
        sb.append(SAVE_CONFIG_LF);
        return sb.toString();
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        SecurityLegendSecV40 securityLegendSecV40 = new SecurityLegendSecV40();
        PolicyObjectDTO srcAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getSrcAddressObjectName(), "src");
        PolicyObjectDTO dstAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getDstAddressObjectName(), "dst");
        PolicyObjectDTO policyObjectDTO = securityLegendSecV40.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.isCreateObjFlag(), false, policyDTO.getExistServiceNameList());
        PolicyObjectDTO postSrcAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getPostAddressObjectName(), "post");
        StringBuilder sb = new StringBuilder();
        preCommand(sb, policyDTO.isVsys(), policyDTO.getVsysName());
        if (srcAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if (dstAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }


        if (policyObjectDTO.isObjectFlag() && !AliStringUtils.isEmpty(policyObjectDTO.getCommandLine())) {
            sb.append(policyObjectDTO.getCommandLine());
        }
        if (postSrcAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(postSrcAddressObject.getCommandLine())) {
            sb.append(postSrcAddressObject.getCommandLine());
        }

        String dstItfJoin = StringUtils.isNotBlank(policyDTO.getDstItf()) ? policyDTO.getDstItf() : "any";

        boolean isAllAny = CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin()) && CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())
                && CommonConstants.ANY.equalsIgnoreCase(policyDTO.getDstItf());
        if (isAllAny) {
            sb.append(String.format("snat %s sip %s dip %s egress-interface %s service %s dynamic type SYMMETRIC trans-ip ", policyDTO.getTheme(), srcAddressObject.getJoin(),
                    dstAddressObject.getJoin(), dstItfJoin, policyObjectDTO.getJoin()));
            if (!CommonConstants.ANY.equalsIgnoreCase(postSrcAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(postSrcAddressObject.getJoin()).append(ENABLE).append(CommonConstants.LINE_BREAK);
        } else {
            sb.append(String.format("snat %s sip ", policyDTO.getTheme()));
            if (!CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(srcAddressObject.getJoin());
            sb.append(DIP);
            if (!CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(dstAddressObject.getJoin()).append(EGRESS_INTERFACE)
                    .append(dstItfJoin);
            sb.append(SERVICE).append(policyObjectDTO.getJoin()).append(" dynamic type SYMMETRIC trans-ip ");
            if (!CommonConstants.ANY.equalsIgnoreCase(postSrcAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(postSrcAddressObject.getJoin()).append(ENABLE).append(CommonConstants.LINE_BREAK);
        }


        sb.append(SNAT).append(policyDTO.getTheme()).append(TOP_LF);
        policyDTO.setPolicyName(policyDTO.getTheme());
        sb.append(END_LF);
        sb.append(SAVE_CONFIG_LF);
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        SecurityLegendSecV40 securityLegendSecV40 = new SecurityLegendSecV40();
        PolicyObjectDTO srcAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getSrcIp(), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getSrcAddressObjectName(), "src");
        PolicyObjectDTO dstAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getDstIp(), policyDTO.getTheme(), policyDTO.isCreateObjFlag(), policyDTO.getDstAddressObjectName(), "dst");
        PolicyObjectDTO policyObjectDTO = securityLegendSecV40.generateServiceObject(policyDTO.getRestServiceList(), policyDTO.isCreateObjFlag(), false, policyDTO.getExistServiceNameList());
        PolicyObjectDTO postAddressObject = securityLegendSecV40.generateAddressObject(policyDTO.getPostIpAddress(), policyDTO.getTheme(), false,null, "post");


        StringBuilder sb = new StringBuilder();
        preCommand(sb, policyDTO.isVsys(), policyDTO.getVsysName());
        if (srcAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(srcAddressObject.getCommandLine())) {
            sb.append(srcAddressObject.getCommandLine());
        }

        if (dstAddressObject.isObjectFlag() && !AliStringUtils.isEmpty(dstAddressObject.getCommandLine())) {
            sb.append(dstAddressObject.getCommandLine());
        }


        if (policyObjectDTO.isObjectFlag() && !AliStringUtils.isEmpty(policyObjectDTO.getCommandLine())) {
            sb.append(policyObjectDTO.getCommandLine());
        }


        sb.append("object address-server ").append(postAddressObject.getJoin()).append(CommonConstants.LINE_BREAK);
        sb.append("ip ").append(policyDTO.getPostIpAddress()).append(CommonConstants.LINE_BREAK);
        sb.append("exit\n");
        sb.append("dnat ");
        sb.append(policyDTO.getTheme() + " ");
        String srcItfJoin = StringUtils.isNotBlank(policyDTO.getSrcItf()) ? policyDTO.getSrcItf() : "any";
        boolean isAllAny = CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin()) && CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())
                && CommonConstants.ANY.equalsIgnoreCase(policyDTO.getDstItf()) && postAddressObject.isObjectFlag();

        if (isAllAny) {
            //转换前源目的地址/服务为any，出接口为any，转换后地址为服务器对象，转换后端口必填
            sb.append(" sip ").append(srcAddressObject.getJoin());
            sb.append(DIP).append(dstAddressObject.getJoin()).append(" ingress-interface ")
                    .append(srcItfJoin);
        } else {

            //DNAT特有的服务器对象，注意此对象不要去复用，是特殊的对象
            sb.append(" sip ");
            if (!CommonConstants.ANY.equalsIgnoreCase(srcAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(srcAddressObject.getJoin());
            sb.append(DIP);
            if (!CommonConstants.ANY.equalsIgnoreCase(dstAddressObject.getJoin())) {
                sb.append(ADDR_OBJECT);
            }
            sb.append(dstAddressObject.getJoin()).append(" ingress-interface ")
                    .append(srcItfJoin);
        }

        String postPort = policyDTO.getPostPort();
        if("any".equals(postPort)||StringUtils.isEmpty(postPort)){
            postPort = "no-trans";
        }
        if (postPort.contains(PolicyConstants.ADDRESS_SEPERATOR)) {
            String[] split = postPort.split(PolicyConstants.ADDRESS_SEPERATOR);
            postPort = split[0];
        }
        sb.append(SERVICE).append(policyObjectDTO.getJoin()).append(" trans-ip server-object ").append(postAddressObject.getJoin())
                .append(" trans-port ").append(postPort).append(ENABLE).append(CommonConstants.LINE_BREAK);
        sb.append("dnat ").append(policyDTO.getTheme()).append(TOP_LF);
        policyDTO.setPolicyName(policyDTO.getTheme());
        sb.append(END_LF);
        sb.append(SAVE_CONFIG_LF);
        return sb.toString();
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }


}
