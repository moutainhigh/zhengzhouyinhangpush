package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateAddressDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreatePolicyDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateServiceDTO;
import com.abtnetworks.totems.push.manager.NsfocusExternalManager;
import com.abtnetworks.totems.push.service.PushForbidIpService;
import com.abtnetworks.totems.push.service.platform.PushNsfocusApiCmdService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import io.swagger.annotations.ApiModelProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @since 2021/3/10
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushNsServiceImplTest {

    @Resource
    NsfocusExternalManager nsfocusExternalManager;

    @Resource
    PushNsfocusApiCmdService pushNsfocusApiCmdService;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private WhaleManager whaleManager;

    @Test
    public void pushNsfocusApiCmd(){
        PushCmdDTO pushCmdDTO =new PushCmdDTO();
        NodeEntity nodeEntity = nodeMapper.getTheNodeByIp("192.168.9.227");

        pushNsfocusApiCmdService.PushNsfocusApiCmd(pushCmdDTO,nodeEntity);

    }

    @Test
    public void querySerevice(){
        List<ServiceDTO> array = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("2222,2223");
        serviceDTO.setSrcPorts("any");
        array.add(serviceDTO);
        DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
        deviceForExistObjDTO.setDeviceUuid("273ab8181e0a483d83f7a43a4f5b62a4");
        deviceForExistObjDTO.setModelNumber(DeviceModelNumberEnum.NSFOCUS);
        String serviceName = whaleManager.getCurrentServiceObjectName(array, deviceForExistObjDTO, null);
        System.out.println(serviceName);
    }


    @Test
    public void  addSecurityPolicy(){
        ManagementPlatformCreatePolicyDTO policyDTO = new ManagementPlatformCreatePolicyDTO();
        policyDTO.setTicket("test-api-ns5");
        policyDTO.setUserName("admin");
        policyDTO.setPassword("nsfocus");
        List<String> stcAddress = new ArrayList<>();
        stcAddress.add("110010");
        stcAddress.add("110011");
        policyDTO.setSrcaddrs(stcAddress);
        List<String> dstAddress = new ArrayList<>();
        dstAddress.add("110003");
        dstAddress.add("110008");
        policyDTO.setDstaddrs(dstAddress);
        policyDTO.setSrcZone("DMZ");
        policyDTO.setDstZone("Intranet");
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add("315010");
        List<String> timeNames = new ArrayList<>();
        timeNames.add("343001");
        policyDTO.setServiceNames(serviceNames);
        policyDTO.setScheduleNames(timeNames);
        policyDTO.setAction(ActionEnum.PERMIT);
//        policyDTO.setIdleTimeout(1800);
        ReturnT<String> aa =  nsfocusExternalManager.createSecurityPolicyData(policyDTO);
        System.out.println(aa.getCode());
    }

    @Test
    public void  addIPV6Address(){
        ManagementPlatformCreateAddressDTO addressDTO = new ManagementPlatformCreateAddressDTO();
        addressDTO.setTicket("lf-test-api4");
        addressDTO.setUserName("admin");
        addressDTO.setPassword("nsfocus");
        addressDTO.setSrcIp("2001:2:3:4:5:6:fffe::/52");
        addressDTO.setSrcIpSystem("2001:2:3:4:5:6:fffe::/52");
        addressDTO.setIpType(1);
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setDeviceUuid("8622ecfdd944463bb67d4ebf5b1b18c4");
        deviceDTO.setModelNumber(DeviceModelNumberEnum.NSFOCUS);
        addressDTO.setDeviceDTO(deviceDTO);
        addressDTO.setAddressPropertyEnum(AddressPropertyEnum.SRC);
        ReturnT<List<String>> aa =  nsfocusExternalManager.createIPV6SrcAddressData(addressDTO);
        System.out.println(aa.getData());
    }

    @Test
    public void  addIPV4Address(){
        ManagementPlatformCreateAddressDTO addressDTO = new ManagementPlatformCreateAddressDTO();
        addressDTO.setTicket("lf-test-api423");
        addressDTO.setUserName("admin");
        addressDTO.setPassword("nsfocus");
        addressDTO.setDstIp("any");
        addressDTO.setIpType(1);
        ReturnT<List<String>> aa =  nsfocusExternalManager.createIPV4DstAddressData(addressDTO);
        System.out.println(aa.getData());
    }


    @Test
    public void  addService(){
        ManagementPlatformCreateServiceDTO serviceDTO = new ManagementPlatformCreateServiceDTO();
        serviceDTO.setTicket("lf-test-api4");
        serviceDTO.setUserName("admin");
        serviceDTO.setPassword("nsfocus");
        List<ServiceDTO> serviceDTOS = new ArrayList<>();
        ServiceDTO serviceDTO1 = new  ServiceDTO();
        serviceDTO1.setProtocol("6");// TCP情况
        serviceDTO1.setDstPorts("20-40");
        serviceDTOS.add(serviceDTO1);
        serviceDTO.setServiceList(serviceDTOS);
        DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
        deviceForExistObjDTO.setDeviceUuid("8622ecfdd944463bb67d4ebf5b1b18c4");
        deviceForExistObjDTO.setModelNumber(DeviceModelNumberEnum.NSFOCUS);
        serviceDTO.setDeviceForExistObjDTO(deviceForExistObjDTO);
        ReturnT<List<String>> aa =  nsfocusExternalManager.createServiceData(serviceDTO);
        System.out.println(aa.getData());
    }

    @Test
    public void createSnatPolicy(){
        ManagementPlatformCreatePolicyDTO policyDTO = new ManagementPlatformCreatePolicyDTO();
        policyDTO.setTicket("test-snat555ss");
        policyDTO.setUserName("admin");
        policyDTO.setPassword("nsfocus");
        List<String> stcAddress = new ArrayList<>();
        stcAddress.add("120018");
        policyDTO.setSrcaddrs(stcAddress);
        List<String> dstAddress = new ArrayList<>();
        dstAddress.add("120024");
        policyDTO.setDstaddrs(dstAddress);
        policyDTO.setSrcZone("Intranet");
        policyDTO.setDstZone("DMZ");
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add("310002");
        List<String> timeNames = new ArrayList<>();
        timeNames.add("343001");
        policyDTO.setServiceNames(serviceNames);
        policyDTO.setDstItf("G1/5");
        List<String> postSrcaddrs  = new ArrayList<>() ;
        postSrcaddrs.add("790001");
        policyDTO.setPostSrcaddrs(postSrcaddrs);
        ReturnT<String> aa =  nsfocusExternalManager.createSNatPolicyData(policyDTO);
        System.out.println(aa.getData());
    }

    @Test
    public void createDnatPolicy(){
        ManagementPlatformCreatePolicyDTO policyDTO = new ManagementPlatformCreatePolicyDTO();
        policyDTO.setTicket("test-api-ns-dnat666");
        policyDTO.setUserName("admin");
        policyDTO.setPassword("nsfocus");
        List<String> stcAddress = new ArrayList<>();
        stcAddress.add("100001");
        policyDTO.setSrcaddrs(stcAddress);
        List<String> dstAddress = new ArrayList<>();
        dstAddress.add("100001");
        policyDTO.setDstaddrs(dstAddress);
        policyDTO.setSrcZone("DMZ");
        policyDTO.setDstZone("Intranet");
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add("315010");
        List<String> timeNames = new ArrayList<>();
        timeNames.add("343001");
        policyDTO.setServiceNames(serviceNames);
        policyDTO.setSrcItf("G1/1");
//        policyDTO.setPostDstIp("110008");
        policyDTO.setPostSrcIp("110002,110003");

        List<ServiceDTO> serviceList =new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setDstPorts("80");
        serviceDTO.setProtocol("6");
        serviceList.add(serviceDTO);
        policyDTO.setServiceList(serviceList);
        // postPort
        List<ServiceDTO> postServiceList =new ArrayList<>();
        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setDstPorts("81");
        postServiceList.add(serviceDTO1);
        policyDTO.setPostServiceList(postServiceList);
        ReturnT<String> aa =  nsfocusExternalManager.createDNatPolicyData(policyDTO);
        System.out.println(aa.getData());
    }


    @Test
    public void createAddreeeGroup(){
        ManagementPlatformCreateAddressDTO addressDTO = new ManagementPlatformCreateAddressDTO();
        addressDTO.setTicket("lf-test-addrgroup");
        addressDTO.setUserName("admin");
        addressDTO.setPassword("nsfocus");
        addressDTO.setIpType(1);
        addressDTO.setAddressPropertyEnum(AddressPropertyEnum.SRC);
        addressDTO.setPostSrcIpSystem("Addr-Name");
        addressDTO.setAddrGroupId("120163,120164");
        ReturnT<String> aa =  nsfocusExternalManager.createAddressDataGroup(addressDTO);
        System.out.println(aa.getData());
    }

}
