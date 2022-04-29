package com.abtnetworks.totems.push.manager.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.forti.*;
import com.abtnetworks.totems.push.dto.platform.dto.*;
import com.abtnetworks.totems.push.manager.FortiExternalManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 外部api接口管理实现类
 *
 * @author lifei
 * @since 2021/2/3
 **/
@Service
@Log4j2
public class FortiExternalManagerImpl implements FortiExternalManager {

    @Value("${management-platform-url.fortinet.common-url}")
    private String commonUrl;

    @Value("${management-platform-url.fortinet.common-path}")
    private String commonPath;

    @Value("${management-platform-url.fortinet.login}")
    private String getTokenPath;

    @Value("${management-platform-url.fortinet.get-package-name}")
    private String getPackagePath;

    @Value("${management-platform-url.fortinet.create-IPV4-address}")
    private String createIPV4AddressPath;

    @Value("${management-platform-url.fortinet.create-IPV6-address}")
    private String createIPV6AddressPath;

    @Value("${management-platform-url.fortinet.create-service-object}")
    private String createServiceObjectPath;

    @Value("${management-platform-url.fortinet.create-time-object}")
    private String createTimeObjectPath;

    @Value("${management-platform-url.fortinet.create-policy}")
    private String createPolicyObjectPath;

    @Value("${management-platform-url.fortinet.install}")
    private String installPath;

    @Value("${management-platform-url.fortinet.delete-policy}")
    private String deletePolicyPath;

    @Autowired
    WhaleManager whaleManager;

    @Override
    public ReturnT<String> getToken(ManagementPlatformLoginDTO loginDTO, String requestIp) {
        try {
            long startTime = System.currentTimeMillis();
            // 构建请求url
            String url = "https://" + requestIp + commonUrl;
            FortiLoginDTO fortiLoginDTO = new FortiLoginDTO();
            fortiLoginDTO.setUser(loginDTO.getUserName());
            fortiLoginDTO.setPasswd(loginDTO.getPassword());
            // 构建参数消息体
            Map<String, Object> parameter = buildParameter(fortiLoginDTO, "exec", getTokenPath, true, false, null);
            // 执行请求
            log.info("请求飞塔管理平台获取session入参:{},url:{}", JSON.toJSONString(parameter), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);

            if (null != jsonObject) {
                log.info("请求飞塔管理平台获取session,返回json:{}", jsonObject.toJSONString());
                // 解析返回对象，如果是异常的直接返回异常信息
                ReturnT<String> statusObject = getReturnFail(jsonObject);
                if (statusObject != null) {
                    return statusObject;
                }
                String session = jsonObject.getString("session");
                long endTime = System.currentTimeMillis();
                long time = (endTime - startTime) / 1000;
                log.info("获取Session结束, Session:{},耗时：{}秒", session, time);
                return new ReturnT(session);
            } else {
                log.error("根据设备uuid[{}]请求飞塔平台获取Session-api失败", loginDTO.getDeviceUuid());
                return new ReturnT(ReturnT.FAIL_CODE, "调用飞塔管理平台seesion接口返回为空");
            }
        } catch (Exception e) {
            log.error("获取飞塔管理平台seesion异常，异常原因:{}", e);
            return new ReturnT(ReturnT.FAIL_CODE, e.getMessage());
        }
    }

    @Override
    public ReturnT<String> getPackageName(String deviceName, String vsysName, String requestIp, String session) {
        log.info("进入飞塔管理平台获取包名入参:{}", deviceName);
        try {
            // 构建请求url
            String url = "https://" + requestIp + commonUrl;
            // 构建参数消息体
            String packagePaths = getPackagePath + vsysName;
            Map<String, Object> parameter = buildParameter(deviceName, "get", packagePaths, false, true, session);
            // 执行请求
            log.info("请求飞塔管理平台获取包名入参:{},url:{}", parameter.toString(), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);
            if (null != jsonObject) {
                log.info("请求飞塔管理平台获取包名,返回json:{}", jsonObject.toJSONString());
                return analysisResultFromPackageName(deviceName, jsonObject);
            } else {
                log.error("根据设备名称[{}]请求飞塔平台获取包名称返回为空", deviceName);
                return new ReturnT(ReturnT.FAIL_CODE, "请求飞塔平台获取包名称返回为空");
            }
        } catch (Exception e) {
            log.error("根据设备名称[{}]请求飞塔平台获取包名称失败", deviceName);
            return new ReturnT(ReturnT.FAIL_CODE, e.getMessage());
        }
    }

