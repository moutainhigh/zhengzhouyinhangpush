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

    //?????????????????????
    @Autowired
    Map<String, CmdService> cmdServiceMap;

    //???????????????
    @Autowired
    VendorManager vendorManager;

    //??????????????????
    @Autowired
    CommandlineManager commandlineManager;

    /**
     * ????????????any?????????[icmp,tcp,udp], ??????????????????
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
            log.info("??????-????????????????????????-?????????[{}]", orderNo);
            int category = orderCenter.getCategory();
            if (category == 0) {
                commandDTOList = generatePolicyCommand(orderCenter);
            } else if (category == 1) {
                commandDTOList = generateRoutingCommand(orderCenter);
            } else {
                throw new Exception("??????["+category+"]?????????");
            }
        } catch (Exception e) {
            log.error("?????????????????????", e);
            return null;
        }
        return commandDTOList;
    }

    /**
     * ??????????????????
     * @param orderCenter
     * @return
     * @throws Exception
     */
    private List<DisposalCommandDTO> generatePolicyCommand(DisposalOrderCenterEntity orderCenter) throws Exception {
        log.info("????????????");
        List<QuintupleUtils.Quintuple> preQuintupleList = serviceJsonHandle(orderCenter.getSrcIp(),orderCenter.getDstIp(),
                orderCenter.getServiceList());
        List<QuintupleUtils.Quintuple> postQuinTupleList = preQuintupleList;
        String action = orderCenter.getAction();
        if (action.equals(DisposalActionEnum.DENY.getCode())) {
            log.debug("???????????????????????????");
            DisposalWhiteListEntity disposalWhiteListEntity = new DisposalWhiteListEntity();
            disposalWhiteListEntity.setDeleted(false);
            disposalWhiteListEntity.setType(0);
            List<DisposalWhiteListEntity> disposalWhiteListEntityList = disposalWhiteListMapper.get(disposalWhiteListEntity);
            if (disposalWhiteListEntityList == null || disposalWhiteListEntityList.size() == 0) {
                log.debug("????????????????????????????????????");
            } else {
                log.debug("?????????????????????");
                List<QuintupleUtils.Quintuple> filterQuintupleList = disposalWhiteListEntityList.stream()
                        .map(whiteList -> serviceJsonHandle(whiteList.getSrcIp(),whiteList.getDstIp(),whiteList.getServiceList()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                postQuinTupleList = QuintupleUtils.quintupleFilter(preQuintupleList, filterQuintupleList);
            }
            if (postQuinTupleList.size() == 0) {
                log.info("?????????????????????????????????????????????");
                return new ArrayList<>();
            }
        }

        log.info("???????????????????????????");
        List<CommandlineDTO> commandlineDTOS;
        switch (orderCenter.getType()) {
            case 1:
                log.info("??????????????????");
                commandlineDTOS = this.createPolicyCmdOne(orderCenter);
                break;
            case 2:
                log.info("ip????????????");
                commandlineDTOS = this.createPolicyCmdTwo(orderCenter);
                break;
            default:
                throw new Exception("???????????????????????????["+orderCenter.getType()+"]");
        }
        if (commandlineDTOS == null || commandlineDTOS.isEmpty()) {
            log.info("????????????????????????????????????????????????????????????,??????uuid:{}?????????name:{}", orderCenter.getUuid(), orderCenter.getOrderName());
        }
        log.info("??????????????????");
        Map<String, List<QuintupleUtils.Quintuple>> deviceQuintupleListMap = new HashMap<>(16);
        for (CommandlineDTO commandlineDTO : commandlineDTOS) {
            String deviceUuid = commandlineDTO.getDeviceUuid();
            List<QuintupleUtils.Quintuple> handleList = filterPolicyHistory(postQuinTupleList, deviceUuid, action)
                    //todo ?????????????????????????????????????????????????????????????????????????????????bug
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
     * ??????????????????
     * @param orderCenter
     * @return
     * @throws Exception
     */
    private List<DisposalCommandDTO> generateRoutingCommand(DisposalOrderCenterEntity orderCenter) throws Exception {
        log.info("????????????");
        List<String> preIpList = Arrays.asList(orderCenter.getRoutingIp().split(","));
        List<String> postIpList;
        log.debug("?????????????????????");
        DisposalWhiteListEntity disposalWhiteListEntity = new DisposalWhiteListEntity();
        disposalWhiteListEntity.setDeleted(false);
        disposalWhiteListEntity.setType(1);
        List<DisposalWhiteListEntity> disposalWhiteListEntityList = disposalWhiteListMapper.get(disposalWhiteListEntity);
        if (disposalWhiteListEntityList == null || disposalWhiteListEntityList.size() == 0) {
            log.debug("????????????????????????????????????");
            postIpList = preIpList;
        } else {
            log.debug("?????????????????????");
            List<String> filterIpList = disposalWhiteListEntityList.stream()
                    .map(DisposalWhiteListEntity::getRoutingIp)
                    .filter(StringUtils::isNotEmpty)
                    .map(ip -> Arrays.asList(ip.split(",")))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            postIpList = QuintupleUtils.ipListFilter(preIpList, filterIpList).getPostFilterData();
        }
        if (postIpList == null || postIpList.size() == 0) {
            log.info("?????????????????????????????????????????????");
            return new ArrayList<>();
        }

        log.info("??????????????????????????????");
        Set<String> deviceUuidSet;
        switch (orderCenter.getType()) {
            case 1:
                log.info("??????????????????");
                deviceUuidSet = this.createRoutingCmdOne(orderCenter);
                break;
            case 2:
                log.info("ip????????????");
                deviceUuidSet = this.createRoutingCmdTwo(orderCenter);
                log.info("????????????????????????????????????[{}]", JSONObject.toJSONString(deviceUuidSet));
                break;
            default:
                throw new Exception("???????????????????????????["+orderCenter.getType()+"]");
        }
        Map<String, List<String>> deviceIpListMap = new HashMap<>(16);
        for (String deviceUuid : deviceUuidSet) {
            deviceIpListMap.put(deviceUuid, filterRoutingHistory(postIpList, deviceUuid));
        }
        return deviceUuidSet.stream()
                //???????????????????????????????????????????????????
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
     * ??????????????????-????????????-??????
     */
    private List<CommandlineDTO> createPolicyCmdOne(DisposalOrderCenterEntity orderCenter) throws Exception {
        List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(orderCenter.getUuid());
        if (orderScenesList != null && orderScenesList.size() != 0) {
            return orderScenesList.stream()
                    .map(DisposalOrderScenesEntity::getScenesUuid)
                    //??????????????????????????????????????????
                    .map(scenesUuid -> {
                        List<DisposalScenesDTO> disposalScenesDTOS = new ArrayList<>();
                        DisposalScenesDTO disposalScenesDTO = new DisposalScenesDTO();
                        disposalScenesDTO.setScenesUuid(scenesUuid);
                        disposalScenesDTO.setQueryTypeList(Collections.singletonList(TYPE_FIREWALL));
                        try {
                            disposalScenesDTOS = disposalScenesNodeMapper.findDtoList(disposalScenesDTO);
                            if (disposalScenesDTOS == null || disposalScenesDTOS.isEmpty()) {
                                log.error("??????????????????????????????????????????????????????????????????????????????????????????");
                            }
                        } catch (Exception e) {
                            log.error("???????????????????????????",e);
                        }
                        return disposalScenesDTOS;
                    })
                    .flatMap(Collection::stream)
                    //?????????????????????
                    .collect(Collectors.toMap(DisposalScenesDTO::getDeviceUuid, value -> value,
                            (oldValue, newValue) -> newValue))
                    .values()
                    .stream()
                    //???????????????????????????????????????????????????
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
            throw new Exception("???????????????????????????");
        }
    }

    /**
     * ??????????????????-????????????-??????-ip??????
     * @param orderCenter
     * @return
     */
    private List<CommandlineDTO> createPolicyCmdTwo(DisposalOrderCenterEntity orderCenter) throws Exception {
        String srcIp = orderCenter.getSrcIp();
        String dstIp = orderCenter.getDstIp();
        String ip;
        if (StringUtils.isNotEmpty(srcIp) && StringUtils.isEmpty(dstIp)) {
            log.info("??????ip???????????????");
            ip = srcIp;
        } else if (StringUtils.isNotEmpty(dstIp) && StringUtils.isEmpty(srcIp)) {
            log.info("?????????ip???????????????");
            ip = dstIp;
        } else {
            throw new Exception("????????????ip?????????srcIp???["+srcIp+"]???dstIp???["+dstIp+"]");
        }

        log.debug("ip -> ?????? -> ???????????????");
        Set<String> deviceUuidSet = Arrays.stream(ip.split(","))
                .parallel()
                //???????????????ip??????????????????????????????
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
                //??????????????????????????????????????????
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

        log.debug("ip -> ???????????? -> ???????????????");
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
                        log.error("ip??????[{}]??????",ipAddr, e);
                    }
                    return filterListsRuleSearchDataROS;
                })
                .flatMap(Collection::stream)
                .map(FilterListsRuleSearchDataRO::getDeviceUuid)
                .collect(Collectors.toSet());

        deviceUuidSet.addAll(deviceUuidSet2);
        log.info("??????????????????[{}]", JSONObject.toJSONString(deviceUuidSet));
        return deviceUuidSet.stream()
                //???????????????????????????????????????????????????
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
     * ????????????-????????????-??????
     * @param orderCenter
     * @return
     */
    private Set<String> createRoutingCmdOne(DisposalOrderCenterEntity orderCenter) throws Exception {
        List<DisposalOrderScenesEntity> orderScenesList = disposalOrderScenesMapper.getByCenterUuid(orderCenter.getUuid());
        if (orderScenesList == null || orderScenesList.size() == 0) {
            throw new Exception("???????????????????????????");
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
     * ????????????-????????????-ip??????
     * @param orderCenter
     * @return
     */
    private Set<String> createRoutingCmdTwo(DisposalOrderCenterEntity orderCenter) throws Exception {
        String routingIp = orderCenter.getRoutingIp();
        if (StringUtils.isEmpty(routingIp)) {
            throw new Exception("routingIp????????????");
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
                        log.error("?????????ip[{}]", ip, e);
                    }
                });
        log.info("??????????????????");
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(null);
        if (deviceRO.getSuccess()) {
            log.info("????????????uuid??????");
            return deviceRO.getData().stream()
                    .parallel()
                    //???????????????????????????
                    .filter(deviceDataRO ->
                        StringUtils.equalsAny(deviceDataRO.getDeviceType(), "FIREWALL", "ROUTER")
                                && deviceDataRO.getRoutingTableIds() != null
                                && deviceDataRO.getRoutingTableIds().size() > 0
                    )
                    //??????whale???????????????????????????????????????????????????????????????
                    .filter(deviceDataRO -> {
                        List<RoutingEntriesRO> routingEntriesROS = new ArrayList<>();
                        log.trace("??????????????????????????????");
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
            throw new Exception("whale????????????");
        }
    }

    /**
     * ??????????????????????????????????????????
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
            log.error("??????????????????[{}]", deviceUuid);
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

                    //todo ???ip???0.0.0.0/0????????????ip??? "", ????????????
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

                    log.info("????????????????????????,dto:{}", JSONObject.toJSONString(commandlineDTO));
                    CmdDTO cmdDTO = new CmdDTO();
                    //??????
                    DeviceDTO device = cmdDTO.getDevice();
                    device.setDeviceUuid(deviceUuid);
                    device.setVsys(commandlineDTO.isVsys());
                    device.setVsysName(commandlineDTO.getVsysName());
                    device.setHasVsys(commandlineDTO.isHasVsys());
                    //??????
                    PolicyDTO policyDTO = cmdDTO.getPolicy();
                    if(IP6Utils.isIPv6(srcIps)){
                        //todo ipType ?????????????????????????????????????????????????????????
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

                    //??????
                    TaskDTO taskDTO = cmdDTO.getTask();
                    taskDTO.setTheme(commandlineDTO.getBusinessName());

                    //??????????????????
                    boolean done = cmdGenerator(cmdDTO);

                    if(!done) {
                        log.error("?????????????????????????????????, name:{}", name);
                        continue;
                    }

            /*        //??????????????????
                    commandService.setPreSteps(modelNumber, commandlineDTO);
                    //??????????????????
                    commandService.setAdvancedSetting(cmdDTO, modelNumber, deviceUuid);*/

                    log.info("?????????????????????????????????,dto:{}", JSONObject.toJSONString(cmdDTO));

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
                    log.error("?????????????????????[{}]??????", deviceUuid, e);
                }
            }

        }
        return disposalCommandDTOS;
    }

    /**
     * ??????????????????????????????????????????
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
            log.error("??????????????????[{}]", deviceUuid);
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
                log.error("?????????????????????[{}]??????",deviceUuid, e);
            }
        }
        return disposalCommandDTO;
    }

    /**
     * ???????????????????????????
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
     * ???????????????????????????
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
     * ??????service Json??????????????????????????????????????????
     * @return
     */
    private List<QuintupleUtils.Quintuple> serviceJsonHandle(String srcIps, String dstIps, String serviceJson) {
        List<QuintupleUtils.Quintuple> quintupleList = new ArrayList<>();
        List<ServiceDTO> serviceDTOS = ServiceDTOUtils.toList(serviceJson);
        //todo ?????????????????????????????????????????????????????????????????????????????????bug
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

    //cmd ???????????????????????????????????????
    private boolean cmdGenerator(CmdDTO cmdDTO){
        boolean done = true;

        //?????????????????????????????????????????????????????????
        vendorManager.getVendorInfo(cmdDTO);

        //?????????????????????
        ProcedureDTO procedure = cmdDTO.getProcedure();
        List<Integer> steps = procedure.getSteps();
        log.info("????????????????????????{}", JSONObject.toJSONString(cmdDTO, true));
        for (Integer step : steps) {
            log.info(String.format("?????????????????????%s??????", step));
            try {
                SubServiceEnum subService = SubServiceEnum.valueOf(step);

                String serviceName = NameUtils.getServiceDefaultName(subService.getServiceClass());
                log.info("????????????{},??????{}??????", subService.getDesc(), serviceName);
                if(cmdServiceMap.containsKey(serviceName)) {
                    CmdService service = cmdServiceMap.get(serviceName);
                    if(service != null) {
                        service.modify(cmdDTO);
                    } else {
                        log.error("???????????????{}??????????????????????????????{}", serviceName, JSONObject.toJSONString(cmdServiceMap, true));
                    }
                } else {
                    log.error("??????????????????{}!?????????????????????{}", serviceName, JSONObject.toJSONString(cmdServiceMap, true));
                }
            } catch (UnInterruptException e) {
                log.error("????????????????????????????????????", e);

                //???????????????????????????????????????????????????
                //continue;
            } catch (Exception e) {
                log.error("??????????????????????????????", e);

                //??????????????????????????????????????????
                done = false;
                break;
            }
        }

        if(!done){
            return done;
        }

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        PolicyEnum policyType = cmdDTO.getPolicy().getType();

        //?????????????????????
        vendorManager.getGenerator(policyType, deviceDTO, procedure);

        return done;
    }
}
