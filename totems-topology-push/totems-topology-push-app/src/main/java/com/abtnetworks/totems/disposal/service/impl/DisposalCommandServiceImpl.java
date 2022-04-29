package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.common.commandline.AbstractCommandlineFactory;
import com.abtnetworks.totems.common.commandline.RoutingFactory;
import com.abtnetworks.totems.common.commandline.routing.RoutingGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.ProcedureDTO;
import com.abtnetworks.totems.common.dto.TaskDTO;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.RoutingCommandDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.exception.UnInterruptException;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.IP6Utils;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.common.utils.ServiceDTOUtils;
import com.abtnetworks.totems.disposal.common.QuintupleUtils;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalCreateCommandLineRecordMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderCenterMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalOrderScenesMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalScenesNodeMapper;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalWhiteListMapper;
import com.abtnetworks.totems.disposal.dto.DisposalCommandDTO;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import com.abtnetworks.totems.disposal.entity.DisposalOrderScenesEntity;
import com.abtnetworks.totems.disposal.entity.DisposalWhiteListEntity;
import com.abtnetworks.totems.disposal.enums.DisposalActionEnum;
import com.abtnetworks.totems.disposal.service.DisposalCommandService;
import com.abtnetworks.totems.generate.manager.VendorManager;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.CommandlineManager;
import com.abtnetworks.totems.recommend.service.CommandService;
import com.abtnetworks.totems.whale.baseapi.dto.RoutingTableSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.ro.SubnetLinkedDeviceRO;
import com.abtnetworks.totems.whale.baseapi.ro.SubnetUuidListRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleSubnetObjectClient;
import com.abtnetworks.totems.whale.common.CommonRangeStringDTO;
import com.abtnetworks.totems.whale.policy.dto.FilterListsRuleSearchDTO;
import com.abtnetworks.totems.whale.policy.dto.IpTermsDTO;
import com.abtnetworks.totems.whale.policy.dto.JsonQueryDTO;
import com.abtnetworks.totems.whale.policy.ro.FilterListsRuleSearchDataRO;
import com.abtnetworks.totems.whale.policy.ro.FilterListsRuleSearchRO;
import com.abtnetworks.totems.whale.policy.ro.RoutingEntriesRO;
import com.abtnetworks.totems.whale.policy.service.WhalePolicyClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_ANY;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_ICMP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_TCP;
import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_NUM_VALUE_UDP;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV6;

/**
 * @author zc
 * @date 2019/11/13
 */
@Slf4j
@Service
public class DisposalCommandServiceImpl implements DisposalCommandService {

    public static final String TYPE_FIREWALL = "0";
    public static final String TYPE_ROUTING = "1";

    @Resource
    private DisposalOrderCenterMapper disposalOrderCenterMapper;

    @Resource
    private DisposalOrderScenesMapper disposalOrderScenesMapper;

    @Resource
    private DisposalScenesNodeMapper disposalScenesNodeMapper;

    @Resource
    private DisposalWhiteListMapper disposalWhiteListMapper;

    @Resource
    private DisposalCreateCommandLineRecordMapper disposalCreateCommandLineRecordMapper;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private WhaleSubnetObjectClient whaleSubnetObjectClient;

    @Resource
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Resource
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Resource
    private WhalePolicyClient whalePolicyClient;

    @Resource
    private CommandService commandService;

    //各型号通用处理
    @Autowired
    Map<String, CmdService> cmdServiceMap;

    //生成器相关
    @Autowired
    VendorManager vendorManager;

    //调用命令生成
    @Autowired
    CommandlineManager commandlineManager;

    /**
     * 协议中的any等同于[icmp,tcp,udp], 暂时这么处理
     */
    private static final List<String> PROTOCOL_ANY;

    static {
        PROTOCOL_ANY = Arrays.asList(POLICY_NUM_VALUE_ICMP, POLICY_NUM_VALUE_TCP, POLICY_NUM_VALUE_UDP);
    }

    @Override
    public List<DisposalCommandDTO> generateCommand(String orderUuid) {
        List<DisposalCommandDTO> commandDTOList;
        try {
            DisposalOrderCenterEntity orderCenter = disposalOrderCenterMapper.getByUuid(orderUuid);
            String orderNo = orderCenter.getOrderNo();
            log.info("新建-策略或路由命令行-工单号[{}]", orderNo);
            int category = orderCenter.getCategory();
            if (category == 0) {
                commandDTOList = generatePolicyCommand(orderCenter);
            } else if (category == 1) {
                commandDTOList = generateRoutingCommand(orderCenter);
            } else {
                throw new Exception("类型["+category+"]不存在");
            }
        } catch (Exception e) {
            log.error("命令行生成错误", e);
            return null;
        }
        return commandDTOList;
    }