    @Override
    public ReturnT<List<String>> createIPV4SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session) {
        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        log.info("开始创建IPV4地址对象，请求url ：{}，入参:{}", url, JSON.toJSONString(addressDTO));
        // 如果地址为空，则默认返回飞塔默认的地址对象名称给我们平台，方便策略生成的时候传参
        if (AliStringUtils.isEmpty(addressDTO.getSrcIp())) {
            List<String> list = new ArrayList<>();
            list.add("all");
            return new ReturnT(list);
        }
        // 获取构建地址DTO集合
        FortiAddressMergeDTO fortiAddressMergeDTOs = buildFortiAddressDTO(addressDTO.getTicket(), addressDTO.getSrcIp(), addressDTO.getItf(), false, addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum());
        log.info("构建地址DTO集合，fortiAddressMergeDTOs ：{}", JSON.toJSONString(fortiAddressMergeDTOs));
        List<ReturnT<String>> resultList = new ArrayList<>();
        for (FortiAddressDTO fortiAddressDTO : fortiAddressMergeDTOs.getFortiAddressDTOs()) {
            // 转换成请求对象参数
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", fortiAddressDTO.getName());
            map.put("type", fortiAddressDTO.getType());
            if (0 == fortiAddressDTO.getType()) {
                map.put("subnet", fortiAddressDTO.getSubnet());
            } else if (1 == fortiAddressDTO.getType()) {
                map.put("start-ip", fortiAddressDTO.getStartIp());
                map.put("end-ip", fortiAddressDTO.getEndIp());
            } else {
                map.put("fqdn", fortiAddressDTO.getFqdn());
            }
            map.put("associated-interface", fortiAddressDTO.getAssociatedInterface());
            try {
                String path = commonPath + addressDTO.getVsysName() + createIPV4AddressPath;
                // 构建参数消息体
                log.info("path ：{}", path);
                Map<String, Object> parameter = buildParameter(map, "add", path, false, false, session);
                // 执行请求
                log.info("请求飞塔管理平台创建ipv4源地址对象入参:{},url:{}", JSON.toJSONString(parameter), url);
                String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
                JSONObject jsonObject = JSON.parseObject(result);
                if (null != jsonObject) {
                    log.info("请求飞塔管理平台创建ipv4源地址对象,返回json:{}", jsonObject.toJSONString());
                    String objectNameForError = String.format(" Create IPV4 srcAddress Object:%s error", fortiAddressDTO.getName());
                    resultList.add(analysisResultForCommon(jsonObject, false, objectNameForError));
                } else {
                    log.error("根据设备uuid[{}]请求飞塔平台创建ipv4源地址对象为空", addressDTO.getDeviceUuid());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台创建ipv4源地址对象返回为空"));
                }
            } catch (Exception e) {
                log.error("请求飞塔平台创建ipv4源地址对象异常,异常原因:{}", e);

                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV4 srcAddress Object:%s error, reason is:%s", fortiAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : fortiAddressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }


    @Override
    public ReturnT<List<String>> createIPV4DstAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session) {
        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        log.info("开始创建IPV4目的地址对象，请求url ：{}，入参:{}", url, JSON.toJSONString(addressDTO));
        // 如果地址为空，则默认返回飞塔默认的地址对象名称给我们平台，方便策略生成的时候传参
        if (AliStringUtils.isEmpty(addressDTO.getDstIp())) {
            List<String> list = new ArrayList<>();
            list.add("all");
            return new ReturnT(list);
        }
        // 获取构建地址DTO集合
        FortiAddressMergeDTO fortiAddressMergeDTOs = buildFortiAddressDTO(addressDTO.getTicket(), addressDTO.getDstIp(), addressDTO.getItf(), false, addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum());
        List<ReturnT<String>> resultList = new ArrayList<>();
        for (FortiAddressDTO fortiAddressDTO : fortiAddressMergeDTOs.getFortiAddressDTOs()) {
            // 转换成请求对象参数
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", fortiAddressDTO.getName());
            map.put("type", fortiAddressDTO.getType());
            if (0 == fortiAddressDTO.getType()) {
                map.put("subnet", fortiAddressDTO.getSubnet());
            } else if (1 == fortiAddressDTO.getType()) {
                map.put("start-ip", fortiAddressDTO.getStartIp());
                map.put("end-ip", fortiAddressDTO.getEndIp());
            } else {
                map.put("fqdn", fortiAddressDTO.getFqdn());
            }
            map.put("associated-interface", fortiAddressDTO.getAssociatedInterface());
            try {
                String path = commonPath + addressDTO.getVsysName() + createIPV4AddressPath;
                // 构建参数消息体
                Map<String, Object> parameter = buildParameter(map, "add", path, false, false, session);
                // 执行请求
                log.info("请求飞塔管理平台创建ipv4目的地址对象入参:{},url:{}", JSONObject.toJSONString(parameter), url);
                String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
                JSONObject jsonObject = JSON.parseObject(result);
                if (null != jsonObject) {
                    log.info("请求飞塔管理平台创建ipv4目的地址对象,返回json:{}", jsonObject.toJSONString());
                    String objectNameForError = String.format(" Create IPV4 dstAddress Object:%s error", fortiAddressDTO.getName());
                    resultList.add(analysisResultForCommon(jsonObject, false, objectNameForError));
                } else {
                    log.error("根据设备uuid[{}]请求飞塔平台创建ipv4目的地址对象返回为空", addressDTO.getDeviceUuid());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台创建ipv4目的地址对象返回为空"));
                }
            } catch (Exception e) {
                log.error("请求飞塔平台创建ipv4目的地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV4 dstAddress Object:%s error, reason is:%s", fortiAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : fortiAddressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }

    @Override
    public ReturnT<List<String>> createIPV6SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session) {
        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        log.info("开始创建IPV6源地址对象，请求url ：{}，入参:{}", url, JSON.toJSONString(addressDTO));
        // 如果地址为空，则默认返回飞塔默认的地址对象名称给我们平台，方便策略生成的时候传参
        if (AliStringUtils.isEmpty(addressDTO.getSrcIp())) {
            List<String> list = new ArrayList<>();
            list.add("all");
            return new ReturnT(list);
        }
        // 获取构建地址DTO集合
        FortiAddressMergeDTO fortiAddressMergeDTOs = buildFortiAddressDTO(addressDTO.getTicket(), addressDTO.getSrcIp(), addressDTO.getItf(), true, addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum());
        List<ReturnT<String>> resultList = new ArrayList<>();
        for (FortiAddressDTO fortiAddressDTO : fortiAddressMergeDTOs.getFortiAddressDTOs()) {
            // 转换成请求对象参数
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", fortiAddressDTO.getName());
            map.put("type", fortiAddressDTO.getType());
            if (0 == fortiAddressDTO.getType()) {
                map.put("ip6", fortiAddressDTO.getSubnet());
            } else if (1 == fortiAddressDTO.getType()) {
                map.put("start-ip", fortiAddressDTO.getStartIp());
                map.put("end-ip", fortiAddressDTO.getEndIp());
            } else {
                map.put("fqdn", fortiAddressDTO.getFqdn());
            }
            map.put("associated-interface", fortiAddressDTO.getAssociatedInterface());
            try {
                String path = commonPath + addressDTO.getVsysName() + createIPV6AddressPath;
                // 构建参数消息体
                Map<String, Object> parameter = buildParameter(map, "add", path, false, false, session);
                // 执行请求
                log.info("请求飞塔管理平台创建ipv6源地址对象入参:{},url:{}", JSONObject.toJSONString(parameter), url);
                String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
                JSONObject jsonObject = JSON.parseObject(result);
                if (null != jsonObject) {
                    log.info("请求飞塔管理平台创建ipv6源地址对象,返回json:{}", jsonObject.toJSONString());
                    String objectNameForError = String.format(" Create IPV6 srcAddress Object:%s error", fortiAddressDTO.getName());
                    resultList.add(analysisResultForCommon(jsonObject, false, objectNameForError));
                } else {
                    log.error("根据设备uuid[{}]请求飞塔平台创建ipv6源地址对象返回为空", addressDTO.getDeviceUuid());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台创建ipv6源地址对象返回为空"));
                }
            } catch (Exception e) {
                log.error("请求飞塔平台创建ipv6源地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV6 srcAddress Object:%s error, reason is:%s", fortiAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : fortiAddressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }

    @Override
    public ReturnT<List<String>> createIPV6DstAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session) {
        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        log.info("开始创建IPV6目的地址对象，请求url ：{}，入参:{}", url, JSON.toJSONString(addressDTO));
        // 如果地址为空，则默认返回飞塔默认的地址对象名称给我们平台，方便策略生成的时候传参
        if (AliStringUtils.isEmpty(addressDTO.getDstIp())) {
            List<String> list = new ArrayList<>();
            list.add("all");
            return new ReturnT(list);
        }
        // 获取构建地址DTO集合
        FortiAddressMergeDTO fortiAddressMergeDTOs = buildFortiAddressDTO(addressDTO.getTicket(), addressDTO.getDstIp(), addressDTO.getItf(), true, addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum());
        List<ReturnT<String>> resultList = new ArrayList<>();
        for (FortiAddressDTO fortiAddressDTO : fortiAddressMergeDTOs.getFortiAddressDTOs()) {
            // 转换成请求对象参数
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", fortiAddressDTO.getName());
            map.put("type", fortiAddressDTO.getType());

            if (0 == fortiAddressDTO.getType()) {
                map.put("ip6", fortiAddressDTO.getSubnet());
            } else if (1 == fortiAddressDTO.getType()) {
                map.put("start-ip", fortiAddressDTO.getStartIp());
                map.put("end-ip", fortiAddressDTO.getEndIp());
            } else {
                map.put("fqdn", fortiAddressDTO.getFqdn());
            }
            map.put("associated-interface", fortiAddressDTO.getAssociatedInterface());
            try {
                String path = commonPath + addressDTO.getVsysName() + createIPV6AddressPath;
                // 构建参数消息体
                Map<String, Object> parameter = buildParameter(map, "add", path, false, false, session);
                // 执行请求
                log.info("请求飞塔管理平台创建ipv6目的地址对象入参:{},url:{}", JSONObject.toJSONString(parameter), url);
                String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
                JSONObject jsonObject = JSON.parseObject(result);
                if (null != jsonObject) {
                    log.info("请求飞塔管理平台创建ipv6目的地址对象,返回json:{}", jsonObject.toJSONString());
                    String objectNameForError = String.format(" Create IPV6 dstAddress Object:%s error", fortiAddressDTO.getName());
                    resultList.add(analysisResultForCommon(jsonObject, false, objectNameForError));
                } else {
                    log.error("根据设备uuid[{}]请求飞塔平台创建ipv6目的地址对象返回为空", addressDTO.getDeviceUuid());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台创建ipv6目的地址对象返回为空"));
                }
            } catch (Exception e) {
                log.error("请求飞塔平台创建ipv6目的地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV6 dstAddress Object:%s error, reason is:%s", fortiAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : fortiAddressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }

    @Override
    public ReturnT<List<String>> createServiceData(ManagementPlatformCreateServiceDTO serviceDTO, String requestIp, String session) {
        log.info("开始创建服务对象入参:{}", JSON.toJSONString(serviceDTO));
        List<ServiceDTO> serviceDTOS = serviceDTO.getServiceList();
        // 如果服务为空，则默认返回飞塔默认的服务对象名称给我们平台，方便策略生成的时候传参
        if (null == serviceDTOS) {
            List<String> list = new ArrayList<>();
            list.add("All");
            return new ReturnT(list);
        }
        // 定义返回创建的对象名称集合，tcp
        Map<String, Object> map = new LinkedHashMap<>();
        // 定义可复用的服务名称集合
        List<String> existServiceNames = new ArrayList<>();
        // 遍历服务中的所有协议和端口，批量生成服务对象
        for (ServiceDTO serDTO : serviceDTOS) {

            List<ServiceDTO> array = new ArrayList<>();
            array.add(serDTO);
            log.info("调用whale接口查询服务对象复用，服务参数:{},设备数据:{},长连接:{}", array, JSON.toJSONString(serviceDTO.getDeviceForExistObjDTO()), serviceDTO.getIdleTimeout());
            String serviceName = whaleManager.getCurrentServiceObjectName(array, serviceDTO.getDeviceForExistObjDTO(), serviceDTO.getIdleTimeout());
            log.info("调用whale接口查询服务对象返回结果:{}", serviceName);
            if (!AliStringUtils.isEmpty(serviceName)) {
                existServiceNames.add(serviceName);
                continue;
            }
            String protocol = ProtocolUtils.getProtocolByString(serDTO.getProtocol());
            List<String> portRange = new ArrayList<>();
            // 协议类型不为icmp的才会有目的端口
            if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                // 如果选择了协议但是没有填写端口则设置默认值
                if (AliStringUtils.isEmpty(serDTO.getDstPorts()) || PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(serDTO.getDstPorts())) {
                    serDTO.setDstPorts(PolicyConstants.PORT_ANY);
                }
                String[] dstPorts = serDTO.getDstPorts().split(",");
                for (String dstPort : dstPorts) {
                    StringBuilder sb = new StringBuilder();
                    if (PortUtils.isPortRange(dstPort)) {
                        String startPort = PortUtils.getStartPort(dstPort);
                        String endPort = PortUtils.getEndPort(dstPort);
                        sb.append(startPort).append("-").append(endPort);
                    } else {
                        sb.append(dstPort).append("-").append(dstPort);
                    }
                    portRange.add(sb.toString());
                }
                map.put(protocol, portRange);
            } else {
                map.put(protocol, portRange);
            }
        }
        boolean haveIcmp = false;
        FortiServiceDTO fortiServiceDTO = new FortiServiceDTO();
        for (String key : map.keySet()) {
            List<String> list = (List) map.get(key);
            if (key.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_TCP)) {
                fortiServiceDTO.setProtocol(5);
                fortiServiceDTO.setTcpPortrange(list);
            } else if (key.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_UDP)) {
                fortiServiceDTO.setProtocol(5);
                fortiServiceDTO.setUdpPortrange(list);
            }
            if (key.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
                haveIcmp = true;
            }
        }
        List<FortiServiceDTO> fortiServiceDTOS = new ArrayList<>();
        // 按照规则去构建服务名称
        String serviceName = String.format("%s_SN_%s", serviceDTO.getTicket(), IdGen.getRandomNumberString());
        fortiServiceDTO.setName(serviceName);
        fortiServiceDTOS.add(fortiServiceDTO);
        // 如果有icmp的服务的话，再创建一个对象然后放集合里面循环去调用接口
        if (haveIcmp) {
            FortiServiceDTO icmpFortiDTO = new FortiServiceDTO();
            icmpFortiDTO.setProtocol(1);
            String icmpServiceName = String.format("%s_SN_%s", serviceDTO.getTicket(), IdGen.getRandomNumberString());
            icmpFortiDTO.setName(icmpServiceName);
            fortiServiceDTOS.add(icmpFortiDTO);
        }

        List<ReturnT<String>> resultList = new ArrayList<>();
        for (FortiServiceDTO fortiServiceDT : fortiServiceDTOS) {
            // 构建请求url
            String url = "https://" + requestIp + commonUrl;
            String path = commonPath + serviceDTO.getVsysName() + createServiceObjectPath;
            try {
                // 转换成请求对象参数
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("name", fortiServiceDT.getName());
                itemMap.put("protocol", fortiServiceDT.getProtocol());
                itemMap.put("tcp-portrange", fortiServiceDT.getTcpPortrange());
                itemMap.put("udp-portrange", fortiServiceDT.getUdpPortrange());

                Map<String, Object> parameter = buildParameter(itemMap, "add", path, false, false, session);
                // 执行请求
                log.info("请求飞塔管理平台创建服务对象入参:{},url:{}", JSONObject.toJSONString(parameter), url);
                String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
                JSONObject jsonObject = JSON.parseObject(result);
                if (null != jsonObject) {
                    log.info("请求飞塔管理平台创建服务对象,返回json:{}", jsonObject.toJSONString());
                    String objectNameForError = String.format("Create service Object:%s error", fortiServiceDT.getName());
                    ReturnT<String> returnT = analysisResultForCommon(jsonObject, false, objectNameForError);
                    //  如果请求协议号为5 则协议类型为tcp/udp/sctp 如果协议号为1 则协议类型为icmp
                    resultList.add(returnT);
                } else {
                    log.error("根据设备uuid[{}]请求飞塔平台创建服务对象返回为空", serviceDTO.getDeviceUuid());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求飞塔平台创建服务对象返回为空"));
                }
            } catch (Exception e) {
                log.error("根据设备uuid[{}]请求飞塔平台创建服务对象返回为空,异常原因:{}", serviceDTO.getDeviceUuid(), e);
                return new ReturnT(ReturnT.FAIL_CODE, String.format("Create service Object:%s error,reason is:%s", fortiServiceDT.getName(), e.getMessage()));
            }
        }
        // 将可复用的服务对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : existServiceNames) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }


    @Override
    public ReturnT<String> createTimeData(ManagementPlatformCreateTimeDTO timeDTO, String requestIp, String session) {
        log.info("开始创建时间对象入参:{}", JSON.toJSONString(timeDTO));
        // 构建创建时间对象
        FortiTimeDTO fortiTimeDTO = new FortiTimeDTO();
        // 如果时间为空，则默认返回飞塔默认的时间对象名称给我们平台，方便策略生成的时候传参
        if (AliStringUtils.isEmpty(timeDTO.getStartTime()) || AliStringUtils.isEmpty(timeDTO.getEndTime())) {
            return new ReturnT("always");
        }
        String timeObjectName = String.format("to%s",
                TimeUtils.transformDateFormat(timeDTO.getEndTime(), TimeUtils.EUROPEAN_TIME_FORMAT, TimeUtils.COMMON_TIME_DAY_FORMAT));
        fortiTimeDTO.setName(timeObjectName);
        // 将本系统的时间日期的2021-2-18 转换为飞塔那边所识别的格式2021/2/18
        String[] startTime = timeDTO.getStartTime().split(" ");
        String[] endTime = timeDTO.getEndTime().split(" ");
        String convertStartTime = startTime[0].replaceAll("-", "/");
        String convertEndTime = endTime[0].replaceAll("-", "/");
        String hoursAndMinStart = startTime[1].substring(0, startTime[1].lastIndexOf(":"));
        String hoursAndMinEnd = endTime[1].substring(0, endTime[1].lastIndexOf(":"));

        fortiTimeDTO.setStart(new String[]{hoursAndMinStart, convertStartTime});
        fortiTimeDTO.setEnd(new String[]{hoursAndMinEnd, convertEndTime});
        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        try {
            String path = commonPath + timeDTO.getVsysName() + createTimeObjectPath;

            Map<String, Object> parameter = buildParameter(fortiTimeDTO, "add", path, false, false, session);
            // 执行请求
            log.info("请求飞塔管理平台创建时间对象入参:{},url:{}", parameter.toString(), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);
            if (null != jsonObject) {
                // Object already exists
                log.info("请求飞塔管理平台创建时间对象,返回json:{}", jsonObject.toJSONString());
                // 针对时间对象创建单独处理，如果对象名称已经重复了，则直接返回对象名称
                String objectNameForError = String.format("Create time Object:%s error", fortiTimeDTO.getName());
                return analysisResultForCreateTime(jsonObject, false, objectNameForError, fortiTimeDTO.getName());
            } else {
                log.error("根据设备uuid[{}]请求飞塔平台创建时间对象失败", timeDTO.getDeviceUuid());
                return new ReturnT(ReturnT.FAIL_CODE, "请求飞塔平台创建时间对象失败返回为空");
            }
        } catch (Exception e) {
            log.error("根据url[{}]请求飞塔平台创建时间对象失败", requestIp);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Create time Object:%s error,reason is:%s", fortiTimeDTO.getName(), e.getMessage()));
        }
    }

    @Override
    public ReturnT<String> createPolicyData(ManagementPlatformCreatePolicyDTO policyDTO, String requestIp, String session, boolean ipv6) {
        log.info("开始创建策略入参:{}", JSON.toJSONString(policyDTO));
        // 构建策略对象
        FortiPoliceDTO fortiPoliceDTO = new FortiPoliceDTO();
        fortiPoliceDTO.setName(String.format("%s_PN_%s", policyDTO.getTicket(), IdGen.getRandomNumberString()));
        List<String> srcItfs = new ArrayList<>();
        srcItfs.add(policyDTO.getSrcItfAlias());

        List<String> dstItfs = new ArrayList<>();
        dstItfs.add(policyDTO.getDstItfAlias());

        fortiPoliceDTO.setSrcintf(srcItfs);
        fortiPoliceDTO.setDstintf(dstItfs);
        fortiPoliceDTO.setSrcaddr(policyDTO.getSrcaddrs());
        fortiPoliceDTO.setDstaddr(policyDTO.getDstaddrs());
        fortiPoliceDTO.setService(policyDTO.getServiceNames());
        fortiPoliceDTO.setSchedule(policyDTO.getScheduleNames());
        fortiPoliceDTO.setStatus(1);
        // 飞塔平台安全策略 0:拒绝 1:接受
        if (0 == policyDTO.getAction().getCode()) {
            fortiPoliceDTO.setAction(1);
        } else if (1 == policyDTO.getAction().getCode()) {
            fortiPoliceDTO.setAction(0);
        }
        // 当动作为拒绝的时候，设置违规流量记录的参数为默认不记录
        if (0 == fortiPoliceDTO.getAction()) {
            fortiPoliceDTO.setLogtraffic(0);
        }

        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        String newPolicyPath = null;
        // 调用获取包名 用于api路径的拼接
        ReturnT<String> returnPackage = getPackageName(policyDTO.getHostName(), policyDTO.getVsysName(), requestIp, session);
        if (ReturnT.FAIL_CODE == returnPackage.getCode()) {
            log.error("根据主机名称[{}]匹配包名失败", policyDTO.getHostName());
            return new ReturnT(ReturnT.FAIL_CODE, returnPackage.getMsg());
        }
        if (ipv6) {
            newPolicyPath = commonPath + policyDTO.getVsysName() + createPolicyObjectPath + returnPackage.getData() + "/firewall/policy6";
        } else {
            newPolicyPath = commonPath + policyDTO.getVsysName() + createPolicyObjectPath + returnPackage.getData() + "/firewall/policy";
        }
        try {
            Map<String, Object> parameter = buildParameter(fortiPoliceDTO, "add", newPolicyPath, false, false, session);
            // 执行请求
            log.info("请求飞塔管理平台创建策略入参:{},url:{}", JSONObject.toJSONString(parameter), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);
            if (null != jsonObject) {
                log.info("请求飞塔管理平台创建策略,返回json:{}", jsonObject.toJSONString());
                String objectNameForError = String.format("Create policy Object:%s error", fortiPoliceDTO.getName());
                return analysisResultForCommon(jsonObject, true, objectNameForError);
            } else {
                log.error("根据设备uuid[{}]请求飞塔管理平台创建策略返回为空", policyDTO.getDeviceUuid());
                return new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台创建策略返回为空");
            }
        } catch (Exception e) {
            log.error("根据设备uuid[{}]请求飞塔管理平台创建策略异常,异常原因:{}", policyDTO.getDeviceUuid(), e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Create policy Object:%s error,reason is:%s", fortiPoliceDTO.getName(), e.getMessage()));
        }
    }

    @Override
    public ReturnT<String> deletePolicyData(ManagementPlatformCreatePolicyDTO policyDTO, String requestIp, String session) {
        log.info("开始删除策略入参:{}", JSON.toJSONString(policyDTO));
        // 构建请求url
        String url = "https://" + requestIp + commonUrl;
        try {

            // 调用获取包名 用于api路径的拼接
            ReturnT<String> returnPackage = getPackageName(policyDTO.getHostName(), policyDTO.getVsysName(), requestIp, session);
            if (ReturnT.FAIL_CODE == returnPackage.getCode()) {
                log.error("根据主机名称 [{}]匹配包名失败", policyDTO.getHostName());
                return new ReturnT(ReturnT.FAIL_CODE, returnPackage.getMsg());
            }
            String newPolicyPath = null;
            // 如果ip类型为ipv6则拼接ipv6地址，否则拼接ipv4的地址
            if (null == policyDTO.getIpType()) {
                policyDTO.setIpType(IpTypeEnum.IPV4.getCode());
            }
            if (1 == policyDTO.getIpType()) {
                newPolicyPath = commonPath + policyDTO.getVsysName() + deletePolicyPath + returnPackage.getData() + "/firewall/policy6/" + policyDTO.getPolicyId();
            } else {
                newPolicyPath = commonPath + policyDTO.getVsysName() + deletePolicyPath + returnPackage.getData() + "/firewall/policy/" + policyDTO.getPolicyId();
            }
            // 构建policyName，根据policyName去删除策略
            Map<String, Object> parameter = buildDeletePolicyParameter("delete", newPolicyPath, session);
            // 执行请求
            log.info("请求飞塔管理平台删除策略入参:{},url:{}", JSONObject.toJSONString(parameter), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);
            if (null != jsonObject) {
                log.info("请求飞塔管理平台删除策略,返回json:{}", jsonObject.toJSONString());
                return analysisResultForDeletePolicy(jsonObject, policyDTO.getPolicyId());
            } else {
                log.error("根据策略id[{}]请求飞塔管理平台删除策略返回为空", policyDTO.getPolicyId());
                return new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台创建策略返回为空");
            }
        } catch (Exception e) {
            log.error("根据策略id[{}]请求飞塔管理平台删除策略异常,异常原因:{}", policyDTO.getPolicyId(), e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("delete policyId:%s error,reason is:%s", policyDTO.getPolicyId(), e.getMessage()));
        }
    }

    @Override
    public ReturnT<String> fortinetInstall(ManagementPlatformInstallDTO installDTO, String requestIp, String session) {
        // 构建请求url
        log.info("开始安装策略入参:{}", JSON.toJSONString(installDTO));
        String url = "https://" + requestIp + commonUrl;
        String packageName = null;
        try {
            // 调用获取包名
            ReturnT<String> returnPackage = getPackageName(installDTO.getHostName(), installDTO.getVsysName(), requestIp, session);
            if (ReturnT.FAIL_CODE == returnPackage.getCode()) {
                log.error("根据主机名称[{}]匹配包名失败", installDTO.getHostName());
                return new ReturnT(ReturnT.FAIL_CODE, returnPackage.getMsg());
            }
            // 获取包名
            packageName = returnPackage.getData();
            FortiInstallDTO fortiInstallDTO = new FortiInstallDTO();
            fortiInstallDTO.setAdom(installDTO.getVsysName());
            fortiInstallDTO.setPkg(packageName);
            // 构建policyName，根据policyName去删除策略
            Map<String, Object> parameter = buildParameter(fortiInstallDTO, "exec", installPath, false, false, session);
            // 执行请求
            log.info("请求飞塔管理平台install入参:{},url:{}", JSONObject.toJSONString(parameter), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);
            if (null != jsonObject) {
                log.info("请求飞塔管理平台install,返回json:{}", jsonObject.toJSONString());
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                for (Object object : jsonArray) {
                    JSONObject item = (JSONObject) object;
                    JSONObject statusObject = item.getJSONObject("status");
                    if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                        return new ReturnT(ReturnT.FAIL_CODE, String.format("Install policy Object error,reason is:%s", statusObject.getString("message")));
                    } else {
                        return new ReturnT(jsonObject.toJSONString());
                    }
                }
            } else {
                log.error("根据包名称[{}]请求飞塔管理平台install返回为空", packageName);
                return new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台install返回为空");
            }
        } catch (Exception e) {
            log.error("根据包名称[{}]请求飞塔管理平台install异常,异常原因:{}", packageName, e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Install policy Object error,reason is:%s", e.getMessage()));
        }
        return null;
    }

    @Override
    public ReturnT<String> movePolicyData(ManagementPlatformCreatePolicyDTO policyDTO, String requestIp, String session) {
        // 构建请求url
        log.info("开始移动策略入参:{}", JSON.toJSONString(policyDTO));
        String url = "https://" + requestIp + commonUrl;
        try {
            // 调用获取包名
            ReturnT<String> returnPackage = getPackageName(policyDTO.getHostName(), policyDTO.getVsysName(), requestIp, session);
            if (ReturnT.FAIL_CODE == returnPackage.getCode()) {
                log.error("根据主机名称[{}]匹配包名失败", policyDTO.getHostName());
                return new ReturnT(ReturnT.FAIL_CODE, returnPackage.getMsg());
            }
            // 如果ip类型为ipv6则拼接ipv6地址，否则拼接ipv4的地址
            if (null == policyDTO.getIpType()) {
                policyDTO.setIpType(IpTypeEnum.IPV4.getCode());
            }
            String newPolicyPath = null;
            if (1 == policyDTO.getIpType()) {
                newPolicyPath = commonPath + policyDTO.getVsysName() + "/pkg/" + returnPackage.getData() + "/firewall/policy6/" + policyDTO.getPolicyId();
            } else {
                newPolicyPath = commonPath + policyDTO.getVsysName() + "/pkg/" + returnPackage.getData() + "/firewall/policy/" + policyDTO.getPolicyId();
            }
            if (MoveSeatEnum.FIRST.getCode() == policyDTO.getMoveSeatEnum().getCode() || MoveSeatEnum.LAST.getCode() == policyDTO
                    .getMoveSeatEnum().getCode()) {
                log.info("policyId:{}移动方式为最前面或最后面的时候,不请求飞塔策略移动接口",policyDTO.getPolicyId());
                return new ReturnT(ReturnT.SUCCESS_CODE);
            }
            // 构建policyName，根据policyId去移动策略
            Map<String, Object> parameter = buildMovePolicyParameter("move", newPolicyPath, session,policyDTO
                    .getMoveSeatEnum().getKey(),policyDTO.getTargetPolicyId());
            // 执行请求
            log.info("请求飞塔管理平台移动策略入参:{},url:{}", JSONObject.toJSONString(parameter), url);
            String result = HttpsUtil.httpsPost(url, JSON.toJSONString(parameter));
            JSONObject jsonObject = JSON.parseObject(result);
            if (null != jsonObject) {
                log.info("请求飞塔管理平台移动策略,返回json:{}", jsonObject.toJSONString());
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                for (Object object : jsonArray) {
                    JSONObject item = (JSONObject) object;
                    JSONObject statusObject = item.getJSONObject("status");
                    if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                        return new ReturnT(ReturnT.FAIL_CODE, String.format("Move policyId:%s Object error,reason is:%s", policyDTO.getPolicyId(),statusObject.getString("message")));
                    } else {
                        return new ReturnT(ReturnT.SUCCESS_CODE);
                    }
                }
            } else {
                log.error("请求飞塔管理平台移动策略返回为空,policyId:{}", policyDTO.getPolicyId());
                return new ReturnT(ReturnT.FAIL_CODE, "请求飞塔管理平台移动策略返回为空");
            }
        }catch (Exception e) {
            log.error("请求飞塔管理平台移动策略异常,policyId:{},异常原因:{}", policyDTO.getPolicyId(), e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Move policyId:%s Object error,reason is:%s",policyDTO.getPolicyId(), e.getMessage()));
        }
        return null;
    }


    /**
     * 封裝批量调用的时 结果集的封装
     *
     * @param returnTList
     * @return
     */
    private ReturnT<List<String>> gettReturnT(List<ReturnT<String>> returnTList) {
        boolean allSuccess = true;
        StringBuffer message = new StringBuffer();
        // 遍历返回结果
        List<String> dataList = new ArrayList<>();
        for (ReturnT<String> returnT : returnTList) {
            if (ReturnT.FAIL_CODE == returnT.getCode()) {
                allSuccess = false;
                message.append(PolicyConstants.ADDRESS_SEPERATOR);
                message.append(returnT.getMsg());
            } else {
                dataList.add(returnT.getData());
            }
        }
        if (message.length() > 0) {
            message.deleteCharAt(0);
        }
        if (allSuccess) {
            return new ReturnT(dataList);
        } else {
            return new ReturnT(ReturnT.FAIL_CODE, message.toString());
        }
    }

    /**
     * 解析公共的异常信息
     *
     * @param jsonObject
     * @return
     */
    private ReturnT<String> getReturnFail(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("result");
        for (Object object : jsonArray) {
            JSONObject item = (JSONObject) object;
            JSONObject statusObject = item.getJSONObject("status");
            if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                return new ReturnT(ReturnT.FAIL_CODE, statusObject.getString("message"));
            }
        }
        return null;
    }

    /**
     * 构建飞塔地址DTO
     *
     * @param ticket
     * @param srcOrDstIp
     * @param itf
     * @return
     */
    private FortiAddressMergeDTO buildFortiAddressDTO(String ticket, String srcOrDstIp, String itf, boolean ip6, DeviceDTO deviceDTO
            , AddressPropertyEnum addressPropertyEnum) {
        FortiAddressMergeDTO addressMergeDTO = new FortiAddressMergeDTO();
        List<FortiAddressDTO> fortiAddressDTOs = new ArrayList<>();
        String[] srcOrDstIps = srcOrDstIp.split(",");
        // 定义可以服用对象的集合名称
        List<String> existNames = new ArrayList<>();
        for (String itemSrcOrDstIp : srcOrDstIps) {
            // 调用whale接口，查询地址对象是否存在，如果存在就直接返回，不存在就直接构建
            log.info("调用whale接口查询地址复用对象目标ip:{},设备信息:{},地址属性枚举:{}", itemSrcOrDstIp, JSON.toJSONString(deviceDTO), JSON.toJSONString(addressPropertyEnum));
            ExistAddressObjectDTO existAddressObjectDTO = whaleManager.getCurrentAddressObjectName(itemSrcOrDstIp, deviceDTO, addressPropertyEnum, null,null,null);
            if (null == existAddressObjectDTO) {
                log.info("根据地址:[{}]去查询地址复用接口返回为空,继续构建地址对象参数", itemSrcOrDstIp);
            } else {
                log.info("调用whale接口查询地址复用对象结果:[{}]", JSON.toJSONString(existAddressObjectDTO));
                if (!AliStringUtils.isEmpty(existAddressObjectDTO.getExistName())) {
                    existNames.add(existAddressObjectDTO.getExistName());
                    continue;
                }
            }

            FortiAddressDTO fortiAddressDTO = new FortiAddressDTO();
            fortiAddressDTO.setName(String.format("%s_AN_%s", ticket, IdGen.getRandomNumberString()));
            if(ip6){
                // ip类型为ipv6的时候判断网段，子网，单ip
                if(IpUtils.isIPv6Range(itemSrcOrDstIp)){
                    String[] ipSegment = itemSrcOrDstIp.trim().split("-");
                    fortiAddressDTO.setStartIp(ipSegment[0]);
                    fortiAddressDTO.setEndIp(ipSegment[1]);
                    fortiAddressDTO.setType(1);
                }else if (IpUtils.isIPv6Subnet(itemSrcOrDstIp)){
                    fortiAddressDTO.setSubnet(new String[]{itemSrcOrDstIp});
                    fortiAddressDTO.setType(0);
                }else if(IpUtils.isIPv6(itemSrcOrDstIp)){
                    fortiAddressDTO.setSubnet(new String[]{itemSrcOrDstIp});
                    fortiAddressDTO.setType(0);
                }else {
                    // 如果既不是单ip也不是ip范围，这是设置地址类型为域名
                    fortiAddressDTO.setFqdn(itemSrcOrDstIp);
                    // 飞塔那边ip是域名，ip类型是ipv6的时候，type传4
                    fortiAddressDTO.setType(4);
                }
            }else{
                //ip类型为ipv4这种的ip地址构建判断
                if (IpUtils.isIPRange(itemSrcOrDstIp)) {
                    String[] ipSegment = itemSrcOrDstIp.trim().split("-");
                    fortiAddressDTO.setStartIp(ipSegment[0]);
                    fortiAddressDTO.setEndIp(ipSegment[1]);
                    fortiAddressDTO.setType(1);
                } else if (IpUtils.isIPSegment(itemSrcOrDstIp)) {
                    String ip = IpUtils.getIpFromIpSegment(itemSrcOrDstIp);
                    String maskBit = IpUtils.getMaskBitFromIpSegment(itemSrcOrDstIp);
                    String mask = IpUtils.getMaskByMaskBit(maskBit);
                    fortiAddressDTO.setSubnet(new String[]{ip, mask});
                    fortiAddressDTO.setType(0);
                } else if (IpUtils.isIP(itemSrcOrDstIp)) {
                    // 单ip默认的掩码为32 转换成的ip为255.255.255.255
                    fortiAddressDTO.setSubnet(new String[]{itemSrcOrDstIp, IpUtils.getMaskByMaskBit("32")});
                    fortiAddressDTO.setType(0);
                } else {
                    // 如果既不是单ip也不是ip范围，这是设置地址类型为域名
                    fortiAddressDTO.setFqdn(itemSrcOrDstIp);
                    // 飞塔那边ip是域名，ip类型是ipv4的时候，type传2
                    fortiAddressDTO.setType(2);
                }
            }
            fortiAddressDTO.setAssociatedInterface(itf);
            fortiAddressDTOs.add(fortiAddressDTO);
        }
        addressMergeDTO.setFortiAddressDTOs(fortiAddressDTOs);
        addressMergeDTO.setExistNames(existNames);
        return addressMergeDTO;
    }

    /**
     * 构建发送请求参数
     *
     * @param object
     * @param methodType
     * @param requestPath
     * @param isGetToken
     * @param getPackage
     * @param session
     * @return
     */
    private Map<String, Object> buildParameter(Object object, String methodType, String requestPath,
                                               boolean isGetToken, boolean getPackage, String session) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("method", methodType);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> detailParameter = new HashMap<>();
        detailParameter.put("url", requestPath);
        if (!getPackage) {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(object);
            detailParameter.put("data", jsonObject);
        }
        list.add(detailParameter);
        parameter.put("params", list);
        if (!isGetToken) {
            parameter.put("session", session);
        }
        return parameter;
    }

    /**
     * 构建发送请求参数(删除策略)
     *
     * @param methodType
     * @param requestPath
     * @param session
     * @return
     */
    private Map<String, Object> buildDeletePolicyParameter(String methodType, String requestPath,
                                                           String session) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("method", methodType);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> detailParameter = new HashMap<>();
        detailParameter.put("url", requestPath);
        list.add(detailParameter);
        parameter.put("params", list);
        parameter.put("session", session);
        return parameter;
    }

    /**
     * 构建发送请求参数(删除策略)
     *
     * @param methodType
     * @param requestPath
     * @param session
     * @return
     */
    private Map<String, Object> buildMovePolicyParameter(String methodType, String requestPath,
                                                         String session, String option,String targetPolicy) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("method", methodType);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> detailParameter = new HashMap<>();
        detailParameter.put("url", requestPath);
        detailParameter.put("option",option);
        detailParameter.put("target",targetPolicy);
        list.add(detailParameter);
        parameter.put("params", list);
        parameter.put("session", session);
        return parameter;
    }

    /**
     * 解析公共的返回结果
     *
     * @param resultJson
     * @return
     */
    private ReturnT<String> analysisResultForDeletePolicy(JSONObject resultJson, String policyId) {
        JSONArray jsonArray = resultJson.getJSONArray("result");
        for (Object object : jsonArray) {
            JSONObject item = (JSONObject) object;
            JSONObject statusObject = item.getJSONObject("status");
            if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                String detailErrorMsg = String.format("delete policyId:%s error,reason is:%s", policyId, statusObject.getString("message"));
                return new ReturnT(ReturnT.FAIL_CODE, detailErrorMsg);
            } else {
                return new ReturnT(ReturnT.SUCCESS_CODE);

            }
        }
        return null;
    }

    /**
     * 根据包列表接口返回结果分析获取包名
     *
     * @param deviceName
     * @param resultJson
     * @return
     */
    private ReturnT<String> analysisResultFromPackageName(String deviceName, JSONObject resultJson) {
        JSONArray jsonArray = resultJson.getJSONArray("result");
        for (Object object : jsonArray) {
            JSONObject item = (JSONObject) object;
            JSONObject statusObject = item.getJSONObject("status");
            if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                return new ReturnT(ReturnT.FAIL_CODE, statusObject.getString("message"));
            }

            JSONArray dataJSONArray = item.getJSONArray("data");
            if (null == dataJSONArray) {
                return new ReturnT(ReturnT.FAIL_CODE, "获取包名结果返回对象为空");
            }
            for (Object dataObject : dataJSONArray) {
                JSONObject dataObjectItem = (JSONObject) dataObject;
                JSONArray itemJSONArray = dataObjectItem.getJSONArray("scope member");
                if (null == itemJSONArray) {
                    continue;
                }
                for (Object scopeObject : itemJSONArray) {
                    JSONObject scopeItem = (JSONObject) scopeObject;
                    if (deviceName.equalsIgnoreCase(scopeItem.getString("name"))) {
                        return new ReturnT(dataObjectItem.getString("name"));
                    }
                }
            }
        }
        return null;
    }

    /**
     * 解析公共的返回结果
     *
     * @param resultJson
     * @return
     */
    private ReturnT<String> analysisResultForCommon(JSONObject resultJson, boolean isCreatePolicy, String objectNameForError) {
        JSONArray jsonArray = resultJson.getJSONArray("result");
        for (Object object : jsonArray) {
            JSONObject item = (JSONObject) object;
            JSONObject statusObject = item.getJSONObject("status");
            if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                StringBuffer sb = new StringBuffer();
                sb.append(objectNameForError);
                sb.append(String.format(" reason is:%s", statusObject.getString("message")));
                return new ReturnT(ReturnT.FAIL_CODE, sb.toString());
            }

            JSONObject dataObject = item.getJSONObject("data");
            if (null == dataObject) {
                return new ReturnT(ReturnT.FAIL_CODE, "接口返回的业务实体对象为空");
            }
            // 如果是创建策略返回结果，则直接取policyId对应的字段，否则直接取name对应的字段
            if (isCreatePolicy) {
                return new ReturnT(dataObject.getString("policyid"));
            } else {
                return new ReturnT(dataObject.getString("name"));
            }
        }
        return null;
    }


    /**
     * 解析公共的返回结果
     *
     * @param resultJson
     * @return
     */
    private ReturnT<String> analysisResultForCreateTime(JSONObject resultJson, boolean isCreatePolicy, String objectNameForError, String timeName) {
        JSONArray jsonArray = resultJson.getJSONArray("result");
        for (Object object : jsonArray) {
            JSONObject item = (JSONObject) object;
            JSONObject statusObject = item.getJSONObject("status");
            if (statusObject.getInteger("code") != ReturnCode.POLICY_MSG_OK) {
                // 如果对象名称已经重复了，则直接返回对象名称
                if (-2 == statusObject.getInteger("code") && "Object already exists".equalsIgnoreCase(statusObject.getString("message"))) {
                    return new ReturnT(timeName);
                }
                StringBuffer sb = new StringBuffer();
                sb.append(objectNameForError);
                sb.append(String.format(" reason is:%s", statusObject.getString("message")));
                return new ReturnT(ReturnT.FAIL_CODE, sb.toString());
            }

            JSONObject dataObject = item.getJSONObject("data");
            if (null == dataObject) {
                return new ReturnT(ReturnT.FAIL_CODE, "接口返回的业务实体对象为空");
            }
            // 如果是创建策略返回结果，则直接取policyId对应的字段，否则直接取name对应的字段
            if (isCreatePolicy) {
                return new ReturnT(dataObject.getString("policyid"));
            } else {
                return new ReturnT(dataObject.getString("name"));
            }
        }
        return null;
    }

}
