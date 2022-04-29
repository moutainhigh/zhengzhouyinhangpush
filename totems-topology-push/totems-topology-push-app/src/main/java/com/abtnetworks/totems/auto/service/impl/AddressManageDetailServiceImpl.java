package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.dao.mysql.AddressDetailEntryMapper;
import com.abtnetworks.totems.auto.dao.mysql.AddressManageDetailMapper;
import com.abtnetworks.totems.auto.dto.AddressCommandTaskEditableDTO;
import com.abtnetworks.totems.auto.dto.AddressManageDetailDTO;
import com.abtnetworks.totems.auto.dto.TopoNodeDO;
import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.enums.AddressStatusEnum;
import com.abtnetworks.totems.auto.enums.AutoRecommendStatusEnum;
import com.abtnetworks.totems.auto.manager.AddressManageTaskManager;
import com.abtnetworks.totems.auto.service.AddressCmdGenerateService;
import com.abtnetworks.totems.auto.service.AddressManageDetailService;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.auto.vo.AddressManagePushVO;
import com.abtnetworks.totems.common.constants.*;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.PushCopyBeanUtils;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.service.PushService;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.manager.ExternalManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.VerifyService;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceSummaryDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceSummaryRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constant.Constants.DATA;
import static com.abtnetworks.totems.common.constant.Constants.TOTAL;

