package com.abtnetworks.totems.common.atomcommandline.nat;

import com.abtnetworks.totems.common.atomcommandline.bothnat.AtomBothNatF5;
import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.*;
import com.abtnetworks.totems.common.enums.ApplyTypeEnum;
import com.abtnetworks.totems.common.enums.BalanceTypeEnum;
import com.abtnetworks.totems.common.enums.ProtocolEnum;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.script.TotemsScriptUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.abtnetworks.totems.common.enums.ApplyTypeEnum.*;

/**
 * @author lifei
 * @desc F5 nat命令行生成执行器
 * @date 2021/8/3 13:51
 */
@Log4j2
@Service(value = "Atom F5 NAT")
public class AtomF5 implements NatPolicyGenerator {

    public static final String DISABLED = "disabled";
    public static final String AUTOMAP = "automap";
    public static final String NONE = "None";

    @Override
    public String generate(CmdDTO cmdDTO) {
        log.info("generate F5 nat策略");
        return NatPolicyGenerator.super.generate(cmdDTO);
    }

    @Override
    public String generateF5DNatCommandLine(F5DNatPolicyDTO policyDTO) {
        log.info("F5的目的nat的命令行的入参：{}", JSONObject.toJSONString(policyDTO));
        StringBuilder sb = new StringBuilder();
        sb.append("tmsh").append(StringUtils.LF);

        if (policyDTO.isVsys()){
            sb.append(String.format("cd /%s",policyDTO.getVsysName())).append(StringUtils.LF);
        }

        sb.append(String.format("create ltm pool %s load-balancing-mode %s ",policyDTO.getPoolInfo().getName(),BalanceTypeEnum.getNameByKey(policyDTO.getLoadBlanaceMode())));
        // create ltm pool pool33 members add {5.4.6.3:80} load-balancing-mode least-sessions  monitor http and gateway_icmp

        if (!policyDTO.getPoolInfo().getGroupPriorityType().equalsIgnoreCase(DISABLED)){
            sb.append(String.format("min-active-members %d ",policyDTO.getPoolInfo().getGroupPriorityLevel()));
        }

        sb.append("members add {");

        if (policyDTO.getPoolInfo().getGroupPriorityType().equalsIgnoreCase(DISABLED)){
            for (PoolDetailInfo poolDetailInfo : policyDTO.getPoolInfo().getPoolDetailInfos()) {
                sb.append(String.format("%s:%s {address %s} ",poolDetailInfo.getIp(),poolDetailInfo.getPort(),poolDetailInfo.getIp()));
            }
        }else {
            for (PoolDetailInfo poolDetailInfo : policyDTO.getPoolInfo().getPoolDetailInfos()) {
                sb.append(String.format("%s:%s {address %s priority-group %s} ",poolDetailInfo.getIp(),poolDetailInfo.getPort(),poolDetailInfo.getIp(),poolDetailInfo.getPriorityLevel()));
            }
        }
        sb = new StringBuilder(sb.substring(0,sb.length() - 1));
        sb.append("} ");

        if (StringUtils.isNotEmpty(policyDTO.getMonitor())){
            sb.append("monitor ");
            String[] monitors = policyDTO.getMonitor().split(",");
            int index = 0;
            for (String monitor : monitors) {
                if (index > 0){
                    sb.append(String.format("and %s ",monitor));
                }else {
                    sb.append(String.format("%s ",monitor));
                }
                index++;
            }
        }

        sb.append(StringUtils.LF);
        sb.append(String.format("create ltm virtual %s ",policyDTO.getTheme()));

        if (StringUtils.isNotEmpty(policyDTO.getSrcIp())){
            if (policyDTO.getSrcIp().contains("/")){
                sb.append(String.format("source %s ",policyDTO.getSrcIp()));
            }else {
                sb.append(String.format("source %s/32 ",policyDTO.getSrcIp()));
            }
        }

        String protocol = ProtocolEnum.getDescByCode(policyDTO.getServiceList().get(0).getProtocol());
        sb.append(String.format("destination %s:%s ip-protocol %s ",policyDTO.getDstIp(),StringUtils.isEmpty(policyDTO.getServiceList().get(0).getDstPorts()) ? "any" : policyDTO.getServiceList().get(0).getDstPorts(),
                null == protocol ? "any" : protocol.toLowerCase()));

        if (!policyDTO.getPersist().equalsIgnoreCase(NONE) && StringUtils.isNotEmpty(policyDTO.getPersist())){
            sb.append(String.format("persist replace-all-with {%s} ",policyDTO.getPersist()));
        }

        sb.append(String.format("pool %s ",policyDTO.getPoolInfo().getName()));
        if (policyDTO.getSnatType().equalsIgnoreCase(AUTOMAP)){
            sb.append("source-address-translation {type automap} ");
        }
        sb.append(String.format("profiles replace-all-with {%s ",ApplyTypeEnum.getNameByKey(policyDTO.getApplyType())));

        if (StringUtils.isNotEmpty(policyDTO.getHttpProfile())){
            sb.append(String.format("%s ",policyDTO.getHttpProfile()));
        }

        if (StringUtils.isNotEmpty(policyDTO.getSslProfile())){
            String[] sslProfiles = policyDTO.getSslProfile().split(",");
            for (String sslProfile : sslProfiles) {
                sb.append(String.format("%s ",sslProfile));
            }
        }

        sb = new StringBuilder(sb.substring(0,sb.length() - 1));
        sb.append("}").append(StringUtils.LF);
        sb.append("save sys config").append(StringUtils.LF);
        sb.append("quit").append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateF5BothNatCommandLine(F5BothNatPolicyDTO policyDTO) {
        log.info("F5的BothNat的命令行的入参：{}", JSONObject.toJSONString(policyDTO));
        StringBuilder sb = new StringBuilder();
        sb.append("tmsh").append(StringUtils.LF);

        if (policyDTO.isVsys()){
            sb.append(String.format("cd /%s",policyDTO.getVsysName())).append(StringUtils.LF);
        }

        sb.append(String.format("create ltm pool %s load-balancing-mode %s ",policyDTO.getPoolInfo().getName(),BalanceTypeEnum.getNameByKey(policyDTO.getLoadBlanaceMode())));
        // create ltm pool pool33 members add {5.4.6.3:80} load-balancing-mode least-sessions  monitor http and gateway_icmp

        if (!policyDTO.getPoolInfo().getGroupPriorityType().equalsIgnoreCase(DISABLED)){
            sb.append(String.format("min-active-members %d ",policyDTO.getPoolInfo().getGroupPriorityLevel()));
        }

        sb.append("members add {");
        if (policyDTO.getPoolInfo().getGroupPriorityType().equalsIgnoreCase(DISABLED)){
            for (PoolDetailInfo poolDetailInfo : policyDTO.getPoolInfo().getPoolDetailInfos()) {
                sb.append(String.format("%s:%s {address %s} ",poolDetailInfo.getIp(),poolDetailInfo.getPort(),poolDetailInfo.getIp()));
            }
        }else {
            for (PoolDetailInfo poolDetailInfo : policyDTO.getPoolInfo().getPoolDetailInfos()) {
                sb.append(String.format("%s:%s {address %s priority-group %s} ",poolDetailInfo.getIp(),poolDetailInfo.getPort(),poolDetailInfo.getIp(),poolDetailInfo.getPriorityLevel()));
            }
        }

        sb = new StringBuilder(sb.substring(0,sb.length() - 1));
        sb.append("} ");


        if (StringUtils.isNotEmpty(policyDTO.getMonitor())){
            sb.append("monitor ");
            String[] monitors = policyDTO.getMonitor().split(",");
            int index = 0;
            for (String monitor : monitors) {
                if (index > 0){
                    sb.append(String.format("and %s ",monitor));
                }else {
                    sb.append(String.format("%s ",monitor));
                }
                index++;
            }
        }

        sb.append(StringUtils.LF);
        //create ltm snatpool A4-CTVPN-1114-ROUTE members add {192.168.1.6 4.4.4.1}

        String existPostSrcName = policyDTO.getPostSrcAddressObjectName();
        if (StringUtils.isNotBlank(existPostSrcName)) {
            // 重新设置snatPool的名称为墙上对象的名称
            policyDTO.getSnatPoolInfo().setName(existPostSrcName);
        } else {
            if (!policyDTO.getSnatPoolInfo().isQuote()) {
                sb.append(String.format("create ltm snatpool %s ", policyDTO.getSnatPoolInfo().getName()));

                if (!policyDTO.getSnatPoolInfo().isQuote()) {
                    sb.append("members add {");
                    String[] split = policyDTO.getSnatPoolInfo().getSnatPoolIp().split(",");
                    for (String s : split) {
                        sb.append(String.format("%s ", s));
                    }
                    sb = new StringBuilder(sb.substring(0, sb.length() - 1));
                    sb.append("}");
                }
                sb.append(StringUtils.LF);
            }
        }

        sb.append(String.format("create ltm virtual %s ",policyDTO.getTheme()));

        if (StringUtils.isNotEmpty(policyDTO.getSrcIp())){
            if (policyDTO.getSrcIp().contains("/")){
                sb.append(String.format("source %s ",policyDTO.getSrcIp()));
            }else {
                sb.append(String.format("source %s/32 ",policyDTO.getSrcIp()));
            }
        }

        String protocol =ProtocolEnum.getDescByCode(policyDTO.getServiceList().get(0).getProtocol());

        sb.append(String.format("destination %s:%s ip-protocol %s ",policyDTO.getDstIp(),StringUtils.isEmpty(policyDTO.getServiceList().get(0).getDstPorts()) ? "any" : policyDTO.getServiceList().get(0).getDstPorts(),
                null == protocol ? "any" : protocol.toLowerCase()));

        if (!policyDTO.getPersist().equalsIgnoreCase(NONE) && StringUtils.isNotEmpty(policyDTO.getPersist())){
            sb.append(String.format("persist replace-all-with {%s} ",policyDTO.getPersist()));
        }

        sb.append(String.format("pool %s ",policyDTO.getPoolInfo().getName()));

        sb.append(String.format("snatpool %s profiles replace-all-with {%s ",policyDTO.getSnatPoolInfo().getName(),ApplyTypeEnum.getNameByKey(policyDTO.getApplyType())));

        if (StringUtils.isNotEmpty(policyDTO.getHttpProfile())){
            sb.append(String.format("%s ",policyDTO.getHttpProfile()));
        }

        if (StringUtils.isNotEmpty(policyDTO.getSslProfile())){
            String[] sslProfiles = policyDTO.getSslProfile().split(",");
            for (String sslProfile : sslProfiles) {
                sb.append(String.format("%s ",sslProfile));
            }
        }
        sb = new StringBuilder(sb.substring(0,sb.length() - 1));
        sb.append("}").append(StringUtils.LF);
        sb.append("save sys config").append(StringUtils.LF);
        sb.append("quit").append(StringUtils.LF);
        return sb.toString();
    }

    @Override
    public String generateDNatCommandLine(DNatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateBothNatCommandLine(NatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateStaticNatCommandLine(StaticNatTaskDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    @Override
    public String generateSNatCommandLine(SNatPolicyDTO policyDTO) {
        return DO_NOT_SUPPORT;
    }

    public static void main(String[] args) {
        F5DNatPolicyDTO f5DNatPolicyDTO = new F5DNatPolicyDTO();
        f5DNatPolicyDTO.setSrcIp("0.0.0.0/0");
        f5DNatPolicyDTO.setDstIp("10.10.1.254");

    }

}
