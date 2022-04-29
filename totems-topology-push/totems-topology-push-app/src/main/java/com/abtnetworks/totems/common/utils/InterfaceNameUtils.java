package com.abtnetworks.totems.common.utils;

import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceInterfaceRO;
import com.abtnetworks.totems.whale.baseapi.ro.InterfacesRO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luwei
 * @date 2019/4/11
 */
public class InterfaceNameUtils {


    /**
     * 获取设备接口信息
     * @param deviceData 设备数据
     * @param interfaceUuid 接口uuid
     * @return 接口信息数组，0为接口名，1为接口别名
     */
    public static List<String> getInterfaceName(DeviceDataRO deviceData, String interfaceUuid) {
        String name = "";
        String alias = "";

        List<InterfacesRO> interfaceList = deviceData.getInterfaces();
        if(interfaceList == null) {
            List<String> list = new ArrayList<>();
            list.add(name);
            list.add(alias);
            return list;
        }

        List<DeviceInterfaceRO> deviceInterfaceList = deviceData.getDeviceInterfaces();

        for(InterfacesRO interfaceRO:interfaceList) {
            if(interfaceRO.getUuid().equals(interfaceUuid)) {
                String nameString = interfaceRO.getDeviceInterfaceName();
                name = nameString;
                break;

            }
        }

        if(deviceInterfaceList == null) {
            List<String> list = new ArrayList<>();
            list.add(name);
            list.add(alias);
            return list;
        }

        for(DeviceInterfaceRO deviceInterfaceRO: deviceInterfaceList) {
            if(deviceInterfaceRO.getName().equals(name)) {
                alias = deviceInterfaceRO.getAlias();
                break;
            }
        }

        List<String> list = new ArrayList<>();
        list.add(name);
        list.add(alias);
        return list;
    }
}
