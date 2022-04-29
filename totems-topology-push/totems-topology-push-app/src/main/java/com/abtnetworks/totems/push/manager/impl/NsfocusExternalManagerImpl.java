package com.abtnetworks.totems.push.manager.impl;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.nsfocus.NsfocusAddressDTO;
import com.abtnetworks.totems.push.dto.nsfocus.NsfocusAddressMergeDTO;
import com.abtnetworks.totems.push.dto.nsfocus.NsfocusPolicyDTO;
import com.abtnetworks.totems.push.dto.nsfocus.NsfocusServiceDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateAddressDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreatePolicyDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateServiceDTO;
import com.abtnetworks.totems.push.manager.NsfocusExternalManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.log4j.Log4j2;

/**
 * 绿盟外部接口api管理类实现类
 *
 * @author lifei
 * @since 2021/3/5
 **/
@Service
@Log4j2
public class NsfocusExternalManagerImpl implements NsfocusExternalManager {

    @Autowired
    WhaleManager whaleManager;

    /***创建ipv4地址对象-子网***/
    @Value("${management-platform-url.nsfocus.create-IPV4-address-network}")
    private String createIPV4AddressNetWorksPath;

    /***创建ipv6地址对象-子网***/
    @Value("${management-platform-url.nsfocus.create-IPV6-address-network}")
    private String createIPV6AddressNetWorksPath;

    /***创建ipv4地址对象-节点***/
    @Value("${management-platform-url.nsfocus.create-IPV4-address-node}")
    private String createIPV4AddressNetWorksNodePath;

    /***创建ipv6地址对象-节点***/
    @Value("${management-platform-url.nsfocus.create-IPV6-address-node}")
    private String createIPV6AddressNetWorksNodePath;

    /***创建地址对象-域名***/

    @Value("${management-platform-url.nsfocus.create-address-url}")
    private String createUrlAddressNetWorksPath;

    /***创建地址对象-ipv4地址池***/
    @Value("${management-platform-url.nsfocus.create-IPV4-address-ippool}")
    private String createIPV4AddressIPPoolNetWorksPath;

    /***创建地址对象-ipv6地址池***/
    @Value("${management-platform-url.nsfocus.create-IPV6-address-ippool}")
    private String createIPV6AddressIPPoolNetWorksPath;

    /***创建地址对象-mac地址***/
    private String createMacAddressNetWorksPath = "/object/networks/mac";

    /***创建地址对象-地址组***/
    @Value("${management-platform-url.nsfocus.create-address-group}")
    private String createAddressGroupNetWorksPath;

    /***创建服务对象***/
    @Value("${management-platform-url.nsfocus.create-service}")
    private String createServicePath;

    /***创建安全策略对象***/
    @Value("${management-platform-url.nsfocus.create-policy}")
    private String createPolicyPath;

    /***创建原nat策略对象路径***/
    @Value("${management-platform-url.nsfocus.create-snat}")
    private String createSNatPolicyPath;

    /***创建目的nat策略对象路径***/
    @Value("${management-platform-url.nsfocus.create-dnat}")
    private String createDNatPolicyPath;

    /***请求头常量***/
    @Value("${management-platform-url.nsfocus.url-common}")
    private String commonUrl;


