package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.dto.AddressUpdateScenesDTO;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.entity.AddressManageTaskEntity;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.auto.vo.AddressManageTaskVO;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
public interface AddressManageTaskService {

    /**
     * 界面新增方法
     * @param addressManageTaskVO 地址任务vo对象
     */
    ReturnT<String> addTask(AddressManageTaskVO addressManageTaskVO);

    /**
     * 界面新增方法-多个
     * @param addressManageTaskVO 地址任务vo对象
     */
    ReturnT<String> addAllTask(AddressManageTaskVO addressManageTaskVO);



    ReturnT<String> update(AddressManageTaskVO addressManageTaskVO) throws Exception;


    /**
     * 详情界面添加地址组或地址对象
     * @param vo 地址任务vo对象
     */
    ReturnT<String> addAddress(AddressManageDetailVO vo);

    /**
     * 删除
     * @param ids 主键，多个用,分隔开
     */
    ReturnT<String> delete(String ids) throws Exception;

    /**
     * 更新
     * @param addressManageTaskEntity 地址任务对象
     */
    ReturnT<String> update(AddressManageTaskEntity addressManageTaskEntity);

    /**
     * 查询 get By Id
     * @param id 主键
     */
    AddressManageTaskEntity getById(int id);

    /**
     * 查询 get By Id
     * @param uuid 任务对象uuid
     */
    AddressManageTaskEntity getByUUID(String uuid);

    /**
     * 分页查询
     * @param addressManageTaskEntity 地址任务实体对象
     * @param pageNum 页数
     * @param pageSize 分页大小
     */
    PageInfo<AddressManageTaskEntity> findList(AddressManageTaskEntity addressManageTaskEntity, int pageNum, int pageSize);

    /**
     * 查询对象管理详情界面
     * @param addressManageDetailEntity 地址详情实体对象
     * @return
     */
    List<AddressManageDetailVO> findDetailList(AddressManageDetailEntity addressManageDetailEntity);

    /**分页：获取地址、地址组列表**/
    PageInfo<NetWorkGroupObjectShowVO> getAddressListByDevice(AddressManageTaskVO vo, int pageNum, int pageSize);

    /**
     * 更换场景
     * @param requestDTO 主键
     */
    ReturnT<String> changeScenes(AddressUpdateScenesDTO requestDTO) throws Exception;

    /**
     * 更换场景
     * @param searchDTO 主键
     */
    ResultRO<List<NetWorkGroupObjectShowVO>> getDeviceAddressList(DeviceObjectSearchDTO searchDTO, DeviceObjectTypeEnum objectTypeEnum);
}
