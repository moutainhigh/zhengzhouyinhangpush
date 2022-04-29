package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.AddressTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.external.utils.PolicyListCommonUtil;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.push.service.PushBussExtendService;
import com.abtnetworks.totems.recommend.dto.risk.DeviceInterfaceDto;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.model.ro.IPItemRO;
import com.abtnetworks.totems.whale.model.ro.NatClauseRO;
import com.abtnetworks.totems.whale.model.ro.PortSpecRO;
import com.abtnetworks.totems.whale.policybasic.ro.FilterListsRO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lifei
 * @desc XXXX
 * @date 2021/4/17 16:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushRemoteServiceimpl {


    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    PushBussExtendService pushBussExtendService;


    @Test
    public void tts() {
        String uuid = "9a2574710d074df984f3e03f13390c94";
        List<InterfacesRO> interfacesROS = whaleDeviceObjectClient.getDeviceInterfacesRO(uuid);
        System.out.println("96");
        System.out.println(interfacesROS);
    }
    @Test
    public void getDeviceROByUuid() {
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid("9a2574710d074df984f3e03f13390c94");
        System.out.println(JSONObject.toJSONString(deviceRO,true));
    }

    @Test
    public  void  queryListByRuleUuid(){
        String  uuid = "260c940d03c64e1e98bf5bd483905828";
        FilterListsRO Filte = whaleManager.getFilterListsByUuid(uuid);
        System.out.println("xxx");
    }

    @Test
    public void getFilterRuleListTest() {

        String deviceUuid = "59988849fb524b1ea062bb62c7d68aff";
        String filterRuleListUuid = "260c940d03c64e1e98bf5bd483905828";
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid,filterRuleListUuid);
        //格式化
        List<PolicyDetailVO> voList =  PolicyListCommonUtil.getPageShowData(resultRO.getData());
        System.out.println("格式化之后：" + JSONObject.toJSONString(voList));
    }

    @Test
    public void  getDeviceInteg(){
        List<InterfacesRO> interfacesROS = whaleDeviceObjectClient.getDeviceInterfacesRO("59988849fb524b1ea062bb62c7d68aff");
        System.out.println(interfacesROS);
    }

    @Test
    public void  getDeviceweweweg(){
//        DeviceRO deviceRO = whaleManager.getDeviceByUuid("59988849fb524b1ea062bb62c7d68aff");
//        System.out.println(deviceRO);
        ResultRO<List<DeviceFilterlistRO>> reos = whaleDevicePolicyClient.getDevicePolicyFilterlist("59988849fb524b1ea062bb62c7d68aff");
        System.out.println(reos);
    }


    @Test
    public void  getInterfaceMethod1(){
        String deviceUuid = "b3e9ee59f0a0430c8c84d818bc4ae092";
        String zoneName = "";
        List<String>  sout = whaleManager.getInterfaces(deviceUuid,zoneName);
        System.out.println(sout);
    }

    @Test
    public void  getInterfaceMethod2(){
        String deviceUuid = "b3e9ee59f0a0430c8c84d818bc4ae092";
        List<DeviceInterfaceDto> resultRo = whaleManager.getDeviceInterfaces(deviceUuid);
        System.out.println(resultRo);
    }

    @Test
    public void  getRoutTable(){
        String deviceUuid = "e03b6dc9716a4d1f987b959bb9770ecd";
        List<RoutingtableRO> resultRo = whaleManager.getRoutTable(deviceUuid);
        System.out.println(resultRo);
    }

    @Test
    public void  getInterfaceNames(){
        String deviceUuid = "bedd1352cc9641ef9724e82924feea86";
        List<String> list = pushBussExtendService.queryInterfaceNames(deviceUuid);
        System.out.println(list);
    }

    @Test
    public void  getExistIpsystemName(){
        String deviceUuid = "f38f89b4cd4645c4826e2aa343cf9fe3";
        String ipSystem = "a嵌套引用";
        boolean isExist = whaleManager.queryIpSystemHasExist(deviceUuid,ipSystem);
        System.out.println(isExist);
    }

    @Test
    public void  getNatPolicy(){
        String deviceUuid = "e035f09b24114b618fbf2c659935d616";
        NatPolicyDTO natPolicyDTO = new NatPolicyDTO();
        String dstIP = "5.5.5.5";
//        natPolicyDTO.setDstIp("3.1.1.1");
        natPolicyDTO.setPostDstIp("6.6.6.6");
        natPolicyDTO.setPostPort("50");
        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("6");
        serviceDTO.setDstPorts("23");
        serviceList.add(serviceDTO);
        natPolicyDTO.setServiceList(serviceList);
        ResultRO<List<DeviceFilterRuleListRO>>  resultRO = whaleManager.getFilterRuleListSearch(deviceUuid,dstIP,serviceList);

        if(null == resultRO){
            return;
        }
        String postDstIp = natPolicyDTO.getPostDstIp();
        String postPort = natPolicyDTO.getPostPort();

        for (DeviceFilterRuleListRO deviceFilterRuleListRO : resultRO.getData()) {
            NatClauseRO natClauseRO = JSONObject.toJavaObject(deviceFilterRuleListRO.getNatClause(),NatClauseRO.class);
            String[] ip4Addresses = postDstIp.split(",");

            String startIp = "";
            String endIp = "";
            AddressTypeEnum addressType = null;
            for (String ip4 : ip4Addresses) {
                if (IpUtils.isIPRange(ip4)) {
                    String[] ipSegment = ip4.trim().split("-");
                    startIp = ipSegment[0];
                    endIp = ipSegment[1];
                    addressType = AddressTypeEnum.RANG;
                    break;
                } else if (IpUtils.isIPSegment(ip4)) {
                    startIp = IpUtils.getStartIp(ip4);
                    endIp = IpUtils.getEndIp(ip4);
                    addressType = AddressTypeEnum.SUB;
                    break;
                } else if (IpUtils.isIP(ip4)) {
                    startIp = ip4;
                    endIp = ip4;
                    addressType = AddressTypeEnum.HOST;
                    break;
                }
            }
            List<IPItemRO> ipItemROS = natClauseRO.getPostDstIPItems();
            if(CollectionUtils.isEmpty(ipItemROS)){
                continue;
            }
            IPItemRO ipItemRO = ipItemROS.get(0);

            boolean samePostDst = false;
            if(Constants.HOST_IP.equalsIgnoreCase(ipItemRO.getType())){
                if(ipItemRO.getIp4Addresses().contains(startIp) && ipItemRO.getIp4Addresses().contains(endIp)){
                    samePostDst = true;
                }
            }else if (Constants.RANGE.equalsIgnoreCase(ipItemRO.getType())){
                if(ipItemRO.getIp4Range().getStart().equalsIgnoreCase(startIp) &&
                        ipItemRO.getIp4Range().getEnd().equalsIgnoreCase(endIp)){
                    samePostDst = true;
                }
            }

            boolean samePostPort = false;

            if(StringUtils.isBlank(postPort) && CollectionUtils.isEmpty(natClauseRO.getPostDstPortSpec())){
                samePostPort =true;
            } else if((StringUtils.isNotBlank(postPort) && CollectionUtils.isEmpty(natClauseRO.getPostDstPortSpec())) ||
                    (StringUtils.isBlank(postPort) && CollectionUtils.isNotEmpty(natClauseRO.getPostDstPortSpec())))       {
                samePostPort =false;
            }else {
                for (PortSpecRO portSpecRO :natClauseRO.getPostDstPortSpec() ){
                    if(CollectionUtils.isNotEmpty(portSpecRO.getPortValues()) && portSpecRO.getPortValues().contains(postPort)){
                        samePostPort =true;
                    }else{
                        samePostPort =false;
                    }
                }
            }
            if( samePostDst && samePostPort){
                System.out.println("王全一直");
                break;
            }
        }
        System.out.println(JSONObject.toJSONString(resultRO));
    }

}
