package com.abtnetworks.totems.fortinet;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.*;
import com.abtnetworks.totems.vender.fortinet.security.NatFortinetImpl;
import com.abtnetworks.totems.vender.fortinet.security.SecurityFortinetImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/21
 */
public class CommandLineFortinetTest {

    @Test
    public void testFortinetCommandLine() throws Exception {
        PolicyParamDTO paramDTO = new PolicyParamDTO();
        paramDTO.setAction("permit");
        paramDTO.setDescription("飞塔命令行生成");
      /*  ZoneParamDTO srcZoneParamDTO=new ZoneParamDTO();
        srcZoneParamDTO.setName("trust");
        ZoneParamDTO dstZoneParamDTO=new ZoneParamDTO();
        dstZoneParamDTO.setName("untrust");*/
        paramDTO.setId("666");
        paramDTO.setSrcZone(new ZoneParamDTO("trust"));
        paramDTO.setDstZone(new ZoneParamDTO("untrust"));
        paramDTO.setMoveSeatEnum(MoveSeatEnum.AFTER);
        paramDTO.setSwapRuleNameId("77");

        AbsoluteTimeParamDTO absoluteTimeParamDTO = new AbsoluteTimeParamDTO("2021-04-27","10:22:00","2021-05-01","23:59:59");
        paramDTO.setAbsoluteTimeParamDTO(absoluteTimeParamDTO);

        //SecurityFortinetImpl securityFortinet = new SecurityFortinetImpl();

        IpAddressRangeDTO ipAddressRangeDTO1 = new IpAddressRangeDTO();
        ipAddressRangeDTO1.setIpTypeEnum(RuleIPTypeEnum.IP4);
        ipAddressRangeDTO1.setStart("192.169.1.1");
        ipAddressRangeDTO1.setEnd("192.168.1.24");


        IpAddressParamDTO srcIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new IpAddressRangeDTO[]{ipAddressRangeDTO1});
        srcIp.setName("address1");
        paramDTO.setSrcIp(srcIp);

        IpAddressParamDTO dstIp = new IpAddressParamDTO();
        dstIp.setSingleIpArray(new String[]{"192.168.1.0"});
        dstIp.setIpTypeEnum(RuleIPTypeEnum.IP4);
        dstIp.setName("addressA");
        paramDTO.setDstIp(dstIp);

        ServiceParamDTO tcpService = new ServiceParamDTO();
        tcpService.setProtocol(ProtocolTypeEnum.TCP);
        tcpService.setSrcSinglePortArray(new Integer[]{5000});
        tcpService.setDstSinglePortArray(new Integer[]{80});

        ServiceParamDTO udpService = new ServiceParamDTO();
        udpService.setProtocol(ProtocolTypeEnum.UDP);
        udpService.setSrcRangePortArray(new PortRangeDTO[]{new PortRangeDTO(3306,3380)});
        udpService.setDstRangePortArray(new PortRangeDTO[]{new PortRangeDTO(10086,10090)});

        paramDTO.setServiceParam(new ServiceParamDTO[]{tcpService,udpService});
        paramDTO.setSrcRefIpAddressObject(new String[]{"address1"});
        paramDTO.setDstRefIpAddressObject(new String[]{"addressA"});

