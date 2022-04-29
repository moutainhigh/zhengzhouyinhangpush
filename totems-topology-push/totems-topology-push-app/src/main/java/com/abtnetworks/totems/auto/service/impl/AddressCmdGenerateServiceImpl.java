package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.service.AddressCmdGenerateService;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.dto.IpAddressParamDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.enums.DeviceModelNumberEnumExtended;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.ObjectCompareUtils;
import com.abtnetworks.totems.retrieval.service.impl.RetrievalServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 对象管理命令行生成类
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
@Service
@Slf4j
public class AddressCmdGenerateServiceImpl implements AddressCmdGenerateService {

    /**
     * 比较地址对象生成命令行
     * @param allAddrMap 存放设备所有地址与内容的map
     * @param sb 存放拼接命令行的StringBuilder对象
     * @param detailVO 地址详情对象
     * @param addressAddList 比较设备与基线数据后新增值
     * @param addressDelList 比较设备与基线数据后删除值
     * @param deviceModelNumberEnum 设备类型
     */
    @Override
    public void generateCmdCompleteCompare(Map<String, List<String>> allAddrMap, StringBuilder sb, AddressManageDetailVO detailVO, List<String> addressAddList, List<String> addressDelList, DeviceModelNumberEnum deviceModelNumberEnum, List<String> hasAdressList) {
        //地址对象是否存在改变
        boolean hasChange = false;
        //二级地址名称
        String addressName = detailVO.getAddressName();
        List<String> currentAdd = new ArrayList<>();
        List<String> currentDel = new ArrayList<>();
        //子级
        List<AddressManageDetailVO> childList = detailVO.getChild();
        //子级地址对象
        List<String> childAddrList = childList.stream().map(AddressManageDetailVO::getAddressName).collect(Collectors.toList());
        List<String> childAddrEntryList = childList.stream().filter(e -> "ADDRESS".equals(e.getAddressType())).map(AddressManageDetailVO::getAddressName).collect(Collectors.toList());
        boolean isObject = CollectionUtils.isNotEmpty(childAddrEntryList) && childList.size() == childAddrEntryList.size();

        Map<String, List<String>> originalMap = new HashMap<>();
        originalMap.put(addressName, childAddrList);
        //比对结果
        Map<String, Map<String, List<String>>> resMap = ObjectCompareUtils.compareObjectMapList(allAddrMap, originalMap);

        if (resMap.containsKey("del")) {
            Map<String, List<String>> delMap = resMap.get("del");
            if (delMap.containsKey(addressName)) {
                hasChange = true;
                if (isObject) {
                    String addrEntry = String.join(PolicyConstants.ADDRESS_SEPERATOR, delMap.get(addressName));
                    if (!addressDelList.contains(addressName) ) {
                        getDeviceCmd(deviceModelNumberEnum, sb, addressName, addrEntry, false);
                        addressDelList.add(addrEntry);
                    }
                    currentDel.addAll(delMap.get(addressName));
                } else {
                    for (String childAddr : delMap.get(addressName)) {
                        //如果当前设备上没有这个地址对象
                        if (!addressDelList.contains(childAddr)) {
                            getDeviceCmd(deviceModelNumberEnum, sb, childAddr, childAddr, false);
                            addressDelList.add(childAddr);
                        }
                        currentDel.add(childAddr);
                    }
                }
            }
        }

        if (resMap.containsKey("add")) {
            Map<String, List<String>> addMap = resMap.get("add");
            if (addMap.containsKey(addressName)) {
                hasChange = true;
                if (isObject) {
                    String addrEntry = String.join(PolicyConstants.ADDRESS_SEPERATOR, addMap.get(addressName));
                    if (!addressAddList.contains(addressName) && !hasAdressList.contains(addressName) ) {
                        getDeviceCmd(deviceModelNumberEnum, sb, addressName, addrEntry, true);
                        addressAddList.add(addrEntry);
                        hasAdressList.add(addressName);
                    }
                    currentAdd.addAll(addMap.get(addressName));
                } else {
                    for (String childAddr : addMap.get(addressName)) {
                        //如果当前设备上没有这个地址对象
                        if (!addressAddList.contains(childAddr) && !hasAdressList.contains(childAddr) && !allAddrMap.containsKey(childAddr)) {
                            getDeviceCmd(deviceModelNumberEnum, sb, childAddr, childAddr, true);
                            addressAddList.add(childAddr);
                            hasAdressList.add(childAddr);
                        }
                        currentAdd.add(childAddr);
                    }
                }
            }
        }

        if (hasChange && !isObject) {
            hasAdressList.add(addressName);
            //将三级对象加到二级对象，如果有新增或删除对象，才进行此操作
            addAddressToGroupCmd(deviceModelNumberEnum, sb, addressName, childAddrList, currentAdd, currentDel);
        }

    }

