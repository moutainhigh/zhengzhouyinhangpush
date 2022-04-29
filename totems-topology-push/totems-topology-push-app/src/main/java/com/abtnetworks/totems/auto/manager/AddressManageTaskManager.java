package com.abtnetworks.totems.auto.manager;

import com.abtnetworks.totems.auto.dto.AddressManageDetailDTO;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.entity.AddressManageTaskEntity;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.common.dto.PageDTO;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchAddressDTO;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;

import java.util.List;
import java.util.Map;

/**
 * @Description 对象管理任务
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 11:45:15'.
 */
public interface AddressManageTaskManager {

    /**搜索地址**/
    ResultRO<List<NetWorkGroupObjectShowVO>> searchAddress(SearchAddressDTO searchAddressDTO, PageDTO pageDTO);

    /**
     * 通过任务id查找详情数据
     * @param taskId
     * @return
     */
    List<AddressManageDetailEntity> getDetailByTaskId(Integer taskId);

    /**分页：获取地址、地址组列表**/
    ResultRO<List<NetWorkGroupObjectShowVO>> getAddressList(DeviceObjectSearchDTO searchDTO, DeviceObjectTypeEnum objectTypeEnum);

    /**
     * 通过新增地址值添加当前对象的子级对象
     * @param vo
     * @param addressAdd
     */
    void addDetailEntityByAddChild(AddressManageDetailDTO vo, String addressAdd);

    /**
     * 通过任务id获取父级初始化结构
     * @param taskId
     * @return
     */
    List<AddressManageDetailVO> getParentDetailListByTaskId(Integer taskId);

    /**
     * 将子对象加到父级对象
     * @param currentList
     * @param childList
     */
    void addChildListToCurrent(List<AddressManageDetailVO> currentList, List<AddressManageDetailVO> childList);

    /**
     * 通过地址组名称获取设备上相关的地址组对象
     * @param deviceUuid
     * @param addressName
     * @return
     */
    ResultRO<List<NetWorkGroupObjectShowVO>> getDeviceAddressByName(String deviceUuid, String addressName);

    /**
     * 获取设备所有地址及地址组信息
     * @param searchDTO
     * @return
     */
    List<NetWorkGroupObjectShowVO> getAllDetailAddressBySearchDTO(DeviceObjectSearchDTO searchDTO, String addressCategory);

    /**
     * 通过任务Id删除对象
     * @param taskId
     * @return
     */
    ReturnT<String> deleteByTaskId(Integer taskId) throws Exception;

    /**
     * 删除
     * @param id
     * @param topDel 顶级对象是否可删除,true可删，false不可删
     */
    ReturnT<String> deleteByDetailId(Integer id, boolean topDel) throws Exception;

    /**
     * 查询
     * @param addressManageTaskEntity 地址任务实体对象
     */
    List<AddressManageTaskEntity> findList(AddressManageTaskEntity addressManageTaskEntity);

    /**
     * 删除下发相关任务
     */
    void deletePushTaskByDetail(AddressManageDetailEntity detailEntity) throws Exception;

    /**
     * 获取地址名称和地址内容映射map
     * @param searchDTO
     * @return
     */
    Map<String, List<String>> getAddressMap(DeviceObjectSearchDTO searchDTO);

}