        SecurityFortinetImpl securityFortinet = new SecurityFortinetImpl();
        System.out.println("飞塔命令行------------------》");
        Map  attr=new HashMap();
        attr.put("theme","测试工单");
        System.out.println(securityFortinet.generateSecurityPolicyCommandLine(StatusTypeEnum.ADD,paramDTO,attr,null));
    }

    @Test
    public void testFortinetSNAT() throws Exception {
        //NatHillStoneR5Impl securityHillStoneR5 = new NatHillStoneR5Impl();
        System.out.println("飞塔SNAT策略命令行------------------》");
        NatPolicyParamDTO paramDTO = new NatPolicyParamDTO();
        paramDTO.setDescription("飞塔SNAT策略命令行");
        IpAddressRangeDTO ipAddressRangeDTO1 = new IpAddressRangeDTO();
        ipAddressRangeDTO1.setIpTypeEnum(RuleIPTypeEnum.IP4);
        ipAddressRangeDTO1.setStart("192.169.1.1");
        ipAddressRangeDTO1.setEnd("192.168.1.24");

        IpAddressRangeDTO ipAddressRangeDTO2 = new IpAddressRangeDTO();
        ipAddressRangeDTO2.setIpTypeEnum(RuleIPTypeEnum.IP4);
        ipAddressRangeDTO2.setStart("192.169.12.48");
        ipAddressRangeDTO2.setEnd("192.168.12.56");
        paramDTO.setSrcZone(new ZoneParamDTO("trust"));
        paramDTO.setDstZone(new ZoneParamDTO("untrust"));
//        IpAddressParamDTO srcIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new IpAddressRangeDTO[]{ipAddressRangeDTO1,ipAddressRangeDTO2});
        IpAddressParamDTO srcIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new String[]{"10.215.34.45"});
        paramDTO.setSrcIp(srcIp);

        IpAddressParamDTO postSrc= new IpAddressParamDTO(RuleIPTypeEnum.IP4, new String[]{"10.215.34.77"});
        paramDTO.setPostSrcIpAddress(postSrc);

        IpAddressParamDTO dstIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new String[]{"12.0.1.1"});
        paramDTO.setSrcIp(dstIp);

        IpAddressRangeDTO dstIpAddressRangeDTO = new IpAddressRangeDTO();
        dstIpAddressRangeDTO.setIpTypeEnum(RuleIPTypeEnum.IP4);
        dstIpAddressRangeDTO.setStart("192.169.251.12");
        dstIpAddressRangeDTO.setEnd("192.169.251.46");
        //IpAddressParamDTO dstIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new String[]{"10.12.111.33","10.12.109.234"});
        IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
        ipAddressSubnetIntDTO.setIp("192.168.9.45");
        ipAddressSubnetIntDTO.setMask(32);
        ipAddressSubnetIntDTO.setType(MaskTypeEnum.mask);
        dstIp.setSubnetIntIpArray(new IpAddressSubnetIntDTO[]{ipAddressSubnetIntDTO});
        dstIp.setIpTypeEnum(RuleIPTypeEnum.IP4);
        paramDTO.setDstIp(dstIp);

        ServiceParamDTO tcpService = new ServiceParamDTO();
        tcpService.setProtocol(ProtocolTypeEnum.TCP);
        tcpService.setSrcSinglePortArray(new Integer[]{5000});
        tcpService.setDstSinglePortArray(new Integer[]{80});

        ServiceParamDTO udpService = new ServiceParamDTO();
        udpService.setProtocol(ProtocolTypeEnum.UDP);
        udpService.setSrcRangePortArray(new PortRangeDTO[]{new PortRangeDTO(3306,3380)});
        udpService.setDstRangePortArray(new PortRangeDTO[]{new PortRangeDTO(10086,10090)});

        paramDTO.setServiceParam(new ServiceParamDTO[]{tcpService,udpService});
//        paramDTO.setSrcRefIpAddressObject(new String[]{"address1","address2"});
//        paramDTO.setSrcRefIpAddressObjectGroup(new String[]{"addressGroup1","addressGropu2"});

       /* paramDTO.setDstRefIpAddressObject(new String[]{"addressA","addressB"});
        paramDTO.setDstRefIpAddressObjectGroup(new String[]{"addressGroupA","addressGropuB"});

        paramDTO.setRefServiceObject(new String[]{"service1","service2"});
        paramDTO.setRefServiceObjectGroup(new String[]{"serviceGroup1","serviceGroup2"});*/
        IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
        ipAddressParamDTO.setSingleIpArray(new String[]{"2.2.2.2"});
//        IpAddressRangeDTO ipAddressRangeDTO = new IpAddressRangeDTO();
//        ipAddressRangeDTO.setStart("192.168.2.3");
//        ipAddressRangeDTO.setEnd("192.168.2.6");
//        ipAddressParamDTO.setRangIpArray(new IpAddressRangeDTO[]{ipAddressRangeDTO});
        paramDTO.setPostSrcIpAddress(ipAddressParamDTO);
        NatFortinetImpl natFortinet=new NatFortinetImpl();
        System.out.println("飞塔命令行------------------》");
        Map  attr=new HashMap();
        attr.put("theme","测试工单");
        System.out.println(natFortinet.generateSNatPolicyCommandLine(StatusTypeEnum.ADD,paramDTO,attr,null));
        //System.out.println(securityHillStoneR5.generateSNatPolicyCommandLine(StatusTypeEnum.ADD,paramDTO,null,null));
    }


}
