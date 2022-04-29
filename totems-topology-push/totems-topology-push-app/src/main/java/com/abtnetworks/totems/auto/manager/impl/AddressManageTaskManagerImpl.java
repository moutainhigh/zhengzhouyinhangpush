package com.abtnetworks.totems.auto.manager.impl;

import com.abtnetworks.totems.auto.dao.mysql.AddressDetailEntryMapper;
import com.abtnetworks.totems.auto.dao.mysql.AddressManageDetailMapper;
import com.abtnetworks.totems.auto.dao.mysql.AddressManageTaskMapper;
import com.abtnetworks.totems.auto.dto.AddressManageDetailDTO;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.entity.AddressManageTaskEntity;
import com.abtnetworks.totems.auto.enums.AddressLevelEnum;
import com.abtnetworks.totems.auto.enums.AddressStatusEnum;
import com.abtnetworks.totems.auto.manager.AddressManageTaskManager;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.PageDTO;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.enums.ObjectSearchTypeEnum;
import com.abtnetworks.totems.common.enums.SearchRangeOpEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.PushCopyBeanUtils;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchAddressDTO;
import com.abtnetworks.totems.whale.baseapi.ro.NetWorkGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
@Service
public class AddressManageTaskManagerImpl implements AddressManageTaskManager {

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    private AddressManageDetailMapper addressManageDetailMapper;

    @Autowired
    private CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;

    @Autowired
    private AddressManageTaskMapper addressManageTaskMapper;

    @Autowired
    private AddressDetailEntryMapper addressDetailEntryMapper;

    @Override
    public ResultRO<List<NetWorkGroupObjectShowVO>> searchAddress(SearchAddressDTO searchAddressDTO, PageDTO pageDTO) {
        ResultRO<List<NetWorkGroupObjectRO>> dataResultRO = whaleDeviceObjectClient.searchAddress(searchAddressDTO, pageDTO);
        return formatNetWorkGroupRO(dataResultRO);
    }

    @Override
    public List<AddressManageDetailEntity> getDetailByTaskId(Integer taskId) {
        return addressManageDetailMapper.getByTaskId(taskId);
    }

    @Override
    public ResultRO<List<NetWorkGroupObjectShowVO>> getAddressList(DeviceObjectSearchDTO searchDTO, DeviceObjectTypeEnum objectTypeEnum) {
        ResultRO<List<NetWorkGroupObjectRO>> deviceFilterlistResultRO = null;
        if (objectTypeEnum.getCode().equalsIgnoreCase(DeviceObjectTypeEnum.NETWORK_OBJECT.getCode())) {
            deviceFilterlistResultRO = whaleDeviceObjectClient.getNetWorkObject(searchDTO);
        } else {
            deviceFilterlistResultRO = whaleDeviceObjectClient.getNetWorkGroupObject(searchDTO);
        }
        List<NetWorkGroupObjectRO> data = deviceFilterlistResultRO.getData();
        if (ObjectUtils.isNotEmpty(data)) {
            List<NetWorkGroupObjectRO> collect = data.stream().filter(s -> s.getDeviceNetworkType() == null).collect(Collectors.toList());
            deviceFilterlistResultRO.setData(collect);
            //deviceFilterlistResultRO.setTotal(collect.size());
        }
        return formatNetWorkGroupRO(deviceFilterlistResultRO);
    }

    @Override
    public void addDetailEntityByAddChild(AddressManageDetailDTO detail, String addressAdd) {
        if (ObjectUtils.isEmpty(detail) || StringUtils.isEmpty(addressAdd)) {
            return;
        }

        String[] addressAdds = addressAdd.split(PolicyConstants.ADDRESS_SEPERATOR);

        for (String addStr : addressAdds) {
            AddressManageDetailEntity entity = new AddressManageDetailEntity();
            BeanUtils.copyProperties(detail, entity);

            entity.setAddressName(addStr);
            entity.setParentId(detail.getId());
            entity.setAddressLevel(detail.getAddressLevel()+1);
            entity.setUuid(IdGen.uuid());
            entity.setCreateTime(new Date());
            entity.setStatus(AddressStatusEnum.INIT_STATUS.getCode());

            addressManageDetailMapper.insert(entity);
        }
    }

