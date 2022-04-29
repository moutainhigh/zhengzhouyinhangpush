package com.abtnetworks.totems.dptech;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.vender.dptech.r003.SecurityDp003Impl;
import com.abtnetworks.totems.command.line.enums.MoveSeatEnum;
import org.junit.Test;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/21
 */
public class CommandLineDpTechTest {

    @Test
    public void testDpTechCommandLine() throws Exception {
        PolicyParamDTO paramDTO = new PolicyParamDTO();
        paramDTO.setAction("permit");
        paramDTO.setDescription("山石命令行生成");
        paramDTO.setSrcZone(new ZoneParamDTO("trust"));
        paramDTO.setDstZone(new ZoneParamDTO("untrust"));
        paramDTO.setId("666");
        paramDTO.setName("newHillStonePolicy");
        paramDTO.setMoveSeatEnum(MoveSeatEnum.LAST);


        AbsoluteTimeParamDTO absoluteTimeParamDTO = new AbsoluteTimeParamDTO("2021-04-27","10:22:00","2021-05-01","23:59:59");
        paramDTO.setAbsoluteTimeParamDTO(absoluteTimeParamDTO);

//        PeriodicTimeParamDTO periodicTimeParamDTO = new PeriodicTimeParamDTO(new String[]{"monday","sunday"},"08:00","18:00");
//        paramDTO.setPeriodicTimeParamDTO(periodicTimeParamDTO);


        IpAddressRangeDTO ipAddressRangeDTO1 = new IpAddressRangeDTO();
        ipAddressRangeDTO1.setIpTypeEnum(RuleIPTypeEnum.IP4);
        ipAddressRangeDTO1.setStart("192.169.1.1");
        ipAddressRangeDTO1.setEnd("192.168.1.24");

        IpAddressRangeDTO ipAddressRangeDTO2 = new IpAddressRangeDTO();
        ipAddressRangeDTO2.setIpTypeEnum(RuleIPTypeEnum.IP4);
        ipAddressRangeDTO2.setStart("192.169.12.48");
        ipAddressRangeDTO2.setEnd("192.168.12.56");
        IpAddressParamDTO srcIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new IpAddressRangeDTO[]{ipAddressRangeDTO1,ipAddressRangeDTO2});
        paramDTO.setSrcIp(srcIp);

        IpAddressRangeDTO dstIpAddressRangeDTO = new IpAddressRangeDTO();
        dstIpAddressRangeDTO.setIpTypeEnum(RuleIPTypeEnum.IP4);
        dstIpAddressRangeDTO.setStart("192.169.251.12");
        dstIpAddressRangeDTO.setEnd("192.169.251.46");
        IpAddressParamDTO dstIp = new IpAddressParamDTO(RuleIPTypeEnum.IP4, new String[]{"10.12.111.33","10.12.109.234"});
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
        paramDTO.setSrcRefIpAddressObject(new String[]{"address1","address2"});
        paramDTO.setSrcRefIpAddressObjectGroup(new String[]{"addressGroup1","addressGropu2"});

        paramDTO.setDstRefIpAddressObject(new String[]{"addressA","addressB"});
        paramDTO.setDstRefIpAddressObjectGroup(new String[]{"addressGroupA","addressGropuB"});

        paramDTO.setRefServiceObject(new String[]{"service1","service2"});
        paramDTO.setRefServiceObjectGroup(new String[]{"serviceGroup1","serviceGroup2"});

        paramDTO.setRefTimeObject(new String[]{"time1","time2"});

        SecurityDp003Impl dp003 = new SecurityDp003Impl();
        System.out.println("迪普------------------》");
        System.out.println(dp003.generateSecurityPolicyCommandLine(StatusTypeEnum.ADD,paramDTO,null,null));
    }

}