    /**
     * 生成策略命令
     * @param orderCenter
     * @return
     * @throws Exception
     */
    private List<DisposalCommandDTO> generatePolicyCommand(DisposalOrderCenterEntity orderCenter) throws Exception {
        log.info("新建策略");
        List<QuintupleUtils.Quintuple> preQuintupleList = serviceJsonHandle(orderCenter.getSrcIp(),orderCenter.getDstIp(),
                orderCenter.getServiceList());
        List<QuintupleUtils.Quintuple> postQuinTupleList = preQuintupleList;
        String action = orderCenter.getAction();
        if (action.equals(DisposalActionEnum.DENY.getCode())) {
            log.debug("策略封堵白名单查询");
            DisposalWhiteListEntity disposalWhiteListEntity = new DisposalWhiteListEntity();
            disposalWhiteListEntity.setDeleted(false);
            disposalWhiteListEntity.setType(0);
            List<DisposalWhiteListEntity> disposalWhiteListEntityList = disposalWhiteListMapper.get(disposalWhiteListEntity);
            if (disposalWhiteListEntityList == null || disposalWhiteListEntityList.size() == 0) {
                log.debug("未查询到需要过滤的白名单");
            } else {
                log.debug("策略白名单过滤");
                List<QuintupleUtils.Quintuple> filterQuintupleList = disposalWhiteListEntityList.stream()
                        .map(whiteList -> serviceJsonHandle(whiteList.getSrcIp(),whiteList.getDstIp(),whiteList.getServiceList()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                postQuinTupleList = QuintupleUtils.quintupleFilter(preQuintupleList, filterQuintupleList);
            }
            if (postQuinTupleList.size() == 0) {
                log.info("白名单过滤后没有策略可以下发了");
                return new ArrayList<>();
            }
        }

        log.info("设备命令行对象生成");
        List<CommandlineDTO> commandlineDTOS;
        switch (orderCenter.getType()) {
            case 1:
                log.info("手动新建策略");
                commandlineDTOS = this.createPolicyCmdOne(orderCenter);
                break;
            case 2:
                log.info("ip新建策略");
                commandlineDTOS = this.createPolicyCmdTwo(orderCenter);
                break;
            default:
                throw new Exception("未知的新建策略类型["+orderCenter.getType()+"]");
        }
        if (commandlineDTOS == null || commandlineDTOS.isEmpty()) {
            log.info("查询场景、设备返回空，请检查设备是否存在,工单uuid:{}，工单name:{}", orderCenter.getUuid(), orderCenter.getOrderName());
        }
        log.info("历史数据过滤");
        Map<String, List<QuintupleUtils.Quintuple>> deviceQuintupleListMap = new HashMap<>(16);
        for (CommandlineDTO commandlineDTO : commandlineDTOS) {
            String deviceUuid = commandlineDTO.getDeviceUuid();
            List<QuintupleUtils.Quintuple> handleList = filterPolicyHistory(postQuinTupleList, deviceUuid, action)
                    //todo 协议的转换因为业务实际场景需求，这里写一个业务逻辑上的bug
                    .stream()
                    .map(quintuple -> {
                        List<QuintupleUtils.Quintuple> quintuples = new ArrayList<>();
                        List<String> protocolList = quintuple.getProtocolList();
                        if (protocolList.containsAll(PROTOCOL_ANY)) {
                            quintuple.setProtocolList(Collections.singletonList(POLICY_NUM_VALUE_ANY));
                            quintuples.add(quintuple);
                        } else if (protocolList.size() == 1) {
                            quintuples.add(quintuple);
                        } else {
                            protocolList.forEach(protocol -> quintuples.add(
                                    new QuintupleUtils.Quintuple(quintuple.getSrcIpList(), quintuple.getDstIpList(), Collections.singletonList(protocol),
                                            quintuple.getSrcPortList(), quintuple.getDstPortList())
                                    )
                            );
                        }
                        return quintuples;
                    })
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
            deviceQuintupleListMap.put(deviceUuid, handleList);
        }

        return commandlineDTOS.stream()
                .map(commandlineDTO -> commonGeneratePolicyCommand(commandlineDTO,
                        deviceQuintupleListMap.get(commandlineDTO.getDeviceUuid())))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 生成路由命令
     * @param orderCenter
     * @return
     * @throws Exception
     */
    private List<DisposalCommandDTO> generateRoutingCommand(DisposalOrderCenterEntity orderCenter) throws Exception {
        log.info("新建路由");
        List<String> preIpList = Arrays.asList(orderCenter.getRoutingIp().split(","));
        List<String> postIpList;
        log.debug("路由白名单查询");
        DisposalWhiteListEntity disposalWhiteListEntity = new DisposalWhiteListEntity();
        disposalWhiteListEntity.setDeleted(false);
        disposalWhiteListEntity.setType(1);
        List<DisposalWhiteListEntity> disposalWhiteListEntityList = disposalWhiteListMapper.get(disposalWhiteListEntity);
        if (disposalWhiteListEntityList == null || disposalWhiteListEntityList.size() == 0) {
            log.debug("未查询到需要过滤的白名单");
            postIpList = preIpList;
        } else {
            log.debug("路由白名单过滤");
            List<String> filterIpList = disposalWhiteListEntityList.stream()
                    .map(DisposalWhiteListEntity::getRoutingIp)
                    .filter(StringUtils::isNotEmpty)
                    .map(ip -> Arrays.asList(ip.split(",")))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            postIpList = QuintupleUtils.ipListFilter(preIpList, filterIpList).getPostFilterData();
        }
        if (postIpList == null || postIpList.size() == 0) {
            log.info("白名单过滤后没有路由可以下发了");
            return new ArrayList<>();
        }

        log.info("查询所有受影响的设备");
        Set<String> deviceUuidSet;
        switch (orderCenter.getType()) {
            case 1:
                log.info("手动新建路由");
                deviceUuidSet = this.createRoutingCmdOne(orderCenter);
                break;
            case 2:
                log.info("ip新建路由");
                deviceUuidSet = this.createRoutingCmdTwo(orderCenter);
                log.info("查询到所有受影响的设备：[{}]", JSONObject.toJSONString(deviceUuidSet));
                break;
            default:
                throw new Exception("未知的新建路由类型["+orderCenter.getType()+"]");
        }
        Map<String, List<String>> deviceIpListMap = new HashMap<>(16);
        for (String deviceUuid : deviceUuidSet) {
            deviceIpListMap.put(deviceUuid, filterRoutingHistory(postIpList, deviceUuid));
        }
        return deviceUuidSet.stream()
                //封装所有设备的命令行查询所用的对象
                .map(deviceUuid -> {

                    RoutingCommandDTO routingCommandDTO = new RoutingCommandDTO();
                    routingCommandDTO.setDeviceUuid(deviceUuid);
                    List<String> ipList = deviceIpListMap.get(deviceUuid);
                    routingCommandDTO.setIpAddr(StringUtils.join(ipList, ","));
                    routingCommandDTO.setRoutingType(RoutingCommandDTO.RoutingType.UNREACHABLE);
                    routingCommandDTO.setDescription(orderCenter.getOrderNo());
                    return routingCommandDTO;
                })
                .filter(routingCommandDTO -> StringUtils.isNotEmpty(routingCommandDTO.getIpAddr()))
                .map(this::commonGenerateRoutingCommand)
                .filter(disposalCommandDTO -> disposalCommandDTO.getDeviceUuid() != null)
                .collect(Collectors.toList());
    }

    /**
     * 生成命令对象-新建策略-手动
     */
    private List<CommandlineDTO> createPolicyCmdOne(DisposalOrderCenterEntity orderCenter) throws Exception {
        List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(orderCenter.getUuid());
        if (orderScenesList != null && orderScenesList.size() != 0) {
            return orderScenesList.stream()
                    .map(DisposalOrderScenesEntity::getScenesUuid)
                    //查询所有场景下的设备相关信息
                    .map(scenesUuid -> {
                        List<DisposalScenesDTO> disposalScenesDTOS = new ArrayList<>();
                        DisposalScenesDTO disposalScenesDTO = new DisposalScenesDTO();
                        disposalScenesDTO.setScenesUuid(scenesUuid);
                        disposalScenesDTO.setQueryTypeList(Collections.singletonList(TYPE_FIREWALL));
                        try {
                            disposalScenesDTOS = disposalScenesNodeMapper.findDtoList(disposalScenesDTO);
                            if (disposalScenesDTOS == null || disposalScenesDTOS.isEmpty()) {
                                log.error("根据工单、场景查询设备信息为空，可能是：场景里面的设备已删除");
                            }
                        } catch (Exception e) {
                            log.error("场景节点表查询报错",e);
                        }
                        return disposalScenesDTOS;
                    })
                    .flatMap(Collection::stream)
                    //排除重复的设备
                    .collect(Collectors.toMap(DisposalScenesDTO::getDeviceUuid, value -> value,
                            (oldValue, newValue) -> newValue))
                    .values()
                    .stream()
                    //封装所有设备的命令行查询所用的对象
                    .map(disposalScenesDTO -> {
                        CommandlineDTO commandlineDTO = new CommandlineDTO();
                        commandlineDTO.setBusinessName(orderCenter.getOrderNo());
                        commandlineDTO.setDeviceUuid(disposalScenesDTO.getDeviceUuid());
                        commandlineDTO.setSrcZone(disposalScenesDTO.getSrcZoneName());
                        commandlineDTO.setSrcItf(disposalScenesDTO.getSrcZoneName());
                        commandlineDTO.setSrcItfAlias(disposalScenesDTO.getSrcZoneName());
                        commandlineDTO.setDstZone(disposalScenesDTO.getDstZoneName());
                        commandlineDTO.setDstItf(disposalScenesDTO.getDstZoneName());
                        commandlineDTO.setDstItfAlias(disposalScenesDTO.getDstZoneName());
                        commandlineDTO.setAction(orderCenter.getAction());
                        commandlineDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
                        return commandlineDTO;
                    })
                    .collect(Collectors.toList());
        } else {
            throw new Exception("未查询到匹配的设备");
        }
    }

    /**
     * 生成命令对象-新建策略-自动-ip范围
     * @param orderCenter
     * @return
     */
    private List<CommandlineDTO> createPolicyCmdTwo(DisposalOrderCenterEntity orderCenter) throws Exception {
        String srcIp = orderCenter.getSrcIp();
        String dstIp = orderCenter.getDstIp();
        String ip;
        if (StringUtils.isNotEmpty(srcIp) && StringUtils.isEmpty(dstIp)) {
            log.info("与源ip相关的策略");
            ip = srcIp;
        } else if (StringUtils.isNotEmpty(dstIp) && StringUtils.isEmpty(srcIp)) {
            log.info("与目的ip相关的策略");
            ip = dstIp;
        } else {
            throw new Exception("源、目的ip有误：srcIp为["+srcIp+"]，dstIp为["+dstIp+"]");
        }

        log.debug("ip -> 子网 -> 连接的设备");
        Set<String> deviceUuidSet = Arrays.stream(ip.split(","))
                .parallel()
                //查询每一种ip所能查询到的子网列表
                .map(ipAddr -> {
                    List<String> subnetUuidList = new ArrayList<>();
                    SubnetUuidListRO subnetUuidListRO = whaleSubnetObjectClient.getSubnetUuidList(ipAddr);
                    if (subnetUuidListRO.getSuccess() && subnetUuidListRO.getData().size() > 0) {
                        subnetUuidList = subnetUuidListRO.getData();
                    }
                    return subnetUuidList;
                })
                .flatMap(Collection::stream)
                .distinct()
                //查询每一个子网关联的设备列表
                .map(subnetUuid -> {
                    List<String> deviceUuidList = new ArrayList<>();
                    SubnetLinkedDeviceRO subnetLinkedDeviceRO = whaleSubnetObjectClient.getSubnetLinkedDevice(subnetUuid);
                    if (subnetLinkedDeviceRO.getSuccess()) {
                        subnetLinkedDeviceRO.getData().stream()
                                .filter(deviceSummaryDataRO -> "FIREWALL".equals(deviceSummaryDataRO.getDeviceType()))
                                .forEach(deviceSummaryDataRO -> deviceUuidList.add(deviceSummaryDataRO.getUuid()));
                    }
                    return deviceUuidList;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        log.debug("ip -> 策略搜索 -> 命中的设备");
        Set<String> deviceUuidSet2 = Arrays.stream(ip.split(","))
                .map(ipAddr -> {
                    List<FilterListsRuleSearchDataRO> filterListsRuleSearchDataROS = new ArrayList<>();
                    try {
                        BigInteger[] bigIntegers = QuintupleUtils.ipv46ToNumRange(ipAddr);
                        String ipStart = QuintupleUtils.bigIntToIpv46(bigIntegers[0]);
                        String ipEnd = QuintupleUtils.bigIntToIpv46(bigIntegers[1]);
                        boolean isIpv4 = QuintupleUtils.isIpv4(ipEnd);
                        CommonRangeStringDTO ipRange = new CommonRangeStringDTO(ipStart, ipEnd);
                        FilterListsRuleSearchDTO filterListsRuleSearchDTO = new FilterListsRuleSearchDTO();
                        if (!isIpv4) {
                            filterListsRuleSearchDTO.setIpType("IP6");
                        }
                        IpTermsDTO ipTermsDTO = new IpTermsDTO();
                        if (StringUtils.isNotEmpty(srcIp)) {
                            if (isIpv4) {
                                ipTermsDTO.setIp4SrcAddresses(Collections.singletonList(ipRange));
                            } else {
                                ipTermsDTO.setIp6SrcAddresses(Collections.singletonList(ipRange));
                            }
                        } else {
                            if (isIpv4) {
                                ipTermsDTO.setIp4DstAddresses(Collections.singletonList(ipRange));
                            } else {
                                ipTermsDTO.setIp6DstAddresses(Collections.singletonList(ipRange));
                            }
                        }
                        filterListsRuleSearchDTO.setIpTerms(Collections.singletonList(ipTermsDTO));
                        JsonQueryDTO jsonQueryDTO = new JsonQueryDTO();
                        Map<String, String[]> filterListType = new HashMap<>();
                        filterListType.put("$in", new String[]{"SYSTEM__POLICY_1", "SYSTEM__POLICY_2"});
                        jsonQueryDTO.setFilterListType(filterListType);
                        filterListsRuleSearchDTO.setJsonQuery(jsonQueryDTO);
                        FilterListsRuleSearchRO filterListsRuleSearchRO = whalePolicyClient.getFilterListsRuleSearch(filterListsRuleSearchDTO);
                        if (filterListsRuleSearchRO.getSuccess()) {
                            filterListsRuleSearchDataROS.addAll(filterListsRuleSearchRO.getData());
                        }
                    } catch (UnknownHostException e) {
                        log.error("ip地址[{}]异常",ipAddr, e);
                    }
                    return filterListsRuleSearchDataROS;
                })
                .flatMap(Collection::stream)
                .map(FilterListsRuleSearchDataRO::getDeviceUuid)
                .collect(Collectors.toSet());

        deviceUuidSet.addAll(deviceUuidSet2);
        log.info("匹配到设备：[{}]", JSONObject.toJSONString(deviceUuidSet));
        return deviceUuidSet.stream()
                //封装所有设备的命令行查询所用的对象
                .map(deviceUuid -> {
                    CommandlineDTO commandlineDTO = new CommandlineDTO();
                    commandlineDTO.setBusinessName(orderCenter.getOrderNo());
                    commandlineDTO.setDeviceUuid(deviceUuid);
                    commandlineDTO.setAction(orderCenter.getAction());
                    commandlineDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
                    return commandlineDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成命令-新建路由-手动
     * @param orderCenter
     * @return
     */
    private Set<String> createRoutingCmdOne(DisposalOrderCenterEntity orderCenter) throws Exception {
        List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(orderCenter.getUuid());
        if (orderScenesList == null || orderScenesList.size() == 0) {
            throw new Exception("未查询到匹配的设备");
        }
        Set<String> deviceUuidSet = new HashSet<>();
        for (DisposalOrderScenesEntity disposalOrderScenesEntity : orderScenesList) {
            String scenesUuid = disposalOrderScenesEntity.getScenesUuid();
            DisposalScenesDTO disposalScenesDTO = new DisposalScenesDTO();
            disposalScenesDTO.setScenesUuid(scenesUuid);
            disposalScenesDTO.setQueryTypeList(Arrays.asList(TYPE_FIREWALL, TYPE_ROUTING));
            disposalScenesNodeMapper.findDtoList(disposalScenesDTO)
                    .forEach(dto -> deviceUuidSet.add(dto.getDeviceUuid()));
        }
        return deviceUuidSet;
    }

    /**
     * 生成命令-新建路由-ip范围
     * @param orderCenter
     * @return
     */
    private Set<String> createRoutingCmdTwo(DisposalOrderCenterEntity orderCenter) throws Exception {
        String routingIp = orderCenter.getRoutingIp();
        if (StringUtils.isEmpty(routingIp)) {
            throw new Exception("routingIp不能为空");
        }
        List<CommonRangeStringDTO> ipv4List = new ArrayList<>();
        List<CommonRangeStringDTO> ipv6List = new ArrayList<>();
        Arrays.stream(routingIp.split(",")).forEach(ip -> {
                    CommonRangeStringDTO commonRangeStringDTO = new CommonRangeStringDTO();
                    try {
                        BigInteger[] bigIntegers = QuintupleUtils.ipv46ToNumRange(ip);
                        String ipStart = QuintupleUtils.bigIntToIpv46(bigIntegers[0]);
                        String ipEnd = QuintupleUtils.bigIntToIpv46(bigIntegers[1]);
                        boolean isIpv4 = QuintupleUtils.isIpv4(ipEnd);
                        if (isIpv4) {
                            ipv4List.add(commonRangeStringDTO);
                        } else {
                            ipv6List.add(commonRangeStringDTO);
                        }
                        commonRangeStringDTO.setStart(ipStart);
                        commonRangeStringDTO.setEnd(ipEnd);
                    } catch (UnknownHostException e) {
                        log.error("错误的ip[{}]", ip, e);
                    }
                });
        log.info("查询所有设备");
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(null);
        if (deviceRO.getSuccess()) {
            log.info("获取设备uuid集合");
            return deviceRO.getData().stream()
                    .parallel()
                    //只访问防火墙和路由
                    .filter(deviceDataRO ->
                        StringUtils.equalsAny(deviceDataRO.getDeviceType(), "FIREWALL", "ROUTER")
                                && deviceDataRO.getRoutingTableIds() != null
                                && deviceDataRO.getRoutingTableIds().size() > 0
                    )
                    //调用whale路由表搜索的接口，过滤掉查询不到结果的数据
                    .filter(deviceDataRO -> {
                        List<RoutingEntriesRO> routingEntriesROS = new ArrayList<>();
                        log.trace("调用搜索路由表的接口");
                        RoutingTableSearchDTO routingTableSearchDTO = new RoutingTableSearchDTO();
                        routingTableSearchDTO.setDeviceUuid(deviceDataRO.getUuid());
                        routingTableSearchDTO.setIgnoreMatchAllRoute(true);
                        if (ipv4List.size() != 0) {
                            routingTableSearchDTO.setAddressRanges(ipv4List);
                            ResultRO<List<RoutingEntriesRO>> listResultRO = whaleDevicePolicyClient.searchRout(routingTableSearchDTO);
                            if (listResultRO.getSuccess() && listResultRO.getData().size() > 0) {
                                routingEntriesROS.addAll(listResultRO.getData());
                            }
                        }
                        if (ipv6List.size() != 0) {
                            routingTableSearchDTO.setAddressRanges(null);
                            routingTableSearchDTO.setIp6AddressRanges(ipv6List);
                            ResultRO<List<RoutingEntriesRO>> listResultRO = whaleDevicePolicyClient.searchRout(routingTableSearchDTO);
                            if (listResultRO.getSuccess() && listResultRO.getData().size() > 0) {
                                routingEntriesROS.addAll(listResultRO.getData());
                            }
                        }
                        if (routingEntriesROS.size() != 0) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .map(DeviceDataRO::getUuid)
                    .collect(Collectors.toSet());
        } else {
            throw new Exception("whale查询报错");
        }
    }

    /**
     * 调用策略命令行生成的公共方法
     * @param commandlineDTO
     * @return
     */
    private List<DisposalCommandDTO> commonGeneratePolicyCommand(CommandlineDTO commandlineDTO,
                                                           List<QuintupleUtils.Quintuple> quintupleList) {
        List<DisposalCommandDTO> disposalCommandDTOS = new ArrayList<>();
        String deviceUuid = commandlineDTO.getDeviceUuid();
        NodeEntity node = nodeMapper.getTheNodeByUuid(deviceUuid);
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
        if (node == null || !deviceRO.getSuccess()) {
            log.error("不存在该设备[{}]", deviceUuid);
        } else {
            DisposalCommandDTO disposalCommandDTO = new DisposalCommandDTO();
            disposalCommandDTO.setDeviceUuid(deviceUuid);
            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            Boolean isVsys = deviceDataRO.getIsVsys();
            if (isVsys != null && isVsys) {
                String vSysName= deviceDataRO.getVsysName();
                commandlineDTO.setVsys(true);
                commandlineDTO.setVsysName(vSysName);
                disposalCommandDTO.setIsVsys(true);
                disposalCommandDTO.setVsysName(vSysName);
                disposalCommandDTO.setRootDeviceUuid(deviceDataRO.getRootDeviceUuid());
            } else {
                disposalCommandDTO.setIsVsys(false);
            }
            String cpmiGatewayClusterName = deviceDataRO.getCpmiGatewayClusterName();
            if(StringUtils.isNotEmpty(cpmiGatewayClusterName)){
                disposalCommandDTO.setCpmiGatewayClusterName(cpmiGatewayClusterName);
            }else{
                disposalCommandDTO.setCpmiGatewayClusterName(deviceDataRO.getName());
            }

            String name = commandlineDTO.getBusinessName();
            for (int i = 0; i < quintupleList.size(); i++) {
                try {
                    DisposalCommandDTO resultCommand = new DisposalCommandDTO();
                    BeanUtils.copyProperties(disposalCommandDTO, resultCommand);
                    String srcIps = StringUtils.join(quintupleList.get(i).getSrcIpList(), ",");
                    String dstIps = StringUtils.join(quintupleList.get(i).getDstIpList(), ",");
                    String protocols = StringUtils.join(quintupleList.get(i).getProtocolList(), ",");
                    String srcPorts = StringUtils.join(quintupleList.get(i).getSrcPortList(), ",");
                    String dstPorts = StringUtils.join(quintupleList.get(i).getDstPortList(), ",");

                    //todo 当ip为0.0.0.0/0时，设置ip为 "", 建议优化
                    commandlineDTO.setSrcIp(StringUtils.containsAny(srcIps, PolicyConstants.IPV4_ANY, PolicyConstants.IPV6_ANY) ? "" : srcIps);
                    commandlineDTO.setDstIp(StringUtils.containsAny(dstIps, PolicyConstants.IPV4_ANY, PolicyConstants.IPV6_ANY) ? "" : dstIps);
                    ServiceDTO serviceDTO = new ServiceDTO();
                    serviceDTO.setProtocol(protocols);
                    serviceDTO.setSrcPorts(srcPorts);
                    serviceDTO.setDstPorts(dstPorts);
                    List<ServiceDTO> serviceDTOS = Collections.singletonList(serviceDTO);
                    commandlineDTO.setServiceList(serviceDTOS);
                    commandlineDTO.setBusinessName(name + "_" + i);
                    commandlineDTO.setName(name + "_" + i);

                    resultCommand.setSrcIp(srcIps);
                    resultCommand.setDstIp(dstIps);
                    resultCommand.setServiceList(JSONObject.toJSONString(serviceDTOS));

                    String modelNumber = node.getModelNumber();
                    resultCommand.setModelNumber(modelNumber);

                    log.info("改版之前，原参数,dto:{}", JSONObject.toJSONString(commandlineDTO));
                    CmdDTO cmdDTO = new CmdDTO();
                    //设备
                    DeviceDTO device = cmdDTO.getDevice();
                    device.setDeviceUuid(deviceUuid);
                    device.setVsys(commandlineDTO.isVsys());
                    device.setVsysName(commandlineDTO.getVsysName());
                    device.setHasVsys(commandlineDTO.isHasVsys());
                    //策略
                    PolicyDTO policyDTO = cmdDTO.getPolicy();
                    if(IP6Utils.isIPv6(srcIps)){
                        //todo ipType 没有入库导致需要判断，最好入库保持一致
                        policyDTO.setIpType(IPV6.getCode());
                    }else{
                        policyDTO.setIpType(IPV4.getCode());
                    }

                    policyDTO.setType(PolicyEnum.SECURITY);
                    policyDTO.setSrcIp(commandlineDTO.getSrcIp());
                    policyDTO.setDstIp(commandlineDTO.getDstIp());
                    policyDTO.setServiceList(commandlineDTO.getServiceList());

                    policyDTO.setSrcZone(commandlineDTO.getSrcZone());
                    policyDTO.setDstZone(commandlineDTO.getDstZone());

                    policyDTO.setSrcItf(commandlineDTO.getSrcItf());
                    policyDTO.setDstItf(commandlineDTO.getDstItf());

                    policyDTO.setSrcItfAlias(commandlineDTO.getSrcItfAlias());
                    policyDTO.setDstItfAlias(commandlineDTO.getDstItfAlias());

                    policyDTO.setDescription(commandlineDTO.getDescription());
                    if(commandlineDTO.getAction().equalsIgnoreCase(ActionEnum.PERMIT.getKey())){
                        policyDTO.setAction(ActionEnum.PERMIT);
                    }else{
                        policyDTO.setAction(ActionEnum.DENY);
                    }

                    //任务
                    TaskDTO taskDTO = cmdDTO.getTask();
                    taskDTO.setTheme(commandlineDTO.getBusinessName());

                    //通用步骤处理
                    boolean done = cmdGenerator(cmdDTO);

                    if(!done) {
                        log.error("无法生成该设备的命令行, name:{}", name);
                        continue;
                    }

            /*        //前置信息处理
                    commandService.setPreSteps(modelNumber, commandlineDTO);
                    //复用对象信息
                    commandService.setAdvancedSetting(cmdDTO, modelNumber, deviceUuid);*/

                    log.info("改版之后，最后一步参数,dto:{}", JSONObject.toJSONString(cmdDTO));

                    String command = commandlineManager.generate(cmdDTO);
                    resultCommand.setCommandLine(command);

                    String rollbackCommandline = commandlineManager.generateRollback(cmdDTO);
                    resultCommand.setDeleteCommandLine(rollbackCommandline);

                    /*AbstractCommandlineFactory commandlineFactory = new SecurityPolicyFactory();
                    PolicyGenerator securityGen = commandlineFactory.securityPolicyFactory(modelNumber);
                    String command = securityGen.generate(cmdDTO);

                    resultCommand.setCommandLine(command);
                    String deleteCommand = "";
                    PolicyGenerator rollbackGen = commandlineFactory.rollbackSecurity(modelNumber);
                    if (rollbackGen != null) {
                        deleteCommand = rollbackGen.generate(cmdDTO);
                    }
                    resultCommand.setDeleteCommandLine(deleteCommand);*/

                    disposalCommandDTOS.add(resultCommand);
                } catch (Exception e) {
                    log.error("命令行调用设备[{}]异常", deviceUuid, e);
                }
            }

        }
        return disposalCommandDTOS;
    }

    /**
     * 调用路由命令行生成的公共方法
     * @param routingCommandDTO
     * @return
     */
    private DisposalCommandDTO commonGenerateRoutingCommand(RoutingCommandDTO routingCommandDTO) {
        DisposalCommandDTO disposalCommandDTO = new DisposalCommandDTO();
        String deviceUuid = routingCommandDTO.getDeviceUuid();
        disposalCommandDTO.setDeviceUuid(deviceUuid);
        disposalCommandDTO.setRoutingIp(routingCommandDTO.getIpAddr());
        NodeEntity node = nodeMapper.getTheNodeByUuid(deviceUuid);
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
        if (node == null || !deviceRO.getSuccess()) {
            log.error("不存在该设备[{}]", deviceUuid);
        } else {
            try {
                DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
                Boolean isVsys = deviceDataRO.getIsVsys();
                if (isVsys != null && isVsys) {
                    String vSysName = deviceDataRO.getVsysName();
                    routingCommandDTO.setIsVsys(true);
                    routingCommandDTO.setVsysName(vSysName);
                    disposalCommandDTO.setIsVsys(true);
                    disposalCommandDTO.setVsysName(vSysName);
                    disposalCommandDTO.setRootDeviceUuid(deviceDataRO.getRootDeviceUuid());
                } else {
                    disposalCommandDTO.setIsVsys(false);
                }
                if(StringUtils.isNotEmpty(deviceDataRO.getCpmiGatewayClusterName())){
                    disposalCommandDTO.setCpmiGatewayClusterName(deviceDataRO.getCpmiGatewayClusterName());
                }else{
                    disposalCommandDTO.setCpmiGatewayClusterName(deviceDataRO.getName());
                }

                String modelNumber = node.getModelNumber();
                disposalCommandDTO.setModelNumber(modelNumber);
                AbstractCommandlineFactory commandlineFactory = new RoutingFactory();
                RoutingGenerator generator = commandlineFactory.routingFactory(modelNumber);
                String command = generator.generatorRoutingCommandLine(routingCommandDTO);
                disposalCommandDTO.setCommandLine(command);
                String deleteCommand = generator.deleteRoutingCommandLine(routingCommandDTO);
                disposalCommandDTO.setDeleteCommandLine(deleteCommand);
            } catch (Exception e) {
                log.error("命令行调用设备[{}]异常",deviceUuid, e);
            }
        }
        return disposalCommandDTO;
    }

    /**
     * 过滤曾经生成的策略
     * @param preQuintupleList
     * @param deviceUuid
     * @return
     * @throws Exception
     */
    private List<QuintupleUtils.Quintuple> filterPolicyHistory(List<QuintupleUtils.Quintuple> preQuintupleList,
                                                               String deviceUuid, String action) throws Exception {
        return preQuintupleList;
        /*List<DisposalCreateCommandLineRecordEntity> recordEntities = new ArrayList<>();
        if (action.equals(DisposalActionEnum.DENY.getCode())) {
            recordEntities = disposalCreateCommandLineRecordMapper.findListByDevice(deviceUuid, null, 0);
        } else if (action.equals(DisposalActionEnum.PERMIT.getCode())) {
            recordEntities = disposalCreateCommandLineRecordMapper.findListByDevice(deviceUuid, null, 1);
        }
        if (recordEntities == null || recordEntities.size() == 0) {
            return preQuintupleList;
        }
        List<QuintupleUtils.Quintuple> filterQuintupleList = new ArrayList<>();
        recordEntities.forEach(recordEntity ->
                filterQuintupleList.addAll(serviceJsonHandle(recordEntity.getSrcIp(),recordEntity.getDstIp(),recordEntity.getServiceList())));
        return QuintupleUtils.quintupleFilter(preQuintupleList, filterQuintupleList);*/
    }

    /**
     * 过滤曾经生成的路由
     * @param ipList
     * @param deviceUuid
     * @return
     * @throws Exception
     */
    private List<String> filterRoutingHistory(List<String> ipList,
                                                   String deviceUuid) throws Exception {
        return ipList;
        /*List<DisposalCreateCommandLineRecordEntity> recordEntities =
                disposalCreateCommandLineRecordMapper.findListByDevice(deviceUuid, "true", 0);
        if (recordEntities == null || recordEntities.size() == 0) {
            return ipList;
        }
        List<String> filterIpList = recordEntities.stream()
                .map(DisposalCreateCommandLineRecordEntity::getRoutingIp)
                .filter(StringUtils::isNotEmpty)
                .map(ip -> Arrays.asList(ip.split(",")))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return QuintupleUtils.ipListFilter(ipList, filterIpList).getPostFilterData();*/
    }

    /**
     * 处理service Json格式的数据处理成标准的五元组
     * @return
     */
    private List<QuintupleUtils.Quintuple> serviceJsonHandle(String srcIps, String dstIps, String serviceJson) {
        List<QuintupleUtils.Quintuple> quintupleList = new ArrayList<>();
        List<ServiceDTO> serviceDTOS = ServiceDTOUtils.toList(serviceJson);
        //todo 协议的转换因为业务实际场景需求，这里写一个业务逻辑上的bug
        if (serviceDTOS != null && serviceDTOS.size() != 0) {
            serviceDTOS.forEach(serviceDTO -> {
                String protocol = serviceDTO.getProtocol();
                if (StringUtils.isEmpty(protocol) || POLICY_NUM_VALUE_ANY.equals(protocol)) {
                    protocol = StringUtils.join(PROTOCOL_ANY,",");
                }
                quintupleList.add(
                        QuintupleUtils.convertQuintuple(
                                srcIps, dstIps, protocol, serviceDTO.getSrcPorts(),serviceDTO.getDstPorts()
                        )
                );
            });
        } else {
            quintupleList.add(QuintupleUtils.convertQuintuple(srcIps, dstIps, StringUtils.join(PROTOCOL_ANY,","),null,null));
        }
        return quintupleList;
    }

    //cmd 通用步骤，复制命令行生成的
    private boolean cmdGenerator(CmdDTO cmdDTO){
        boolean done = true;

        //获取品牌相关数据、该型号需要执行的步骤
        vendorManager.getVendorInfo(cmdDTO);

        //循环执行每一步
        ProcedureDTO procedure = cmdDTO.getProcedure();
        List<Integer> steps = procedure.getSteps();
        log.info("生成命令行数据为{}", JSONObject.toJSONString(cmdDTO, true));
        for (Integer step : steps) {
            log.info(String.format("命令行生成进行%s步骤", step));
            try {
                SubServiceEnum subService = SubServiceEnum.valueOf(step);

                String serviceName = NameUtils.getServiceDefaultName(subService.getServiceClass());
                log.info("开始进行{},调用{}服务", subService.getDesc(), serviceName);
                if(cmdServiceMap.containsKey(serviceName)) {
                    CmdService service = cmdServiceMap.get(serviceName);
                    if(service != null) {
                        service.modify(cmdDTO);
                    } else {
                        log.error("查找到服务{}为空！已注册子服务为{}", serviceName, JSONObject.toJSONString(cmdServiceMap, true));
                    }
                } else {
                    log.error("查找不到服务{}!已注册子服务为{}", serviceName, JSONObject.toJSONString(cmdServiceMap, true));
                }
            } catch (UnInterruptException e) {
                log.error("命令行对象修饰服务异常！", e);

                //非阻塞异常，命令行生成步骤继续进行
                //continue;
            } catch (Exception e) {
                log.error("命令行对象修饰出错！", e);

                //阻塞异常，命令行生成过程跳出
                done = false;
                break;
            }
        }

        if(!done){
            return done;
        }

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        PolicyEnum policyType = cmdDTO.getPolicy().getType();

        //设置回滚生成器
        vendorManager.getGenerator(policyType, deviceDTO, procedure);

        return done;
    }
}
