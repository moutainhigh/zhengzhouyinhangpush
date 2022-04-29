package com.abtnetworks.totems.common.dto.commandline;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {

    String srcPorts;

    String dstPorts;

    String protocol;

    String code;

    String type;

    public boolean equals(ServiceDTO service) {
        if(service.getDstPorts() == null && dstPorts == null) {

        } else if(service.getDstPorts() == null && dstPorts != null) {
            return false;
        } else if( service.getDstPorts() != null && dstPorts == null) {
            return false;
        } else if (!service.getDstPorts().equals(dstPorts)) {
            return false;
        }

        if(service.getProtocol() == null && protocol == null) {

        } else if(service.getProtocol() == null && protocol != null) {
            return false;
        } else if( service.getProtocol() != null && protocol == null) {
            return false;
        } else if (!service.getProtocol().equals(protocol)) {
            return false;
        }

        return true;
    }

    public static List<ServiceDTO> getServiceList() {
        //多服务
        List<ServiceDTO> serviceDTOList = new ArrayList<>();
        ServiceDTO service1 = new ServiceDTO();
        service1.setProtocol("6");
        service1.setSrcPorts("any");
        service1.setDstPorts("80");

        ServiceDTO service2 = new ServiceDTO();
        service2.setProtocol("17");
        service2.setSrcPorts("any");
        service2.setDstPorts("60-80");

//        service1.setDstPorts("3,4,1521-1522");
        serviceDTOList.add(service1);
        serviceDTOList.add(service2);
        serviceDTOList.add(service1);

//        service1 = new ServiceDTO();
//        service1.setProtocol("6");
//        service1.setSrcPorts("any");
//        service1.setDstPorts("10,20");
//        service1.setDstPorts("any");
//        serviceDTOList.add(service1);

//        ServiceDTO service2 = new ServiceDTO();
//        service2.setProtocol("17");
//        service2.setSrcPorts("any");
//        service2.setDstPorts("any");
//        service2.setDstPorts("10,20");
//        serviceDTOList.add(service2);
////
//        ServiceDTO service3 = new ServiceDTO();
//        service3.setProtocol("1");
//        service3.setSrcPorts("any");
//        service3.setDstPorts("any");
//////        service3.setType("20");
//////        service3.setCode("1024");
//        serviceDTOList.add(service3);

       /* ServiceDTO service4 = new ServiceDTO();
        service4.setProtocol("0");
        service4.setSrcPorts("any");
        service4.setDstPorts("any");
        serviceDTOList.add(service4);*/

        return serviceDTOList;
    }

}