    @Override
    public List<AddressManageDetailVO> getParentDetailListByTaskId(Integer taskId) {
        //获取对象详情list
        List<AddressManageDetailEntity> detailList = this.getDetailByTaskId(taskId);

        AddressManageTaskEntity byId = addressManageTaskMapper.getById(taskId);
        for (AddressManageDetailEntity entity : detailList) {
            entity.setScenesUuid( byId.getScenesUuid() );
        }

        //将entity对象复制给vo对象
        List<AddressManageDetailVO> originalList = PushCopyBeanUtils.copyList(detailList, AddressManageDetailVO.class);
        //获取各级对象地址组
        List<AddressManageDetailVO> parentList = originalList.stream().filter(s -> AddressLevelEnum.ADDRESS_PARENT.getCode().equals(s.getAddressLevel())).collect(Collectors.toList());
        List<AddressManageDetailVO> firstList = originalList.stream().filter(s -> AddressLevelEnum.ADDRESS_FIRST.getCode().equals(s.getAddressLevel())).collect(Collectors.toList());
        List<AddressManageDetailVO> secondList = originalList.stream().filter(s -> AddressLevelEnum.ADDRESS_SECOND.getCode().equals(s.getAddressLevel())).collect(Collectors.toList());
        List<AddressManageDetailVO> thirdList = originalList.stream().filter(s -> AddressLevelEnum.ADDRESS_THIRD.getCode().equals(s.getAddressLevel())).collect(Collectors.toList());
        List<AddressManageDetailVO> fourList = originalList.stream().filter(s -> AddressLevelEnum.ADDRESS_FOUR.getCode().equals(s.getAddressLevel())).collect(Collectors.toList());
        List<AddressManageDetailVO> fiveList = originalList.stream().filter(s -> AddressLevelEnum.ADDRESS_FIVE.getCode().equals(s.getAddressLevel())).collect(Collectors.toList());

        //将第五级对象加到第四级
        this.addChildListToCurrent(fourList, fiveList);
        //将第四级对象加到第三级
        this.addChildListToCurrent(thirdList, fourList);
        //将第三级对象加到第二级
        this.addChildListToCurrent(secondList, thirdList);
        //将第二级对象加到第一级
        this.addChildListToCurrent(firstList, secondList);
        //将第一级对象加到顶级对象
        this.addChildListToCurrent(parentList, firstList);

        return parentList;
    }

    @Override
    public void addChildListToCurrent(List<AddressManageDetailVO> currentList, List<AddressManageDetailVO> childList) {
        if (ObjectUtils.isEmpty(currentList) || ObjectUtils.isEmpty(childList)) {
            return;
        }

        for (AddressManageDetailVO current : currentList) {
            List<AddressManageDetailVO> addChildList = new ArrayList<>();
            for (AddressManageDetailVO child : childList) {
                if (current.getId().equals(child.getParentId())) {
                    child.setParentName(current.getAddressName());
                    addChildList.add(child);
                }
            }
            current.setChild(addChildList);
        }

    }

    @Override
    public ResultRO<List<NetWorkGroupObjectShowVO>> getDeviceAddressByName(String deviceUuid, String addressName) {
        SearchAddressDTO searchDTO = new SearchAddressDTO();
        searchDTO.setDeviceUuid(deviceUuid);
        searchDTO.setRangeOp(SearchRangeOpEnum.EQUAL.getCode());
        searchDTO.setSearchType(ObjectSearchTypeEnum.TEXT.getCode());
        String content = addressName.trim();
        //根据关键字搜索
        searchDTO.setQuickSearchText(content);
        PageDTO pageDTO = new PageDTO();
        return this.searchAddress(searchDTO, pageDTO);
    }

    @Override
    public List<NetWorkGroupObjectShowVO> getAllDetailAddressBySearchDTO(DeviceObjectSearchDTO searchDTO, String addressCategory) {
        if (StringUtils.isEmpty(searchDTO.getDeviceUuid())) {
            return null;
        }

        List<NetWorkGroupObjectShowVO> totalList = new ArrayList<>();
        if (StringUtils.isEmpty(addressCategory)) {
            ResultRO<List<NetWorkGroupObjectShowVO>> addressList = this.getAddressList(searchDTO, DeviceObjectTypeEnum.NETWORK_OBJECT);
            ResultRO<List<NetWorkGroupObjectShowVO>> addressGroupList = this.getAddressList(searchDTO, DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT);
            totalList.addAll(addressList.getData());
            totalList.addAll(addressGroupList.getData());
        } else {
            if (DeviceObjectTypeEnum.NETWORK_OBJECT.getCode().equals(addressCategory)) {
                ResultRO<List<NetWorkGroupObjectShowVO>> addressList = this.getAddressList(searchDTO, DeviceObjectTypeEnum.NETWORK_OBJECT);
                totalList.addAll(addressList.getData());
            } else if (DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT.getCode().equals(addressCategory)) {
                ResultRO<List<NetWorkGroupObjectShowVO>> addressGroupList = this.getAddressList(searchDTO, DeviceObjectTypeEnum.NETWORK_GROUP_OBJECT);
                totalList.addAll(addressGroupList.getData());
            }
        }

        if (CollectionUtils.isNotEmpty(totalList)) {
            totalList.forEach(s -> s.setIncludeAddress(s.getIncludeAddress().replace("()","")));
        }

        return totalList;
    }