    @Override
    public void getDeviceCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb, String addressName, String addressEntry, Boolean isAdd) {
        try {
            boolean needAdaptCmd = checkAdaptCmd(deviceModelNumberEnum);
            if (!needAdaptCmd) {
                sb.append("生成地址命令行出错，该设备暂不支持");
                return;
            }
            //获取命令行生成器
            OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = getGeneratorAbstractBean(deviceModelNumberEnum);
            //生成命令行
            String s = generaAddressCommandline(overAllGeneratorAbstractBean, addressName, addressEntry, isAdd);
            sb.append(s);
        } catch (Exception e) {
            log.error("生成地址命令行出错:",e);
            sb.append("生成地址命令行出错，该设备暂不支持");
        }
    }

    @Override
    public void addAddressToGroupCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb, String addressName, List<String> childList, List<String> addressAddList, List<String> addressDelList) {
        try {
            boolean needAdaptCmd = checkAdaptCmd(deviceModelNumberEnum);
            if (!needAdaptCmd) {
                sb.append("生成地址命令行出错，该设备暂不支持");
                return;
            }
            //获取命令行生成器
            OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = getGeneratorAbstractBean(deviceModelNumberEnum);
            //生成命令行
            String s = generaAddressGroupCommandline(overAllGeneratorAbstractBean, addressName, childList, addressAddList, addressDelList);
            sb.append(s);
        } catch (Exception e) {
            log.error("生成地址组命令行出错:",e);
            sb.append("生成地址组命令行出错，该设备暂不支持");
        }
    }

    @Override
    public void getPreCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb, Boolean isVsys, String vsysName) {
        boolean needPreCmd = checkNeedPreCmd(deviceModelNumberEnum);
        if (!needPreCmd) {
            return;
        }

        try {
            //获取命令行生成器
            OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = getGeneratorAbstractBean(deviceModelNumberEnum);
            StringBuilder preCmd = new StringBuilder();
            //生成命令行
            preCmd.append(overAllGeneratorAbstractBean.generatePreCommandline(isVsys, vsysName, null, null));
            sb.insert(0, preCmd);
        } catch (Exception e) {
            log.error("生成前置命令行出错:",e);
            sb.append("生成前置命令行出错，该设备暂不支持");
        }
    }

    @Override
    public void getPostCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb) {
        boolean needPostCmd = checkNeedPostCmd(deviceModelNumberEnum);
        if (!needPostCmd) {
            return;
        }
        try {
            //获取命令行生成器
            OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = getGeneratorAbstractBean(deviceModelNumberEnum);
            //后置命令行
            sb.append(overAllGeneratorAbstractBean.generatePostCommandline(null,null));
        } catch (Exception e) {
            log.error("生成后置命令行出错:",e);
            sb.append("生成后置命令行出错，该设备暂不支持");
        }
    }

    /**
     * 检测是否已适配命令行
     * @param deviceModelNumberEnum
     * @return
     */
    public boolean checkAdaptCmd (DeviceModelNumberEnum deviceModelNumberEnum) {
        switch (deviceModelNumberEnum) {
            case HILLSTONE:
            case HILLSTONE_R5:
            case HILLSTONE_V5:
            case FORTINET:
            case H3CV7:
                return true;
            default:
                return false;
        }
    }

    /**
     * 检测是否需要前置命令行
     * @param deviceModelNumberEnum
     * @return
     */
    public boolean checkNeedPreCmd (DeviceModelNumberEnum deviceModelNumberEnum) {
        switch (deviceModelNumberEnum) {
            case HILLSTONE:
            case HILLSTONE_R5:
            case HILLSTONE_V5:
                return false;
            default:
                return true;
        }
    }

    /**
     * 检测是否需要后置命令行
     * @param deviceModelNumberEnum
     * @return
     */
    public boolean checkNeedPostCmd (DeviceModelNumberEnum deviceModelNumberEnum) {
        switch (deviceModelNumberEnum) {
            case HILLSTONE:
            case HILLSTONE_R5:
            case HILLSTONE_V5:
            case FORTINET:
                return false;
            default:
                return true;
        }
    }

    /**
     * 获取命令行生成器
     * @param deviceModelNumberEnum
     * @return
     */
    public OverAllGeneratorAbstractBean getGeneratorAbstractBean(DeviceModelNumberEnum deviceModelNumberEnum) throws Exception {
        //获得设备型号
        DeviceModelNumberEnumExtended modelNumber = DeviceModelNumberEnumExtended.fromString(deviceModelNumberEnum.getKey());
        //获得生成器
        return (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(modelNumber.getSecurityClass());
    }

    /**
     * 生成地址相关命令行
     * @param overAllGeneratorAbstractBean
     * @param addressName
     * @param addressEntry
     * @param isAdd
     * @return
     */
    public String generaAddressCommandline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean, String addressName, String addressEntry, Boolean isAdd){
        //参数调用命令行生成器，生成最终命令行
        StringBuffer commandLine = new StringBuffer();
        try {
            if (addressName != null) {
                addressName = getRealAddressName(addressName);
                IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
                getDealWithAddress(ipAddressParamDTO, addressEntry);
                StatusTypeEnum statusTypeEnum = null;
                if (isAdd) {
                    statusTypeEnum = StatusTypeEnum.ADD;
                } else {
                    statusTypeEnum = StatusTypeEnum.DELETE;
                }
                commandLine.append(overAllGeneratorAbstractBean.generateManageIpAddressObjectCommandLine(statusTypeEnum, addressName, ipAddressParamDTO.getSingleIpArray()
                        , ipAddressParamDTO.getRangIpArray(), ipAddressParamDTO.getSubnetIntIpArray(), ipAddressParamDTO.getSubnetStrIpArray(), ipAddressParamDTO.getHosts(), null, null));
            }
        } catch (Exception e) {
            log.error("生成地址命令行异常:",e);
        }
        return commandLine.toString();
    }

    /**
     * 生成地址组相关命令行
     * @param overAllGeneratorAbstractBean
     * @param addressName
     * @param childList
     * @param addressAddList
     * @param addressDelList
     * @return
     */
    public String generaAddressGroupCommandline(OverAllGeneratorAbstractBean overAllGeneratorAbstractBean, String addressName, List<String> childList, List<String> addressAddList, List<String> addressDelList){
        //参数调用命令行生成器，生成最终命令行
        StringBuffer commandLine = new StringBuffer();
        try {
            if (addressName != null) {
                addressName = getRealAddressName(addressName);
                IpAddressParamDTO ipAddressParamDTO = new IpAddressParamDTO();
                String[] objectNameArray = null;
                if (CollectionUtils.isNotEmpty(childList)) {
                    objectNameArray = childList.toArray(new String[0]);
                }
                String[] addObjectNameArray = null;
                if (CollectionUtils.isNotEmpty(addressAddList)) {
                    addressAddList.remove(addressName);
                    addObjectNameArray = addressAddList.toArray(new String[0]);
                }
                String[] delObjectNameArray = null;
                if (CollectionUtils.isNotEmpty(addressDelList)) {
                    delObjectNameArray = addressDelList.toArray(new String[0]);
                }
                commandLine.append(overAllGeneratorAbstractBean.generateManageIpAddressGroupObjectCommandLine(StatusTypeEnum.ADD, addressName, ipAddressParamDTO.getSingleIpArray()
                        , ipAddressParamDTO.getRangIpArray(), ipAddressParamDTO.getSubnetStrIpArray(), ipAddressParamDTO.getHosts(), objectNameArray, addObjectNameArray, delObjectNameArray, null, null));
            }
        } catch (Exception e) {
            log.error("生成地址组命令行异常:",e);
        }
        return commandLine.toString();
    }

    /**
     * 处理地址命令生成对象格式
     * @param ipAddressParamDTO
     * @param addressEntry
     */
    public void getDealWithAddress(IpAddressParamDTO ipAddressParamDTO, String addressEntry) {
        String[] addsArray = addressEntry.split(PolicyConstants.ADDRESS_SEPERATOR);
        RetrievalServiceImpl retrievalServiceImpl = new RetrievalServiceImpl();
        for (String val : addsArray) {
            retrievalServiceImpl.dealWithAddress(ipAddressParamDTO, val);
            if (IPUtil.isIPSegment(val)) {
                IpAddressSubnetIntDTO subnetIntDto = new IpAddressSubnetIntDTO();
                String ipAddress = val.split("/")[0];
                String mask = val.split("/")[1];
                subnetIntDto.setIp(ipAddress);
                subnetIntDto.setMask(Integer.parseInt(mask));
                //是否已存在
                if(ipAddressParamDTO.getSubnetIntIpArray() != null){
                    IpAddressSubnetIntDTO[] subnetIpArray = ipAddressParamDTO.getSubnetIntIpArray();
                    List<IpAddressSubnetIntDTO> dtoList = Arrays.asList(subnetIpArray);
                    List<IpAddressSubnetIntDTO> dtoLists = new ArrayList<>(dtoList);
                    dtoLists.add(subnetIntDto);
                    IpAddressSubnetIntDTO[] newRangeIpArray = new IpAddressSubnetIntDTO[dtoLists.size()];
                    ipAddressParamDTO.setSubnetIntIpArray(dtoLists.toArray(newRangeIpArray));
                } else {
                    IpAddressSubnetIntDTO[] subnetStrIpArray={subnetIntDto};
                    ipAddressParamDTO.setSubnetIntIpArray(subnetStrIpArray);
                }
            }
        }
    }

    /**
     * 获取真实的地址名称
     * @param addressName 转换前地址名称
     * @return String 转换后地址名称，去掉IP_OBJ_GRP或IP_OBJ
     */
    private String getRealAddressName(String addressName) {
        //去掉IP_OBJ_GRP或IP_OBJ
        if (addressName.startsWith("IP_OBJ_GRP_")) {
            return addressName.substring(11, addressName.length());
        }

        if (addressName.startsWith("IP_OBJ_")) {
            return addressName.substring(7, addressName.length());
        }

        return addressName;
    }

}