/**
 * @Description 对象管理详情
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
@Service
@Slf4j
public class AddressManageDetailServiceImpl implements AddressManageDetailService {

    @Autowired
    private AddressManageDetailMapper addressManageDetailMapper;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;

    @Autowired
    private AddressDetailEntryMapper addressDetailEntryMapper;

    @Autowired
    private NodeMapper nodeMapper;

    //场景 Service
    @Autowired
    public DisposalScenesService disposalScenesService;

    @Autowired
    private AddressManageTaskManager addressManageTaskManager;

    @Autowired
    private PushService pushService;

    @Autowired
    private AddressCmdGenerateService addressCmdGenerateService;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    @Qualifier(value = "addressPushExecutor")
    private Executor addressPushExecutor;

    @Autowired
    private VerifyService verifyService;

    @Autowired
    ExternalManager externalService;

    @Autowired
    WhaleManager whaleService;

    @Override
    public ReturnT<String> insert(AddressManageDetailEntity addressManageDetailEntity) {

        if (addressManageDetailEntity == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        addressManageDetailMapper.insert(addressManageDetailEntity);
        return ReturnT.SUCCESS;
    }


    @Override
    public ReturnT<String> deleteByIds(String ids) throws Exception {
        String[] idList = ids.split(PolicyConstants.ADDRESS_SEPERATOR);

        for (String idStr : idList) {
            Integer id = Integer.valueOf(idStr);
            //删除详情对象
            addressManageTaskManager.deleteByDetailId(id, false);
        }

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> deleteSelectOne(AddressManageDetailVO vo) throws Exception {
        if ("ADDRESS".equals(vo.getAddressCategory())) {
            addressDetailEntryMapper.deleteByPrimaryKey(vo.getId());
        } else {
            addressManageTaskManager.deleteByDetailId(vo.getId(), false);
        }

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> update(AddressManageDetailEntity addressManageDetailEntity) {
        int ret = addressManageDetailMapper.update(addressManageDetailEntity);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public AddressManageDetailEntity getById(Integer id) {
        return addressManageDetailMapper.getById(id);
    }


    @Override
    public PageInfo<AddressManageDetailEntity> findList(AddressManageDetailEntity addressManageDetailEntity, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<AddressManageDetailEntity> list = addressManageDetailMapper.findList(addressManageDetailEntity);
        PageInfo<AddressManageDetailEntity> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public AddressManageDetailVO getDetailById(Integer id) {

        AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(id);

        List<AddressManageDetailEntity> childList = addressManageDetailMapper.getByParentId(id);
        AddressManageDetailVO detailVo = PushCopyBeanUtils.copy(detailEntity, AddressManageDetailVO.class);
        List<AddressManageDetailVO> detailVOList = PushCopyBeanUtils.copyList(childList, AddressManageDetailVO.class);
        detailVo.getChild().addAll(detailVOList);
        detailVo.setAddressAdd(null);
        detailVo.setAddressDel(null);
        if (null != detailEntity.getParentId()) {
            AddressManageDetailEntity parentVo = addressManageDetailMapper.getById(detailEntity.getParentId());
            detailVo.setParentName(parentVo.getAddressName());
        }

        if (StringUtils.isNotEmpty(detailVo.getScenesUuid())) {
            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(detailVo.getScenesUuid());
            if (scenesEntity != null) {
                detailVo.setScenesName(scenesEntity.getName());
            }
        }

        List<AddressDetailEntryEntity> detailEntryEntityList = addressDetailEntryMapper.getByDetailId(id);
        if (CollectionUtils.isNotEmpty(detailEntryEntityList)) {
            for (AddressDetailEntryEntity detailEntryEntity : detailEntryEntityList) {
                AddressManageDetailVO detailVO1 = new AddressManageDetailVO();
                detailVO1.setId(detailEntryEntity.getId());
                detailVO1.setAddressName(detailEntryEntity.getAddressName());
                detailVO1.setAddressType(detailEntryEntity.getAddressType());
                detailVO1.setCreateTime(detailEntryEntity.getCreateTime());
                detailVo.getChild().add(detailVO1);
            }
        }

        //更新命令行生成及下发状态
        String pushId = detailEntity.getPushId();
        if (StringUtils.isEmpty(pushId)) {
            detailVo.setPushStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_NOT_START.getCode());
        } else {
            Boolean hasFail = false;
            String[] pushIds = pushId.split(PolicyConstants.ADDRESS_SEPERATOR);
            List<Integer> pushIdList = Arrays.asList(pushIds).stream().map(e -> Integer.valueOf(e)).collect(Collectors.toList());
            List<CommandTaskEditableEntity> commandTaskEditableList = new ArrayList<>();
            for (Integer pId : pushIdList) {
                CommandTaskEditableEntity editTaskEntity = commandTaskEdiableMapper.selectByPrimaryKey(pId);
                String command = editTaskEntity.getCommandline();
                if (StringUtils.isNotBlank(command) && command.startsWith("无法生成该设备的命令行")) {
                    log.info(String.format("[%s]开始下发失败，存在未生成命令行的设备！", editTaskEntity.getTheme()));
                    hasFail = true;
                    break;
                }
                commandTaskEditableList.add(editTaskEntity);
            }
            if (hasFail) {
                detailVo.setPushStatus(AutoRecommendStatusEnum.GENERATE_COMMANDLINE_FAIL.getCode());
            } else {
                int pushStatus;
                int pushStatusInTaskList = recommendTaskService.getPushStatusInTaskList(commandTaskEditableList);
                if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_NOT_START) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_NOT_START.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_PUSHING) {
                    pushStatus = AutoRecommendStatusEnum.PUSHING.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_FINISHED) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_SUCCESS.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_STATUS_FAILED) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_FAIL.getCode();
                } else if (pushStatusInTaskList == PushStatusConstans.PUSH_INT_PUSH_QUEUED) {
                    pushStatus = AutoRecommendStatusEnum.PUSH_WAITING.getCode();
                } else {
                    pushStatus = AutoRecommendStatusEnum.PUSH_SUCCESS_PARTS.getCode();
                }
                detailVo.setPushStatus(pushStatus);
            }
        }

        return detailVo;
    }

    @Override
    public ReturnT<String> saveDetail(AddressManageDetailVO vo) throws Exception {
        //保存详情信息
        AddressManageDetailDTO addressManageDetailDTO = returnAddressDTOForDelAdd(vo);
        //生成命令行
        generateCmd(vo.getId(), addressManageDetailDTO.getPushId(), addressManageDetailDTO.getScenesUuid());

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> checkAddress(AddressManageDetailVO vo) throws Exception {
        for (Integer detailId : vo.getIdList()) {
            //设置id
            vo.setId(detailId);
            //保存详情信息
            AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(vo.getId());

            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(detailEntity.getScenesUuid());
            if (scenesEntity == null) {
                log.error(String.format("场景UUID：%s 查询场景不存在。", detailEntity.getScenesUuid()));
                return new ReturnT<>(ReturnT.FAIL_CODE, "场景不存在,请先选择场景");
            }
            //生成命令行
            generateCmd(detailId, detailEntity.getPushId(), detailEntity.getScenesUuid());
        }

        return ReturnT.SUCCESS;
    }

    @Override
    public List<AddressManagePushVO> getPushCmdByScenes(AddressManageDetailVO vo) {
        List<AddressManagePushVO> addressManagePushVOList = new ArrayList<>();
        //获取详情
        AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(vo.getId());
        //获取下发id
        String pushId = detailEntity.getPushId();
        if (StringUtils.isNotEmpty(pushId)) {
            String[] pushIds = pushId.split(PolicyConstants.ADDRESS_SEPERATOR);

            for (String str : pushIds) {
                AddressManagePushVO pushVO = new AddressManagePushVO();
                BeanUtils.copyProperties(detailEntity, pushVO);
                Integer id = Integer.valueOf(str);
                CommandTaskEditableEntity entity = commandTaskEdiableMapper.selectByPrimaryKey(id);
                AddressCommandTaskEditableDTO cmdDto = new AddressCommandTaskEditableDTO();
                BeanUtils.copyProperties(entity, cmdDto);
                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(entity.getDeviceUuid());
                String deviceName = String.format("未知设备(%s)", entity.getDeviceUuid());
                if(nodeEntity != null) {
                    deviceName = String.format("%s(%s)", nodeEntity.getDeviceName(), nodeEntity.getIp());
                }
                cmdDto.setDeviceName(deviceName);
                pushVO.setCommandTaskEditableEntity(cmdDto);

                addressManagePushVOList.add(pushVO);
            }
        }

        return addressManagePushVOList;
    }

    @Override
    public ReturnT<String> pushCmd(AddressManagePushVO vo) {
        boolean gatherNow = null == vo.getGatherNow() ? false : vo.getGatherNow();
        for (Integer id : vo.getIdList()) {
            AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(id);
            if (null == detailEntity) {
                log.warn("根据ID：{} 未查询到对象管理详情任务", id);
                continue;
            }

            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(detailEntity.getScenesUuid());
            if (scenesEntity == null) {
                log.error(String.format("场景UUID：%s 查询场景不存在。", detailEntity.getScenesUuid()));
                return new ReturnT<>(ReturnT.FAIL_CODE, "场景不存在,请先选择场景");
            }

            if (StringUtils.isEmpty(detailEntity.getPushId())) {
                log.warn("根据ID：{} 未查询到下发任务", id);
                continue;
            }

            // 命令行生成失败、下发等待中、下发中、策略已开通不允许下发
            if(detailEntity.getStatus().equals(AddressStatusEnum.INIT_STATUS.getCode())) {
                log.error("地址：{} 状态为：{}，不允许下发!", detailEntity.getAddressName(), AddressStatusEnum.getDescByCode(detailEntity.getStatus()));
                continue;
            }
            try {
                // 更新状态为下发中
                AddressManageDetailEntity updateEntity = new AddressManageDetailEntity();
                updateEntity.setId(id);
                updateEntity.setStatus(AddressStatusEnum.PUSHING.getCode());
                addressManageDetailMapper.updateByPrimaryKey(updateEntity);

                String pushId = detailEntity.getPushId();
                String[] pushIds = pushId.split(PolicyConstants.ADDRESS_SEPERATOR);
                List<Integer> pushIdList = Arrays.asList(pushIds).stream().map(e -> Integer.valueOf(e)).collect(Collectors.toList());

                List<String>  deviceUuidList = new ArrayList<>();
                // 获取该地址对象所有命令行数据的设备uuid
                getAllDeviceUuid(pushIdList, deviceUuidList);
                deviceUuidList = deviceUuidList.stream().distinct().collect(Collectors.toList());

                // 验证设备采集状态
                boolean hasGatheringDevices = verifyService.hasGatheringDevices(deviceUuidList);
                if (hasGatheringDevices) {
                    // 如果存在设备正在采集 或者有采集失败的设备直接提示
                    return new ReturnT(ReturnCode.OBJECT_PUSH_ERROR, ReturnCode.getMsg(ReturnCode.OBJECT_PUSH_ERROR));
                }

                CountDownLatch latch = new CountDownLatch(pushIdList.size());
                for (Integer pushIdItem : pushIdList) {
                    List<CommandTaskEditableEntity> commandTaskEditableList = new ArrayList<>();
                    CommandTaskEditableEntity editTaskEntity = commandTaskEdiableMapper.selectByPrimaryKey(pushIdItem);
                    String command = editTaskEntity.getCommandline();
                    if (StringUtils.isNotBlank(command) && command.startsWith("无法生成该设备的命令行")) {
                        log.info(String.format("[%s]开始下发失败，存在未生成命令行的设备！", editTaskEntity.getTheme()));
                        latch.countDown();
                        break;
                    }
                    commandTaskEditableList.add(editTaskEntity);
                    String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss") + "-" + IdGen.randomBase62(6);
                    addressPushExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, detailEntity.getAddressName(), "对象管理下发", new Date())) {
                        @Override
                        protected void start() throws Exception {
                            try {

                                CommandTaskDTO taskDTO = new CommandTaskDTO();
                                taskDTO.setRevert(false);
                                taskDTO.setList(commandTaskEditableList);

                                taskDTO.setTaskId(commandTaskEditableList.get(0).getTaskId());
                                taskDTO.setTheme(detailEntity.getAddressName());

                                // 下发命令行
                                pushService.pushCommand(taskDTO);
                                //计数器减1
                                latch.countDown();
                            } catch (Exception e) {
                                log.error("对象管理下发异常，异常原因：", e);
                                latch.countDown();
                                throw e;
                            }
                        }
                    });
                }


                try {
                    latch.await();
                } catch (Exception e) {
                    log.error("对象管理下发异常:{}",e);
                }

                // 校验统计 重新采集本次对象下发所涉及到的场景里面的所有在线采集设备
                if(gatherNow){
                    log.info("开始重新采集设备uuids:{}", JSONObject.toJSONString(deviceUuidList));
                    verifyDoGather(deviceUuidList);
                }

                //下发完成后更改为初始化状态
                detailEntity.setStatus(AddressStatusEnum.INIT_STATUS.getCode());
                addressManageDetailMapper.updateByPrimaryKey(detailEntity);

            } catch (Exception e) {
                log.error("自动开通工单下发异常，异常原因：", e);
                detailEntity.setStatus(AddressStatusEnum.PUSH_FAIL.getCode());
                addressManageDetailMapper.updateByPrimaryKey(detailEntity);
                throw e;
            }

        }
        return ReturnT.SUCCESS;
    }

    /**
     * 获取该对象 关联的所有命令行数据的uuid
     * @param pushIdList
     * @param deviceUuidList
     */
    private void getAllDeviceUuid(List<Integer> pushIdList, List<String> deviceUuidList) {
        for (Integer pushIdItem : pushIdList) {
            CommandTaskEditableEntity editTaskEntity = commandTaskEdiableMapper.selectByPrimaryKey(pushIdItem);
            String command = editTaskEntity.getCommandline();
            if (StringUtils.isNotBlank(command) && command.startsWith("无法生成该设备的命令行")) {
                log.info(String.format("[%s]开始下发失败，存在未生成命令行的设备！", editTaskEntity.getTheme()));
                break;
            }
            deviceUuidList.add(editTaskEntity.getDeviceUuid());
        }
    }


    /**
     * 重新采集
     * @param deviceUuidList
     */
    private void verifyDoGather(List<String> deviceUuidList) {
        List<String> gatherIdList = new ArrayList<>();
        for(String deviceUuid: deviceUuidList) {
            NodeEntity nodeEntity = recommendTaskService.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                log.error(String.format("设备(%s)节点已被删除...", deviceUuid));
                continue;
            }

            //设备来源：1手工导入2采集
            if(nodeEntity.getOrigin() == 1) {
                log.error(String.format("设备(%s)[%s(%s)]的手动采集设备...", deviceUuid, nodeEntity.getDeviceName(), nodeEntity.getIp()));
                continue;
            }
            //若设备为虚设备，则下发设备信息获取其主设备的
            DeviceRO deviceRO = whaleService.getDeviceByUuid(deviceUuid);
            int gatherId = nodeEntity.getId();
            if(deviceRO == null || deviceRO.getData() == null ||deviceRO.getData().size() ==0 ) {
                log.error("设备信息为空，不查询是否为虚设备");

            }else{
                DeviceDataRO deviceData = deviceRO.getData().get(0);
                if (deviceData.getIsVsys() != null) {
                    //不等于null就是虚墙,获取主墙的设备uuid
                    String deviceUuidRoot =  deviceData.getRootDeviceUuid();
                    log.info("采集时发现虚墙{}=设备{}uuid={}，就使用主墙的uuid={}",deviceData.getIsVsys(),deviceData.getVsysName(),deviceData.getUuid(),deviceUuidRoot);
                    NodeEntity  rootNode =  recommendTaskService.getTheNodeByUuid(deviceUuidRoot);
                    if(rootNode == null) {
                        log.error(String.format("设备(%s)节点已被删除...", deviceUuidRoot));
                        continue;
                    }
                    gatherId  = rootNode.getId();
                }
            }
            gatherIdList.add(String.valueOf(gatherId));

        }
        //去重，重复的id不能重复采集，避免虚墙和主墙存在重复
        if(CollectionUtils.isNotEmpty(gatherIdList)){
            gatherIdList.stream().distinct().forEach(p->{
                externalService.doGather(p);
            });
        }
    }

    @Override
    public ReturnT<Map<String, Map<String, Map<String, Object>>>> getDeviceByScenes(AddressManageDetailVO vo) {
        Map<String, Map<String, Map<String, Object>>> deviceTypeMap = new HashMap<>(16);
        Map<String, DeviceSummaryDataRO> deviceNameMap = new HashMap<>(16);

        List<NodeEntity> nodeEntityList = new ArrayList<>();
        List<DisposalScenesDTO> scenesDTOList = disposalScenesService.findByScenesUuid(vo.getScenesUuid());
        if (CollectionUtils.isNotEmpty(scenesDTOList)) {
            List<String> idList = new ArrayList<>();
            for (DisposalScenesDTO scenesDTO : scenesDTOList) {
                String deviceUuid = scenesDTO.getDeviceUuid();
                NodeEntity theNodeByUuid = nodeMapper.getTheNodeByUuid(deviceUuid);
                if( null == theNodeByUuid) {
                    return new ReturnT(ReturnT.FAIL_CODE,"场景中设备已下线");
                }
                nodeEntityList.add(theNodeByUuid);
            }
        }

        DeviceSummaryRO deviceSummaryRO = whaleDeviceObjectClient.getDevicesSummaryRO(true, true);
        if (deviceSummaryRO.getSuccess()) {
            List<DeviceSummaryDataRO> deviceSummaryDataROS = deviceSummaryRO.getData();
            deviceNameMap = deviceSummaryDataROS.stream()
                    .collect(Collectors.toMap(DeviceSummaryDataRO::getUuid, y -> y));
        }

        List<TopoNodeDO> topoNodeDOS = PushCopyBeanUtils.copyList(nodeEntityList, TopoNodeDO.class);
        for (TopoNodeDO topoNodeDO : topoNodeDOS) {
            if (topoNodeDO.getUuid() == null) {
                log.info("设备:{}的uuid为null,跳过该设备", topoNodeDO.getDeviceName());
                continue;
            }
            DeviceSummaryDataRO deviceSummaryDataRO = deviceNameMap.get(topoNodeDO.getUuid());
            if (deviceSummaryDataRO == null) {
                log.error("设备:[{}]在mysql中存在，在mongo数据库中不存在，数据不同步", topoNodeDO.getUuid());
                throw new RuntimeException("设备:["+topoNodeDO.getUuid()+"]在mysql中存在，在mongo数据库中不存在，数据不同步");
            }
            if (StringUtils.equals("true", deviceSummaryDataRO.getIsVsys())) {
                topoNodeDO.setIsVsys("true");
                topoNodeDO.setName(deviceSummaryDataRO.getVsysName());
            } else {
                topoNodeDO.setName(deviceSummaryDataRO.getName());
            }
            String type = topoNodeDO.getType();
            Map<String, Map<String, Object>> vendorNameMap;
            log.debug("处理是否存在设备类型");
            if (deviceTypeMap.containsKey(type)) {
                vendorNameMap = deviceTypeMap.get(type);
            } else {
                vendorNameMap = new HashMap<>(16);
                deviceTypeMap.put(type, vendorNameMap);
            }
            String vendorName = topoNodeDO.getVendorName();
            Map<String, Object> dataMap;
            List<TopoNodeDO> topoNodeDOList;
            log.debug("处理是否存在厂商类型");
            if (vendorNameMap.containsKey(vendorName)) {
                dataMap = vendorNameMap.get(vendorName);
                int total = (int) dataMap.get(TOTAL);
                dataMap.put(TOTAL, total + 1);
                topoNodeDOList = (List<TopoNodeDO>) dataMap.get(DATA);
                topoNodeDOList.add(topoNodeDO);
            } else {
                dataMap = new HashMap<>(16);
                topoNodeDOList = new ArrayList<>();
                topoNodeDOList.add(topoNodeDO);
                dataMap.put(TOTAL, 1);
                dataMap.put(DATA, topoNodeDOList);
                vendorNameMap.put(vendorName, dataMap);
            }
        }
        return new ReturnT(deviceTypeMap);
    }

    @Override
    public List<AddressManageDetailDTO> findAddressByName(String name) {
        List<AddressManageDetailDTO> rList = Lists.newArrayList();
        List<AddressManageDetailEntity> addressByName = addressManageDetailMapper.findAddressByName(name, DeviceObjectTypeEnum.NETWORK_OBJECT.getCode());
        Iterator<AddressManageDetailEntity> iterator = addressByName.iterator();
        while ( iterator.hasNext() ){
            AddressManageDetailEntity entity = iterator.next();
            List<AddressDetailEntryEntity> list = addressDetailEntryMapper.getByDetailId(entity.getId());
            if ( CollectionUtils.isEmpty( list ) ){
                iterator.remove();
                continue;
            }
            AddressManageDetailDTO dto = new AddressManageDetailDTO();
            BeanUtils.copyProperties( entity, dto);
            dto.setAddressDetailEntryList(list);
            rList.add(dto);
        }

        return rList;
    }

    @Override
    public ReturnT<List<AddressDetailEntryEntity>> findAddressEntityByName(String name) {
        List<AddressManageDetailEntity> addressByName = addressManageDetailMapper.getAddressByName(name, DeviceObjectTypeEnum.NETWORK_OBJECT.getCode());
        List<AddressDetailEntryEntity> byDetailId = new ArrayList<>();
        if ( CollectionUtils.isNotEmpty( addressByName ) ){
             byDetailId = addressDetailEntryMapper.getByDetailId(addressByName.get(0).getId());
        }
        return new ReturnT( byDetailId );
    }


    /**
     * 比较场景下设备的地址生成命令行
     * @param id 主键
     * @param scenesUuid 场景uuid
     * @param secondDetailList 第二级对象
     * @param taskEditableEntityList 命令行对象List
     */
    private void compareScenesAddress (Integer id, String scenesUuid, List<AddressManageDetailVO> secondDetailList, List<CommandTaskEditableEntity> taskEditableEntityList) {
        //判断场景是否存在
        List<DisposalScenesDTO> scenesDTOList = null;
        DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(scenesUuid);
        if (null == scenesEntity) {
            log.error(String.format("生成命令行场景UUID：%s 查询场景不存在。", scenesUuid));
        } else {
            scenesDTOList = disposalScenesService.findByScenesUuid(scenesUuid);
            if (CollectionUtils.isNotEmpty(scenesDTOList)) {
                AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(id);

                List<String> idList = new ArrayList<>();
                for (DisposalScenesDTO scenesDTO : scenesDTOList) {
                    String deviceUuid = scenesDTO.getDeviceUuid();
                    //新增或更新命令行对象
                    CommandTaskEditableEntity entity = compareAndAddDifferentAddr(secondDetailList, deviceUuid, taskEditableEntityList, detailEntity.getAddressName());
                    if (StringUtils.isNotEmpty(entity.getCommandline())) {
                        idList.add(String.valueOf(entity.getId()));
                    }
                }
                //更新详情对象状态
                AddressManageDetailEntity updateEntity = new AddressManageDetailEntity();
                updateEntity.setId(id);
                if (ObjectUtils.isNotEmpty(idList)) {
                    String pushIds = idList.stream().collect(Collectors.joining(PolicyConstants.ADDRESS_SEPERATOR));
                    updateEntity.setPushId(pushIds);
                    updateEntity.setStatus(AddressStatusEnum.ISSUED_STATUS.getCode());
                } else {
                    updateEntity.setStatus(AddressStatusEnum.INIT_STATUS.getCode());
                }
                addressManageDetailMapper.updateByPrimaryKey(updateEntity);
            }
        }
    }


    /**
     * 生成命令行
     * @param detailId 地址详情id
     * @param pushIdStr 下发id集
     * @param scenesUuid 场景uuid
     */
    private void generateCmd(Integer detailId, String pushIdStr, String scenesUuid) {
        List<CommandTaskEditableEntity> taskEditableEntityList = new ArrayList<>();
        //获取原下发记录
        if (StringUtils.isNotEmpty(pushIdStr)) {
            String[] pushIds = pushIdStr.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String pushId : pushIds) {
                Integer id = Integer.valueOf(pushId);
                CommandTaskEditableEntity editableEntity = commandTaskEdiableMapper.selectByPrimaryKey(id);
                taskEditableEntityList.add(editableEntity);
            }
        }

        //获取当前地址所有下一级地址
        List<AddressManageDetailVO> detailList = getDetailListById(detailId);
        Collections.reverse(detailList);

        //比较场景下设备的地址生成命令行
        if (CollectionUtils.isNotEmpty(detailList)) {
            compareScenesAddress(detailId, scenesUuid, detailList, taskEditableEntityList);
        }

    }

    /**
     * 获取地址对象组层级
     * @param id
     * @return
     */
    private List<AddressManageDetailVO> getDetailListById(Integer id) {
        //获取二级对象地址组
        List<AddressManageDetailVO> returnList = new ArrayList<>();
        //获取对象详情list
        AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(id);
        AddressManageDetailVO detailVO = new AddressManageDetailVO();
        BeanUtils.copyProperties(detailEntity, detailVO);
        //获取下一级对象地址组
        List<AddressManageDetailEntity> childList = addressManageDetailMapper.getByParentId(id);
        //获取下一级地址条目
        List<AddressDetailEntryEntity> detailEntryList = addressDetailEntryMapper.getByDetailId(id);
        List<AddressManageDetailVO> entryChildList = PushCopyBeanUtils.copyList(detailEntryList, AddressManageDetailVO.class);
        detailVO.getChild().addAll(entryChildList);
        if (CollectionUtils.isNotEmpty(childList) || CollectionUtils.isNotEmpty(entryChildList)) {
            getNextAddress(returnList, detailVO, childList);
        }

        return returnList;
    }

    /**
     * 获取下一级地址对象
     * @param detailVOList
     * @param detailVO
     * @param childList
     */
    private void getNextAddress(List<AddressManageDetailVO> detailVOList, AddressManageDetailVO detailVO, List<AddressManageDetailEntity> childList) {
        //将下一级地址对象设置为本级地址
        List<AddressManageDetailVO> nextList = PushCopyBeanUtils.copyList(childList, AddressManageDetailVO.class);
        detailVO.getChild().addAll(nextList);
        detailVOList.add(detailVO);
        for (AddressManageDetailEntity detailEntity1 : childList) {
            AddressManageDetailVO nextDetailVO = new AddressManageDetailVO();
            BeanUtils.copyProperties(detailEntity1, nextDetailVO);
            //获取下一级地址组
            List<AddressManageDetailEntity> nextChildList = addressManageDetailMapper.getByParentId(detailEntity1.getId());
            //获取下一级地址条目
            List<AddressDetailEntryEntity> detailEntryList = addressDetailEntryMapper.getByDetailId(detailEntity1.getId());
            List<AddressManageDetailVO> entryChildList = PushCopyBeanUtils.copyList(detailEntryList, AddressManageDetailVO.class);
            nextDetailVO.getChild().addAll(entryChildList);
            if (CollectionUtils.isNotEmpty(nextChildList) || CollectionUtils.isNotEmpty(entryChildList)) {
                getNextAddress(detailVOList, nextDetailVO, nextChildList);
            }
        }
    }

    /**
     * 返回新增地址值后的地址详情DTO并保存信息
     * @param vo 地址详情vo对象
     * @return
     */
    private AddressManageDetailDTO returnAddressDTOForDelAdd(AddressManageDetailVO vo) {
        AddressManageDetailEntity entity = addressManageDetailMapper.getById(vo.getId());
        AddressManageDetailDTO detailDTO = new AddressManageDetailDTO();
        BeanUtils.copyProperties(entity, detailDTO);

        String addressName = vo.getAddressName();
        //本次删除的地址条目
        String addressDel = vo.getAddressDel();
        if (StringUtils.isNotEmpty(addressDel)) {
            String[] delIds = addressDel.split(PolicyConstants.ADDRESS_SEPERATOR);
            //删除子级对象
            for (String idStr :  delIds) {
                Integer id = Integer.valueOf(idStr);
                addressManageDetailMapper.delete(id);
            }
        }
        //本次新增的地址条目
        String addressAdd = vo.getAddressAdd();
        if (StringUtils.isNotEmpty(addressName)) {
            String addStr = StringUtils.isEmpty(addressAdd) ? "" : (PolicyConstants.ADDRESS_SEPERATOR + addressAdd);
            addressName = addressName + addStr;
        } else {
            addressName = addressAdd;
        }
        detailDTO.setAddressName(addressName);

        //新增子级对象
        addressManageTaskManager.addDetailEntityByAddChild(detailDTO, addressAdd);
        return detailDTO;
    }

    /**
     * 比较地址对象的不同生成命令行
     * @param detailVOList 地址详情VO对象
     * @param deviceUuid 设备uuid
     * @param taskEditableEntityList 当前地址详情下存在的下发对象
     */
    private CommandTaskEditableEntity compareAndAddDifferentAddr(List<AddressManageDetailVO> detailVOList, String deviceUuid, List<CommandTaskEditableEntity> taskEditableEntityList, String theme) {
        //命令行对象
        CommandTaskEditableEntity editableEntity = new CommandTaskEditableEntity();

        //是否已存在设备命令行
        boolean hasDevice = false;
        if (ObjectUtils.isNotEmpty(taskEditableEntityList)) {
            for (CommandTaskEditableEntity editableEntity1 : taskEditableEntityList) {
                if (deviceUuid.equals(editableEntity1.getDeviceUuid())) {
                    editableEntity.setId(editableEntity1.getId());
                    hasDevice = true;
                }
            }
        }
        //获取设备型号
        String modelNumber = nodeMapper.getDeviceModelNumber(deviceUuid);
        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);

        DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();
        searchDTO.setDeviceUuid(deviceUuid);

        //获取地址和地址内容的映射map
        Map<String, List<String>> allAddrMap = addressManageTaskManager.getAddressMap(searchDTO);

        //新增值
        List<String> addressAddList = new ArrayList<>();
        //删除值
        List<String> addressDelList = new ArrayList<>();

        List<String> hasAdressList = new ArrayList<>();

        //比较地址对象，生成命令行，拼接在sb对象中
        StringBuilder sb = new StringBuilder();

        for (AddressManageDetailVO detailVO : detailVOList) {

            addressCmdGenerateService.generateCmdCompleteCompare(allAddrMap, sb, detailVO, addressAddList, addressDelList, deviceModelNumberEnum, hasAdressList);

        }
        //组装默认参数
        if (sb.length()>0) {
            DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
            DeviceDataRO deviceData = deviceRO.getData().get(0);
            boolean isVsys = false;
            String vsysName = "";
            if (deviceData.getIsVsys() != null) {
                isVsys = deviceData.getIsVsys();
                vsysName = deviceData.getVsysName();
            }
            //前置命令行
            addressCmdGenerateService.getPreCmd(deviceModelNumberEnum, sb, isVsys, vsysName);
            //后置命令行
            addressCmdGenerateService.getPostCmd(deviceModelNumberEnum, sb);

            if (sb.indexOf("该设备暂不支持") > 0) {
                sb = new StringBuilder("该设备暂不支持");
            }
        }
        setCommandTaskEditableEntity(editableEntity,deviceUuid,sb.toString(),theme);
        //添加新增地址
        if (ObjectUtils.isNotEmpty(addressAddList)) {
            String addressAdd = addressAddList.stream().collect(Collectors.joining(PolicyConstants.ADDRESS_SEPERATOR));
            editableEntity.setAddressAdd(addressAdd);
        } else {
            editableEntity.setAddressAdd("");
        }
        //添加删除地址
        if (ObjectUtils.isNotEmpty(addressDelList)) {
            String addressDel = addressDelList.stream().collect(Collectors.joining(PolicyConstants.ADDRESS_SEPERATOR));
            editableEntity.setAddressDel(addressDel);
        } else {
            editableEntity.setAddressDel("");
        }

        editableEntity.setMovePosition(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP);
        if (hasDevice) {
            commandTaskEdiableMapper.updateByPrimaryKeySelective(editableEntity);
        } else {
            //组装建议策略
            RecommendTaskEntity recommendTaskEntity = new RecommendTaskEntity();
            setRecommandTaskEntity(recommendTaskEntity);
            recommendTaskMapper.insert(recommendTaskEntity);
            editableEntity.setTaskId(recommendTaskEntity.getId());
            commandTaskEdiableMapper.insert(editableEntity);
        }

        return editableEntity;
    }


    /**
     * 设置命令行对象信息
     * @param entity 下发任务实体
     * @param deviceUuid 设备uuid
     * @param commandline 命令行
     * @param addressName 地址名称
     */
    private void setCommandTaskEditableEntity(CommandTaskEditableEntity entity, String deviceUuid, String commandline, String addressName) {
        entity.setTheme(addressName);
        entity.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
        entity.setTaskType(PolicyConstants.ADDRESS_MANAGE);
        entity.setCommandline(commandline);
        entity.setCreateTime(new Date());
        entity.setStatus(PushConstants.PUSH_INT_PUSH_RESULT_STATUS_NOT_START);
        entity.setDeviceUuid(deviceUuid);
        entity.setPushResult("");
        entity.setErrorMsg("");
        entity.setBranchLevel("00");
    }

    /**
     * 设置仿真对象信息
     * @param recommendTaskEntity
     */
    private void setRecommandTaskEntity(RecommendTaskEntity recommendTaskEntity) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String orderNumber= "A" + simpleDateFormat.format(date);
        recommendTaskEntity.setTheme(orderNumber);
        recommendTaskEntity.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
        recommendTaskEntity.setOrderNumber(orderNumber);
        recommendTaskEntity.setCreateTime(date);
        recommendTaskEntity.setTaskType(PolicyConstants.ADDRESS_MANAGE);
        recommendTaskEntity.setBranchLevel("00");
        recommendTaskEntity.setStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);
        //数据库中限制不能为空，给个默认值
        recommendTaskEntity.setSrcIp(PolicyConstants.IPV4_ANY);
        recommendTaskEntity.setDstIp(PolicyConstants.IPV4_ANY);
        recommendTaskEntity.setIpType(IpTypeEnum.IPV4.getCode());
    }

}