    @Override
    public ReturnT<String> deleteByTaskId(Integer taskId) throws Exception {
        List<AddressManageDetailEntity> detailList = addressManageDetailMapper.getByTaskId(taskId);
        //删除详情
        if (ObjectUtils.isNotEmpty(detailList)) {
            for (AddressManageDetailEntity entity : detailList) {
                deleteByDetailId(entity.getId(), true);
            }
        }
        addressManageDetailMapper.deleteByTaskId(taskId);
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> deleteByDetailId(Integer id, boolean topDel) throws Exception {
        AddressManageDetailEntity detailEntity = addressManageDetailMapper.getById(id);

        if (ObjectUtils.isEmpty(detailEntity)) {
            throw new RuntimeException("对象详情为空");
        }

        if (!topDel && AddressLevelEnum.ADDRESS_PARENT.getCode().equals(detailEntity.getAddressLevel())) {
            throw new RuntimeException("顶级对象不允许删除");
        }

        //删除下发相关
        deletePushTaskByDetail(detailEntity);

        addressManageDetailMapper.delete(id);
        addressManageDetailMapper.deleteByParentId(id);
        addressDetailEntryMapper.deleteByDetailId(id);

        return ReturnT.SUCCESS;
    }

    @Override
    public List<AddressManageTaskEntity> findList(AddressManageTaskEntity addressManageTaskEntity) {
        return addressManageTaskMapper.findList(addressManageTaskEntity);
    }

    @Override
    public void deletePushTaskByDetail(AddressManageDetailEntity detailEntity) throws Exception {
        String pushId = detailEntity.getPushId();
        //删除下发相关
        if (StringUtils.isNotEmpty(pushId)) {
            String[] pushIds = pushId.split(PolicyConstants.ADDRESS_SEPERATOR);
            List<Integer> pushIdList = Arrays.stream(pushIds).map(Integer::valueOf).collect(Collectors.toList());
            for (Integer pid : pushIdList) {
                CommandTaskEditableEntity editableEntity = commandTaskEdiableMapper.selectByPrimaryKey(pid);
                if (editableEntity != null) {
                    commandTaskEdiableMapper.deleteByTaskId(editableEntity.getTaskId());
                    recommendTaskMapper.deleteByTaskId(editableEntity.getTaskId());
                }
            }
        }
    }

    @Override
    public Map<String, List<String>> getAddressMap(DeviceObjectSearchDTO searchDTO) {
        //查询设备下所有地址
        List<NetWorkGroupObjectShowVO> allAddressList = getAllDetailAddressBySearchDTO(searchDTO, null);

        //所有地址的map
        Map<String, List<String>> allAddrMap = new HashMap<>();
        allAddressList.forEach(e -> {
            if (StringUtils.isNotEmpty(e.getIncludeAddress())) {
                String[] includeList = e.getIncludeAddress().split(";");
                List<String> childList = Arrays.stream(includeList).collect(Collectors.toList());
                allAddrMap.put(e.getName(), childList);
            }
        });
        return allAddrMap;
    }

    /**
     * 搜索地址结果对象转换
     * @param deviceFilterlistResultRO
     * @return
     */
    private ResultRO<List<NetWorkGroupObjectShowVO>> formatNetWorkGroupRO(ResultRO<List<NetWorkGroupObjectRO>> deviceFilterlistResultRO) {
        if (deviceFilterlistResultRO == null || !deviceFilterlistResultRO.getSuccess()) {
            return new ResultRO<>(false);
        }
        ResultRO<List<NetWorkGroupObjectShowVO>> resultRO = new ResultRO<>(true);
        List<NetWorkGroupObjectShowVO> voList = NetWorkGroupObjectShowVO.formatByList(deviceFilterlistResultRO.getData());

        for (NetWorkGroupObjectShowVO showVO : voList) {
            for (NetWorkGroupObjectRO ro : deviceFilterlistResultRO.getData()) {
                if (showVO.getName().equals(ro.getName())) {
                    //添加地址类型
                    showVO.setTypeDesc(ro.getDeviceObjectType());
                    showVO.setIncludeAddress(showVO.getIncludeAddress().replace("()",""));
                }
            }
        }
        resultRO.setTotal(deviceFilterlistResultRO.getTotal());
        resultRO.setData(voList);
        return resultRO;
    }
}