    @Override
    public ReturnT<List<String>> createIPV4SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO) {
        // 构建请求url
        String requestHead = addressDTO.getWebUrl() + commonUrl;
        log.info("创建ipv4源地址对象入参:{}", JSON.toJSONString(addressDTO));
        // 获取构建地址合并DTO
        NsfocusAddressMergeDTO addressMergeDTOs = buildAddressDTO(addressDTO.getTicket(), addressDTO.getSrcIp(),
            addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum(), addressDTO.getIpType(),addressDTO.getSrcIpSystem(),addressDTO.getDstIpSystem());

        List<ReturnT<String>> resultList = new ArrayList<>();
        for (NsfocusAddressDTO nsfocusAddressDTO : addressMergeDTOs.getNsAddressDTOs()) {
            try {
                // 转换成请求对象参数
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", nsfocusAddressDTO.getName());
                if (0 == nsfocusAddressDTO.getType()) {
                    map.put("net", nsfocusAddressDTO.getNet());
                } else if (1 == nsfocusAddressDTO.getType()) {
                    map.put("ip", nsfocusAddressDTO.getIp());
                } else if (2 == nsfocusAddressDTO.getType()) {
                    map.put("url", nsfocusAddressDTO.getUrl());
                } else if (3 == nsfocusAddressDTO.getType()) {
                    map.put("beginIp", nsfocusAddressDTO.getBeginIp());
                    map.put("endIp", nsfocusAddressDTO.getEndIp());
                }
                // 获取构建的方法请求路径
                String methodRequestPath = buildMethodRequestPath(addressDTO, nsfocusAddressDTO);
                // 通过加密算法得到最终请求url
                String requestUrl = getRequestURl(addressDTO.getUserName(), addressDTO.getPassword(), requestHead, methodRequestPath, JSON.toJSONString(map));
                log.info("请求绿盟接口创建ipv4源地址对象请求入参:{},requestUrl:{}", JSON.toJSONString(map), requestUrl);
                String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(map));
                if (!AliStringUtils.isEmpty(result)) {
                    log.info("请求绿盟接口创建ipv4源地址对象,返回json:{}", JSON.toJSONString(result));
                    String objectNameForError = String.format("Create IPV4 srcAddress Object:%s error", nsfocusAddressDTO.getName());
                    resultList.add(analysisResultForCommon(result, objectNameForError));
                } else {
                    log.error("根据源ip[{}]请求绿盟接口创建ipv4源地址对象为空", addressDTO.getSrcIp());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建ipv4源地址对象为空"));
                }
            } catch (Exception e) {
                log.error("请求绿盟接口创建ipv4源地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV4 srcAddress Object:%s error, reason is:%s", nsfocusAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : addressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }


    @Override
    public ReturnT<List<String>> createIPV4DstAddressData(ManagementPlatformCreateAddressDTO addressDTO) {
        String requestHead = addressDTO.getWebUrl() + commonUrl;
        log.info("创建ipv4目的地址对象入参:{}", JSON.toJSONString(addressDTO));
        // 获取构建地址合并DTO
        NsfocusAddressMergeDTO addressMergeDTOs = buildAddressDTO(addressDTO.getTicket(), addressDTO.getDstIp(),
            addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum(), addressDTO.getIpType(),addressDTO.getSrcIpSystem(),addressDTO.getDstIpSystem());

        List<ReturnT<String>> resultList = new ArrayList<>();
        for (NsfocusAddressDTO nsfocusAddressDTO : addressMergeDTOs.getNsAddressDTOs()) {
            try {
                // 转换成请求对象参数
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", nsfocusAddressDTO.getName());
                if (0 == nsfocusAddressDTO.getType()) {
                    map.put("net", nsfocusAddressDTO.getNet());
                } else if (1 == nsfocusAddressDTO.getType()) {
                    map.put("ip", nsfocusAddressDTO.getIp());
                } else if (2 == nsfocusAddressDTO.getType()) {
                    map.put("url", nsfocusAddressDTO.getUrl());
                } else if (3 == nsfocusAddressDTO.getType()) {
                    map.put("beginIp", nsfocusAddressDTO.getBeginIp());
                    map.put("endIp", nsfocusAddressDTO.getEndIp());
                }
                // 获取构建的方法请求路径
                String methodRequestPath = buildMethodRequestPath(addressDTO, nsfocusAddressDTO);
                // 通过加密算法得到最终请求url
                String requestUrl = getRequestURl(addressDTO.getUserName(), addressDTO.getPassword(), requestHead, methodRequestPath, JSON.toJSONString(map));
                log.info("请求绿盟接口创建ipv4目的地址对象请求入参:{},requestUrl:{}", JSON.toJSONString(map), requestUrl);
                String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(map));
                if (!AliStringUtils.isEmpty(result)) {
                    log.info("请求绿盟接口创建ipv4目的地址对象,返回json:{}", JSON.toJSONString(result));
                    String objectNameForError = String.format("Create IPV4 srcAddress Object:%s error", nsfocusAddressDTO.getName());
                    resultList.add(analysisResultForCommon(result, objectNameForError));
                } else {
                    log.error("根据地址目的ip[{}]请求绿盟接口创建ipv4目的地址对象为空", addressDTO.getDstIp());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建ipv4目的地址对象为空"));
                }
            } catch (Exception e) {
                log.error("请求绿盟接口创建ipv4目的地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV4 dstAddress Object:%s error, reason is:%s", nsfocusAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : addressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }

    @Override
    public ReturnT<List<String>> createIPV6SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO) {
        String requestHead = addressDTO.getWebUrl() + commonUrl;
        log.info("创建ipv6源地址对象入参:{}", JSON.toJSONString(addressDTO));
        // 获取构建地址合并DTO
        NsfocusAddressMergeDTO addressMergeDTOs = buildAddressDTO(addressDTO.getTicket(), addressDTO.getSrcIp(),
            addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum(), addressDTO.getIpType(),addressDTO.getSrcIpSystem(),addressDTO.getDstIpSystem());

        List<ReturnT<String>> resultList = new ArrayList<>();
        for (NsfocusAddressDTO nsfocusAddressDTO : addressMergeDTOs.getNsAddressDTOs()) {
            try {
                // 转换成请求对象参数
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", nsfocusAddressDTO.getName());
                if (0 == nsfocusAddressDTO.getType()) {
                    map.put("net", nsfocusAddressDTO.getNet());
                } else if (1 == nsfocusAddressDTO.getType()) {
                    map.put("ip", nsfocusAddressDTO.getIp());
                } else if (2 == nsfocusAddressDTO.getType()) {
                    map.put("url", nsfocusAddressDTO.getUrl());
                } else if (3 == nsfocusAddressDTO.getType()) {
                    map.put("beginIp", nsfocusAddressDTO.getBeginIp());
                    map.put("endIp", nsfocusAddressDTO.getEndIp());
                }
                // 获取构建的方法请求路径
                String methodRequestPath = buildMethodRequestPath(addressDTO, nsfocusAddressDTO);
                // 通过加密算法得到最终请求url
                String requestUrl = getRequestURl(addressDTO.getUserName(), addressDTO.getPassword(), requestHead, methodRequestPath, JSON.toJSONString(map));
                log.info("请求绿盟接口创建ipv6源地址对象请求入参:{},requestUrl:{}", JSON.toJSONString(map), requestUrl);
                String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(map));
                if (!AliStringUtils.isEmpty(result)) {
                    log.info("请求绿盟接口创建ipv6源地址对象,返回json:{}", JSON.toJSONString(result));
                    String objectNameForError = String.format("Create IPV6 srcAddress Object:%s error", nsfocusAddressDTO.getName());
                    resultList.add(analysisResultForCommon(result, objectNameForError));
                } else {
                    log.error("根据地址源ip[{}]请求绿盟接口创建ipv6源地址对象为空", addressDTO.getSrcIp());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建ipv6源地址对象为空"));
                }
            } catch (Exception e) {
                log.error("请求绿盟接口创建ipv4目的地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV6 srcAddress Object:%s error, reason is:%s", nsfocusAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : addressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }

    @Override
    public ReturnT<List<String>> createIPV6DstAddressData(ManagementPlatformCreateAddressDTO addressDTO) {
        String requestHead = addressDTO.getWebUrl() + commonUrl;

        log.info("创建ipv6目的地址对象入参:{}", JSON.toJSONString(addressDTO));
        // 获取构建地址合并DTO
        NsfocusAddressMergeDTO addressMergeDTOs = buildAddressDTO(addressDTO.getTicket(), addressDTO.getDstIp(),
            addressDTO.getDeviceDTO(), addressDTO.getAddressPropertyEnum(), addressDTO.getIpType(),addressDTO.getSrcIpSystem(),addressDTO.getDstIpSystem());

        List<ReturnT<String>> resultList = new ArrayList<>();
        for (NsfocusAddressDTO nsfocusAddressDTO : addressMergeDTOs.getNsAddressDTOs()) {
            try {
                // 转换成请求对象参数
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", nsfocusAddressDTO.getName());
                if (0 == nsfocusAddressDTO.getType()) {
                    map.put("net", nsfocusAddressDTO.getNet());
                } else if (1 == nsfocusAddressDTO.getType()) {
                    map.put("ip", nsfocusAddressDTO.getIp());
                } else if (2 == nsfocusAddressDTO.getType()) {
                    map.put("url", nsfocusAddressDTO.getUrl());
                } else if (3 == nsfocusAddressDTO.getType()) {
                    map.put("beginIp", nsfocusAddressDTO.getBeginIp());
                    map.put("endIp", nsfocusAddressDTO.getEndIp());
                }
                // 获取构建的方法请求路径
                String methodRequestPath = buildMethodRequestPath(addressDTO, nsfocusAddressDTO);
                // 通过加密算法得到最终请求url
                String requestUrl = getRequestURl(addressDTO.getUserName(), addressDTO.getPassword(), requestHead, methodRequestPath, JSON.toJSONString(map));
                log.info("请求绿盟接口创建ipv6目的地址对象请求入参:{},requestUrl:{}", JSON.toJSONString(map), requestUrl);
                String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(map));
                if (!AliStringUtils.isEmpty(result)) {
                    log.info("请求绿盟接口创建ipv6目的地址对象,返回json:{}", JSON.toJSONString(result));
                    String objectNameForError = String.format("Create IPV6 srcAddress Object:%s error", nsfocusAddressDTO.getName());
                    resultList.add(analysisResultForCommon(result, objectNameForError));
                } else {
                    log.error("根据地址目的ip[{}]请求绿盟接口创建ipv6目的地址对象为空", addressDTO.getDstIp());
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建ipv6目的地址对象为空"));
                }
            } catch (Exception e) {
                log.error("请求绿盟接口创建ipv6目的地址对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format(" Create IPV6 dstAddress Object:%s error, reason is:%s", nsfocusAddressDTO.getName(), e.getMessage())));
            }
        }
        // 将可复用的地址对象集合添加到结果集合中
        List<ReturnT<String>> existResultList = new ArrayList<>();
        for (String existName : addressMergeDTOs.getExistNames()) {
            existResultList.add(new ReturnT(existName));
        }
        resultList.addAll(existResultList);
        return gettReturnT(resultList);
    }

    @Override
    public ReturnT<String> createAddressDataGroup(ManagementPlatformCreateAddressDTO addressDTO) {
        String requestHead = addressDTO.getWebUrl() + commonUrl;

        log.info("创建地址组对象入参:{}", JSON.toJSONString(addressDTO));
        NsfocusAddressDTO nsfocusAddressDTO = new NsfocusAddressDTO();
        String addressObjectName =
            AddressPropertyEnum.SRC.getName().equals(addressDTO.getAddressPropertyEnum().getName())
                ? addressDTO.getPostSrcIpSystem() : addressDTO.getPostDstIpSystem();
        nsfocusAddressDTO.setName(
            StringUtils.isBlank(addressObjectName) ? String.format("%s_AN_%s", addressDTO.getTicket(), IdGen.getRandomNumberString())
                : String.format("%s_group_%s", addressObjectName, DateUtils.getDate().replace("-", "").substring(2)));
        nsfocusAddressDTO.setInclude(addressDTO.getAddrGroupId());
        // 适配拼接地址组这种类型的请求路径
        nsfocusAddressDTO.setType(4);
        try {
            // 获取构建的方法请求路径
            String methodRequestPath = buildMethodRequestPath(addressDTO, nsfocusAddressDTO);
            // 通过加密算法得到最终请求url
            String requestUrl = getRequestURl(addressDTO.getUserName(), addressDTO.getPassword(), requestHead,
                methodRequestPath, JSON.toJSONString(nsfocusAddressDTO));
            log.info("请求绿盟接口创建地址组对象请求入参:{},requestUrl:{}", JSON.toJSONString(nsfocusAddressDTO), requestUrl);
            String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(nsfocusAddressDTO));
            if (!AliStringUtils.isEmpty(result)) {
                log.info("请求绿盟接口创建地址组对象,返回json:{}", JSON.toJSONString(result));
                String objectNameForError =
                    String.format("Create addressGroup Object:%s error", nsfocusAddressDTO.getName());
                return analysisResultForCommon(result, objectNameForError);
            } else {
                log.error("根据地址组的ip[{}]请求绿盟接口创建地址组对象为空", addressDTO.getAddrGroupId());
                return new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建地址组对象为空");
            }
        } catch (Exception e) {
            log.error("请求绿盟接口创建地址组对象异常,异常原因:{}", e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format(" Create addressGroup object:%s error, reason is:%s",
                nsfocusAddressDTO.getName(), e.getMessage()));
        }
    }

    @Override
    public ReturnT<List<String>> createServiceData(ManagementPlatformCreateServiceDTO serviceDTO) {
        String requestHead = serviceDTO.getWebUrl() + commonUrl;

        log.info("开始创建服务对象入参:{}", JSON.toJSONString(serviceDTO));
        List<ServiceDTO> serviceDTOS = serviceDTO.getServiceList();
        // 如果服务为空，则默认返回飞塔默认的服务对象名称给我们平台，方便策略生成的时候传参
        if (null == serviceDTOS) {
            ServiceDTO serviceDTO1 = new ServiceDTO();
            serviceDTO1.setProtocol("any");
            List<ServiceDTO> newServiceDTOS = new ArrayList<>();
            newServiceDTOS.add(serviceDTO1);
            serviceDTOS = newServiceDTOS;
        }
        List<ReturnT<String>> resultList = new ArrayList<>();
        // 定义可复用的服务名称集合
        List<String> existServiceNames = new ArrayList<>();
        // 定义请求参数
        List<NsfocusServiceDTO> nsfocusServiceDTOS = new ArrayList<>();

        // 遍历服务中的所有协议和端口，批量生成服务对象
        for (ServiceDTO serDTO : serviceDTOS) {
            // 查询服务是否能否复用
            if (queryCurrentService(serviceDTO, existServiceNames, serDTO,"整体")){
                continue;
            }
            log.info("目的端口:{} 服务整体复用没有查询到,进入服务离散查询---------",serDTO.getDstPorts());
            String[] dstPorts = serDTO.getDstPorts().split(",");
            for (String itemmDstPost : dstPorts){
                serDTO.setDstPorts(itemmDstPost);
                if (queryCurrentService(serviceDTO, existServiceNames, serDTO,"离散")){
                    continue;
                }else{
                    NsfocusServiceDTO needCreateServiceDto = new NsfocusServiceDTO();
                    buildNeedCreateService(serviceDTO, nsfocusServiceDTOS, serDTO, needCreateServiceDto);
                }
            }
        }

        // 当需要请求的对象集合不为空的时候才去请求，如果为空说明都是可以共用的服务，直接返回可以复用的服务名称就可以了
        if(nsfocusServiceDTOS.size() > 0){
            try {
                // 通过加密算法得到最终请求url
                String requestUrl = getRequestURl(serviceDTO.getUserName(), serviceDTO.getPassword(), requestHead, createServicePath, JSON.toJSONString(nsfocusServiceDTOS));
                // 执行请求
                log.info("请求绿盟接口创建服务对象入参:{},requestUrl:{}", JSONObject.toJSONString(nsfocusServiceDTOS), requestUrl);
                String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(nsfocusServiceDTOS));
                if (!AliStringUtils.isEmpty(result)) {
                    log.info("请求绿盟接口创建服务对象返回参数:{}", JSON.toJSONString(result));
                    String objectNameForError = String.format("Batch create service Object error");
                    resultList.addAll(batchAnalysisResultForCommon(result, objectNameForError));
                } else {
                    log.error("请求绿盟接口创建服务对象返回为空!");
                    resultList.add(new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建服务对象返回参数为空"));
                }
            } catch (Exception e) {
                log.error("请求绿盟接口创建服务对象异常,异常原因:{}", e);
                resultList.add(new ReturnT(ReturnT.FAIL_CODE, String.format("Batch create service Object error, reason is %s", e.getMessage())));
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

    /**
     * 构建需要创建的服务
     * @param serviceDTO
     * @param nsfocusServiceDTOS
     * @param serDTO
     * @param nsfocusServiceDTO
     */
    private void buildNeedCreateService(ManagementPlatformCreateServiceDTO serviceDTO, List<NsfocusServiceDTO> nsfocusServiceDTOS, ServiceDTO serDTO, NsfocusServiceDTO nsfocusServiceDTO) {
        String protocol = ProtocolUtils.getProtocolByString(serDTO.getProtocol());
        // 协议类型不为icmp的才会有目的端口
        if (!protocol.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)) {
            // 如果选择了协议但是没有填写端口则设置默认值
            if (AliStringUtils.isEmpty(serDTO.getDstPorts())
                || PolicyConstants.POLICY_STR_VALUE_ANY.equalsIgnoreCase(serDTO.getDstPorts())) {
                nsfocusServiceDTO.setDstPorts(PolicyConstants.PORT_ANY);
            } else {
                // 这个地方不进行多个端口根据逗号切分，因为绿盟支持"80,81-82"作为端口参数的传入
                nsfocusServiceDTO.setDstPorts(serDTO.getDstPorts());
            }
            nsfocusServiceDTO.setSrcPorts(PolicyConstants.PORT_ANY);
            nsfocusServiceDTO.setProto(protocol.toLowerCase());
        } else {
            // TODO 待确认，我们系统的icmp协议是不是对应的绿盟ip
            nsfocusServiceDTO.setProto("ip");
            nsfocusServiceDTO.setIpProto("xx");
        }
        nsfocusServiceDTO.setName(String.format("%s_SN_%s", serviceDTO.getTicket(), IdGen.getRandomNumberString()));
        nsfocusServiceDTOS.add(nsfocusServiceDTO);
    }

    /**
     * 查询复用逻辑
     * @param serviceDTO
     * @param existServiceNames
     * @param serDTO
     * @param queryType
     * @return
     */
    private boolean queryCurrentService(ManagementPlatformCreateServiceDTO serviceDTO, List<String> existServiceNames, ServiceDTO serDTO,String queryType) {
        List<ServiceDTO> itemArray = new ArrayList<>();
        itemArray.add(serDTO);
        log.info("服务{}复用查询--调用whale接口查询服务对象复用，服务参数:{},设备数据:{},长连接:{}",queryType, itemArray,
            JSON.toJSONString(serviceDTO.getDeviceForExistObjDTO()), serviceDTO.getIdleTimeout());
        String itemServiceName = whaleManager.getCurrentServiceObjectNameForNsfocus(itemArray,
            serviceDTO.getDeviceForExistObjDTO(), serviceDTO.getIdleTimeout());
        log.info("服务{}复用查询--调用whale接口查询服务对象返回结果:{}",queryType,itemServiceName);
        if (StringUtils.isNotBlank(itemServiceName)) {
            // whale返回的复用对象名称是id拼上名称的格式: 120018_test997_AN_8212
            String[] existNammes = itemServiceName.split("_");
            // 取第一个对象,绿盟创建服务的时候如果地址已经存在，则复用其id
            existServiceNames.add(existNammes[0]);
            return true;
        }
        return false;
    }

    @Override
    public ReturnT<String> createSecurityPolicyData(ManagementPlatformCreatePolicyDTO policyDTO) {
        String requestHead = policyDTO.getWebUrl() + commonUrl;

        log.info("开始创建安全策略入参:{}", JSON.toJSONString(policyDTO));
        // 构建策略对象
        NsfocusPolicyDTO nsfocusPolicyDTO = new NsfocusPolicyDTO();
        nsfocusPolicyDTO.setName(String.format("%s_PN_%s", policyDTO.getTicket(), IdGen.getRandomNumberString()));
        nsfocusPolicyDTO.setSrcObject(convertListToString(policyDTO.getSrcaddrs()));
        nsfocusPolicyDTO.setDstObject(convertListToString(policyDTO.getDstaddrs()));
        nsfocusPolicyDTO.setSrcZone(policyDTO.getSrcZone());
        nsfocusPolicyDTO.setDstZone(policyDTO.getDstZone());
        nsfocusPolicyDTO.setService(convertListToString(policyDTO.getServiceNames()));
        nsfocusPolicyDTO.setTime(convertListToString(policyDTO.getScheduleNames()));
        nsfocusPolicyDTO.setSessionTimeout(null == policyDTO.getIdleTimeout() ? "" : policyDTO.getIdleTimeout().toString());
        if (0 == policyDTO.getAction().getCode()) {
            nsfocusPolicyDTO.setAction("accept");
        } else {
            nsfocusPolicyDTO.setAction("drop");
        }
        nsfocusPolicyDTO.setSessionTimeout(null == policyDTO.getIdleTimeout() ? "" : String.valueOf(policyDTO.getIdleTimeout()));

        try {
            // 通过加密算法得到最终请求url
            String requestUrl = getRequestURl(policyDTO.getUserName(), policyDTO.getPassword(), requestHead, createPolicyPath, JSON.toJSONString(nsfocusPolicyDTO));
            // 执行请求
            log.info("请求绿盟接口创建安全策略接口入参:{},requestUrl:{}", JSONObject.toJSONString(nsfocusPolicyDTO), requestUrl);
            String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(nsfocusPolicyDTO));
            if (!AliStringUtils.isEmpty(result)) {
                log.info("请求绿盟接口创建安全策略接口返回参数:{}", JSON.toJSONString(result));
                String objectNameForError = String.format("Create security policy Object error");
                return analysisPolicyResult(result, objectNameForError);
            } else {
                log.error("请求绿盟接口创建服务对象返回为空!");
                return new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建服务对象返回参数为空");
            }
        } catch (Exception e) {
            log.error("请求绿盟接口创建安全策略接口异常,异常原因:{}", e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Create Security Policy Object error, reason is %s", e.getMessage()));

        }
    }

    @Override
    public ReturnT<String> createSNatPolicyData(ManagementPlatformCreatePolicyDTO policyDTO) {
        String requestHead = policyDTO.getWebUrl() + commonUrl;

        log.info("开始创建源nat策略入参:{}", JSON.toJSONString(policyDTO));
        Map<String,Object>  requestMap = new LinkedHashMap<>();
        requestMap.put("name",String.format("%s_PN_%s", policyDTO.getTicket(), IdGen.getRandomNumberString()));
        requestMap.put("srcZone",policyDTO.getSrcZone());
        requestMap.put("dstZone",policyDTO.getDstZone());
        requestMap.put("srcNet",convertListToString(policyDTO.getSrcaddrs()));
        requestMap.put("dstNet",convertListToString(policyDTO.getDstaddrs()));
        requestMap.put("service",convertListToString(policyDTO.getServiceNames()));
        requestMap.put("interface",policyDTO.getDstItf());//这里的接口名称取和平台名称对应的目的接口
        if(CollectionUtils.isNotEmpty(policyDTO.getPostSrcaddrs())){
            requestMap.put("natIp",convertListToString(policyDTO.getPostSrcaddrs()));// 转换后的源地址即为nat对象
        }
        try {
            // 通过加密算法得到最终请求url
            String requestUrl = getRequestURl(policyDTO.getUserName(), policyDTO.getPassword(), requestHead, createSNatPolicyPath, JSON.toJSONString(requestMap));
            // 执行请求
            log.info("请求绿盟接口创建源nat策略接口入参:{},requestUrl:{}", JSONObject.toJSONString(requestMap), requestUrl);
            String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(requestMap));
            if (!AliStringUtils.isEmpty(result)) {
                log.info("请求绿盟接口创建源nat策略接口返回参数:{}", JSON.toJSONString(result));
                String objectNameForError = String.format("Create SNat policy Object error");
                return analysisPolicyResult(result, objectNameForError);
            } else {
                log.error("请求绿盟接口创建源nat策略对象返回为空!");
                return new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建源nat策略对象返回参数为空");
            }
        } catch (Exception e) {
            log.error("请求绿盟接口创建源nat策略接口异常,异常原因:{}", e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Create SNat Policy Object error, reason is %s", e.getMessage()));
        }
    }

    @Override
    public ReturnT<String> createDNatPolicyData(ManagementPlatformCreatePolicyDTO policyDTO) {
        String requestHead = policyDTO.getWebUrl() + commonUrl;

        log.info("开始创建目的nat策略入参:{}", JSON.toJSONString(policyDTO));
        Map<String,Object>  requestMap = new LinkedHashMap<>();
        requestMap.put("name",String.format("%s_PN_%s", policyDTO.getTicket(), IdGen.getRandomNumberString()));
        requestMap.put("interface",policyDTO.getSrcItf());// 这里的接口名称取和平台名称对应的源接口
        requestMap.put("intraObject",convertListToString(policyDTO.getPostDstaddrs()));
        requestMap.put("extraObject",convertListToString(policyDTO.getDstaddrs()));

        if(CollectionUtils.isNotEmpty(policyDTO.getServiceList())){
            requestMap.put("type","pat");
            String protocol = ProtocolUtils.getProtocolByString(policyDTO.getServiceList().get(0).getProtocol());
            requestMap.put("proto",protocol.toLowerCase());
            requestMap.put("extraPort",policyDTO.getServiceList().get(0).getDstPorts());// 转换前的源地址为外部接口
        }
        if(CollectionUtils.isNotEmpty(policyDTO.getPostServiceList())){
            requestMap.put("intraPort",policyDTO.getPostServiceList().get(0).getDstPorts());//这里的内部接口为转换后的端口
        }
        try {
            // 通过加密算法得到最终请求url
            String requestUrl = getRequestURl(policyDTO.getUserName(), policyDTO.getPassword(), requestHead, createDNatPolicyPath, JSON.toJSONString(requestMap));
            // 执行请求
            log.info("请求绿盟接口创建目的nat策略接口入参:{},requestUrl:{}", JSONObject.toJSONString(requestMap), requestUrl);
            String result = HttpsUtil.httpsPost(requestUrl, JSON.toJSONString(requestMap));
            if (!AliStringUtils.isEmpty(result)) {
                log.info("请求绿盟接口创建目的nat策略接口返回参数:{}", JSON.toJSONString(result));
                String objectNameForError = String.format("Create DNat policy Object error");
                return analysisPolicyResult(result, objectNameForError);
            } else {
                log.error("请求绿盟接口创建目的nat策略对象返回为空!");
                return new ReturnT(ReturnT.FAIL_CODE, "请求绿盟接口创建目的nat策略对象返回参数为空");
            }
        } catch (Exception e) {
            log.error("请求绿盟接口创建目的nat策略接口异常,异常原因:{}", e);
            return new ReturnT(ReturnT.FAIL_CODE, String.format("Create DNat Policy Object error, reason is %s", e.getMessage()));
        }
    }

    /**
     * 转换集合成String
     *
     * @param targetList
     * @return
     */
    private String convertListToString(List<String> targetList) {
        if (CollectionUtils.isEmpty(targetList)) {
            return "any";
        }
        StringBuffer sb = new StringBuffer();
        for (String item : targetList) {
            sb.append(",");
            sb.append(item);
        }
        if (sb.length() > 0) {
            sb = sb.deleteCharAt(0);
        }
        return sb.toString();
    }


    /**
     * 构建地址对象
     *
     * @param ticket
     * @param srcOrDstIp
     * @param deviceDTO
     * @param addressPropertyEnum
     * @return
     */
    private NsfocusAddressMergeDTO buildAddressDTO(String ticket, String srcOrDstIp, DeviceDTO deviceDTO
            , AddressPropertyEnum addressPropertyEnum, Integer ipType,String srcIpSystem,String dstIpSystem) {
        NsfocusAddressMergeDTO addressMergeDTO = new NsfocusAddressMergeDTO();
        List<NsfocusAddressDTO> nsAddressDTOs = new ArrayList<>();
        if(StringUtils.isBlank(srcOrDstIp)){
            // 如果地址为空则传默认值
            srcOrDstIp = "0.0.0.0/0";
        }
        // 地址对象名称 如果页面上填写了 就用填写的字段去创建。没有填写还是以命名规则去命名
        String addressObjectNamme = AddressPropertyEnum.SRC.getName().equals(addressPropertyEnum.getName()) ? srcIpSystem : dstIpSystem;
        String[] srcOrDstIps = srcOrDstIp.split(",");
        // 定义可以服用对象的集合名称
        List<String> existNames = new ArrayList<>();
        log.info("地址整体复用查询--工单:{},{}地址整体复用查询入参:{}",ticket,addressPropertyEnum.getName(),srcOrDstIp);
        ExistAddressObjectDTO existDstAddressObjectDTO = whaleManager.getCurrentAddressObjectName(srcOrDstIp, deviceDTO, addressPropertyEnum, null,null,null);
        log.info("地址整体复用查询--工单:{},{}地址整体复用查询返回结果:[{}]", ticket,addressPropertyEnum.getName(),JSON.toJSONString(existDstAddressObjectDTO));
        if (null == existDstAddressObjectDTO) {
            log.info("地址整体复用查询--工单:{},{}地址整体复用查询返回为空，继续地址离散复用查询", ticket,addressPropertyEnum.getName());
        } else {
            if (!AliStringUtils.isEmpty(existDstAddressObjectDTO.getExistName())) {
                // whale返回的复用对象名称是id拼上名称的格式: 120018_test997_AN_8212
                String[] existNammes = existDstAddressObjectDTO.getExistName().split("_");
                // 取第一个对象,绿盟创建服务的时候如果地址已经存在，则复用其id
                existNames.add(existNammes[0]);
                addressMergeDTO.setExistNames(existNames);
                addressMergeDTO.setNsAddressDTOs(nsAddressDTOs);
                return addressMergeDTO;
            }
        }
        int index = 1;
        // 如果整体查询不到再去离散查询
        for (String itemSrcOrDstIp : srcOrDstIps) {
            // 调用whale接口，查询地址对象是否存在，如果存在就直接返回，不存在就直接构建
            log.info("地址离散复用查询--调用whale接口查询地址复用对象目标ip:{},设备信息:{},地址属性枚举:{}", itemSrcOrDstIp, JSON.toJSONString(deviceDTO), JSON.toJSONString(addressPropertyEnum));
            ExistAddressObjectDTO existAddressObjectDTO = whaleManager.getCurrentAddressObjectName(itemSrcOrDstIp, deviceDTO, addressPropertyEnum, null,null,null);
            if (null == existAddressObjectDTO) {
                log.info("地址离散复用查询--根据地址:[{}]去查询地址复用接口返回为空,继续构建地址对象参数", itemSrcOrDstIp);
            } else {
                log.info("地址离散复用查询--调用whale接口查询地址复用对象结果:[{}]", JSON.toJSONString(existAddressObjectDTO));
                if (!AliStringUtils.isEmpty(existAddressObjectDTO.getExistName())) {
                    // whale返回的复用对象名称是id拼上名称的格式: 120018_test997_AN_8212
                    String[] existNammes = existAddressObjectDTO.getExistName().split("_");
                    // 取第一个对象,绿盟创建服务的时候如果地址已经存在，则复用其id
                    existNames.add(existNammes[0]);
                    continue;
                }
            }

            NsfocusAddressDTO addressDTO = new NsfocusAddressDTO();
            // 创建地址对象名称的名称 如果页面上填写了
            // 就用填写的字段去创建(且拼进去当前日期的年月日，例如对象名称填写的SRC_OBJECT,则最后的对象名称为:SRC_OBJECT_210611)。没有填写还是以命名规则去命名
            addressDTO.setName(StringUtils.isBlank(addressObjectNamme)
                ? String.format("%s_AN_%s", ticket, IdGen.getRandomNumberString()) : String.format("%s_%d_%s",
                    addressObjectNamme, index, DateUtils.getDate().replace("-", "").substring(2)));
            if (0 == ipType) {
                if (IpUtils.isIPRange(itemSrcOrDstIp)) {
                    String[] ipSegment = itemSrcOrDstIp.trim().split("-");
                    addressDTO.setBeginIp(ipSegment[0]);
                    addressDTO.setEndIp(ipSegment[1]);
                    addressDTO.setType(3);
                } else if (IpUtils.isIPSegment(itemSrcOrDstIp)) {
                    addressDTO.setNet(itemSrcOrDstIp);
                    addressDTO.setType(0);
                } else if (IpUtils.isIP(itemSrcOrDstIp)) {
                    // 如果是单ip，直接setIp
                    addressDTO.setIp(itemSrcOrDstIp);
                    addressDTO.setType(1);
                } else {
                    // 如果既不是单ip也不是ip范围，这是设置地址类型为域名
                    addressDTO.setUrl(itemSrcOrDstIp);
                    addressDTO.setType(2);
                }
            } else if (1 == ipType) {
                if (IP6Utils.isIPv6Range(itemSrcOrDstIp)) {
                    String[] ipSegment = itemSrcOrDstIp.trim().split("-");
                    addressDTO.setBeginIp(ipSegment[0]);
                    addressDTO.setEndIp(ipSegment[1]);
                    addressDTO.setType(3);
                } else if (IP6Utils.isIPv6Subnet(itemSrcOrDstIp)) {
                    addressDTO.setNet(itemSrcOrDstIp);
                    addressDTO.setType(0);
                } else if (IP6Utils.isIPv6(itemSrcOrDstIp)) {
                    // 如果是单ip，直接setIp
                    addressDTO.setIp(itemSrcOrDstIp);
                    addressDTO.setType(1);
                } else {
                    // 如果既不是单ip也不是ip范围，这是设置地址类型为域名
                    addressDTO.setUrl(itemSrcOrDstIp);
                    addressDTO.setType(2);
                }
            }
            index++;
            nsAddressDTOs.add(addressDTO);
        }
        addressMergeDTO.setExistNames(existNames);
        addressMergeDTO.setNsAddressDTOs(nsAddressDTOs);
        return addressMergeDTO;
    }

    /**
     * 构建获取请求路径
     *
     * @param addressDTO
     * @param nsfocusAddressDTO
     */
    private String buildMethodRequestPath(ManagementPlatformCreateAddressDTO addressDTO, NsfocusAddressDTO nsfocusAddressDTO) {
        String requestPath = null;
        if (0 == nsfocusAddressDTO.getType() && 0 == addressDTO.getIpType()) {
            requestPath = createIPV4AddressNetWorksPath;
        } else if (0 == nsfocusAddressDTO.getType() && 1 == addressDTO.getIpType()) {
            requestPath = createIPV6AddressNetWorksPath;
        } else if (1 == nsfocusAddressDTO.getType() && 0 == addressDTO.getIpType()) {
            requestPath = createIPV4AddressNetWorksNodePath;
        } else if (1 == nsfocusAddressDTO.getType() && 1 == addressDTO.getIpType()) {
            requestPath = createIPV6AddressNetWorksNodePath;
        } else if (2 == nsfocusAddressDTO.getType()) {
            requestPath = createUrlAddressNetWorksPath;
        } else if (3 == nsfocusAddressDTO.getType() && 0 == addressDTO.getIpType()) {
            requestPath = createIPV4AddressIPPoolNetWorksPath;
        } else if (3 == nsfocusAddressDTO.getType() && 1 == addressDTO.getIpType()) {
            requestPath = createIPV6AddressIPPoolNetWorksPath;
        }else if (4 == nsfocusAddressDTO.getType()){
            requestPath = createAddressGroupNetWorksPath;
        }
        return requestPath;
    }

    /**
     * 解析公共的返回结果
     *
     * @param result
     * @return
     */
    private ReturnT<String> analysisResultForCommon(String result, String objectNameForError) {
        JSONArray jsonArray = JSON.parseArray(result);
        JSONObject item = (JSONObject) jsonArray.get(0);
        if (item.getInteger("errorCode") != ReturnCode.POLICY_MSG_OK) {
            StringBuffer sb = new StringBuffer();
            sb.append(objectNameForError);
            sb.append(String.format(" reason is:%s", item.getString("errMsg")));
            return new ReturnT(ReturnT.FAIL_CODE, sb.toString());
        }
        // 直接取name对应的字段
        return new ReturnT(item.getString("id"));
    }

    /**
     * 解析创建策略公共返回结果
     *
     * @param result
     * @return
     */
    private ReturnT<String> analysisPolicyResult(String result, String objectNameForError) {
        JSONArray jsonArray = JSON.parseArray(result);
        JSONObject item = (JSONObject) jsonArray.get(0);
        if (item.getInteger("errorCode") != ReturnCode.POLICY_MSG_OK) {
            StringBuffer sb = new StringBuffer();
            sb.append(objectNameForError);
            sb.append(String.format(" reason is:%s", item.getString("errorMsg")));
            return new ReturnT(ReturnT.FAIL_CODE, sb.toString());
        }
        // 直接取name对应的字段
        return new ReturnT(item.getString("id"));
    }




    /**
     * 批量解析解析公共的返回结果
     *
     * @param result
     * @return
     */
    private List<ReturnT<String>> batchAnalysisResultForCommon(String result, String objectNameForError) {
        List<ReturnT<String>> returnTList = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(result);
        for (Object object : jsonArray) {
            JSONObject item = (JSONObject) object;
            if (item.getInteger("errorCode") != ReturnCode.POLICY_MSG_OK) {
                StringBuffer sb = new StringBuffer();
                sb.append(objectNameForError);
                sb.append(String.format(" reason is:%s", item.getString("errMsg")));
                returnTList.add(new ReturnT(ReturnT.FAIL_CODE, sb.toString()));
            } else {
                // 直接取name对应的字段
                returnTList.add(new ReturnT(item.getString("id")));
            }
        }
        return returnTList;
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
     * 获取请求url(包含了签名算法)
     *
     * @param accountId
     * @param token
     * @param restfulHead
     * @param methodRequestPath
     * @param messageBody
     * @return
     */
    private String getRequestURl(String accountId, String token, String restfulHead, String methodRequestPath, String messageBody) {
        String url = restfulHead + methodRequestPath;
        // 1.获取请求方法path的md5哈希
        String hashStr1 = org.apache.commons.codec.digest.DigestUtils.md5Hex(methodRequestPath.getBytes());
        // 2.获取请求方法条件参数的md5哈希，如果为空就为""
        String hashStr2 = "";//org.apache.commons.codec.digest.DigestUtils.md5Hex("");
        // 3.请求的消息体字符串md5 哈希
        String hashStr3 = org.apache.commons.codec.digest.DigestUtils.md5Hex(messageBody.getBytes());
        // 4.获取当前时间戳的字符串
        String timestr = String.valueOf(new Date().getTime() / 1000);
        Integer timestamp = Integer.valueOf(timestr);
        // 5.获取1-1000内的随机整数
        int nonce = (int) (Math.random() * 10000 + 1);

        // 6.将7个参数放在list里面排序，然后拼接排序后的字符串 进行sha1 加密得到 signature
        List<String> sortList = new ArrayList<>();
        sortList.add(accountId);
        sortList.add(token);
        sortList.add(timestr);
        sortList.add(String.valueOf(nonce));
        sortList.add(hashStr1);
        sortList.add(hashStr2);
        sortList.add(hashStr3);
        Collections.sort(sortList);
        StringBuffer sb = new StringBuffer();
        for (String item : sortList) {
            sb.append(item);
        }
        // 进行sha1加密得到signature
        String signature = org.apache.commons.codec.digest.DigestUtils.sha1Hex(sb.toString());
        // 7.拼接最后的请求参数
        StringBuffer sb2 = new StringBuffer();
        url = url + "?";
        sb2.append(String.format("%ssignature=%s&nonce=%s&timestamp=%s&accountId=%s", url, signature, nonce, timestamp, accountId));
        return sb2.toString();
    }

}
