package com.abtnetworks.totems.auto.controller;


import com.abtnetworks.totems.auto.dto.AddressManageDetailDTO;
import com.abtnetworks.totems.auto.dto.AddressUpdateScenesDTO;
import com.abtnetworks.totems.auto.entity.AddressDetailEntryEntity;
import com.abtnetworks.totems.auto.entity.AddressManageDetailEntity;
import com.abtnetworks.totems.auto.entity.AddressManageTaskEntity;
import com.abtnetworks.totems.auto.service.AddressManageDetailService;
import com.abtnetworks.totems.auto.service.AddressManageTaskService;
import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.auto.vo.AddressManagePushVO;
import com.abtnetworks.totems.auto.vo.AddressManageTaskVO;
import com.abtnetworks.totems.common.enums.DeviceObjectTypeEnum;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.policyoptimize.vo.NetWorkGroupObjectShowVO;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description 对象管理
 * @Version --
 * @Created by zhoumuhua on '2021-07-27'.
 */
@Api(tags = "对象管理")
@RestController
@RequestMapping(value = "/address/manage/")
@Slf4j
public class PushAddressManageController {

    @Autowired
    private AddressManageTaskService addressManageTaskService;

    @Autowired
    private AddressManageDetailService addressManageDetailService;

    @ApiOperation("新增")
    @PostMapping("insert")
    public ReturnT<String> insert(@RequestBody AddressManageTaskVO vo, Authentication auth) {

        try {
            if (StringUtils.isEmpty(vo.getAddressName())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "地址组名称缺失");
            }
            vo.setUserName(auth.getName());
            return addressManageTaskService.addAllTask(vo);
        } catch (Exception  e ) {
            log.error("新建对象管理任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"新建对象管理任务异常");
        }
    }


    @ApiOperation("编辑")
    @PostMapping("update")
    public ReturnT<String> update(@RequestBody AddressManageTaskVO vo, Authentication auth) throws Exception{
        try {
            if (TotemsStringUtils.isBlank(vo.getScenesUuid())){
                return new ReturnT<>(ReturnT.FAIL_CODE, "场景id缺失");
            }
           return addressManageTaskService.update(vo);

        } catch (Exception  e ) {
            log.error("生成命令行异常：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"生成命令行异常");
        }
    }

    @ApiOperation("详情内新建地址对象或地址组")
    @PostMapping("insertAddressObject")
    public ReturnT<String> insertAddressObject(@RequestBody AddressManageDetailVO vo, Authentication auth) {

        try {
            if (StringUtils.isEmpty(vo.getAddressName())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "地址组名称缺失");
            }
            return addressManageTaskService.addAddress(vo);
        } catch (Exception  e ) {
            log.error("新建对象管理任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"新建对象管理任务异常");
        }
    }

    @ApiOperation("删除")
    @PostMapping("delete")
    public ReturnT<String> delete(String ids) {
        try {
            if (StringUtils.isEmpty(ids)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageTaskService.delete(ids);
        } catch (Exception e) {
            log.error("删除对象管理任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"删除对象管理任务异常");
        }
    }

    @ApiOperation("通过场景uuid查找设备")
    @PostMapping("getDeviceByScenes")
    public ReturnT<Map<String, Map<String, Map<String, Object>>>> getDeviceByScenes(@RequestBody AddressManageDetailVO vo) {
        try {
            if (null == vo || null == vo.getScenesUuid()) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageDetailService.getDeviceByScenes(vo);
        } catch (Exception e) {
            log.error("通过场景uuid查找设备，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"通过场景uuid查找设备异常");
        }
    }

    @ApiOperation("查询任务详情By Id")
    @PostMapping("getById")
    public AddressManageTaskEntity getById(int id) {
        return addressManageTaskService.getById(id);
    }

    @ApiOperation("分页查询")
    @PostMapping("pageList")
    public ReturnT pageList(@RequestBody AddressManageTaskVO vo) {
        try {
            if (null == vo) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            AddressManageTaskEntity addressManageTaskEntity = new AddressManageTaskEntity();
            BeanUtils.copyProperties(vo, addressManageTaskEntity);
            PageInfo<AddressManageTaskEntity> pageInfo = addressManageTaskService.findList(addressManageTaskEntity, vo.getPage(), vo.getLimit());

            return new ReturnT(pageInfo);
        } catch (Exception e) {
            log.error("分页查询对象管理列表异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"新建对象管理任务异常");
        }

    }

    @ApiOperation("获取对象管理详情界面数据")
    @PostMapping("getDetailList")
    public ReturnT<List<AddressManageDetailVO>> detailList(@RequestBody AddressManageDetailVO addressManageDetailVO) {
        try {
            if (null == addressManageDetailVO || null == addressManageDetailVO.getTaskId()) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            AddressManageDetailEntity addressManageDetailEntity = new AddressManageDetailEntity();
            BeanUtils.copyProperties(addressManageDetailVO, addressManageDetailEntity);
            List<AddressManageDetailVO> detailVOList = addressManageTaskService.findDetailList(addressManageDetailEntity);
            return new ReturnT(detailVOList);
        } catch (Exception e) {
            log.error("获取详情数据异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"获取详情数据异常");
        }

    }

    @ApiOperation("获取地址详情byId")
    @PostMapping("getDetailById")
    public ReturnT<AddressManageDetailVO> getDetailById(@RequestBody AddressManageDetailVO vo) {
        try {
            if (null == vo || null == vo.getId()) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            AddressManageDetailVO detail = addressManageDetailService.getDetailById(vo.getId());
            return new ReturnT(detail);
        } catch (Exception e) {
            log.error("获取地址详情异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"获取地址详情异常");
        }
    }

    @ApiOperation("保存详情修改信息")
    @PostMapping("saveDetail")
    public ReturnT<String> saveDetail(@RequestBody AddressManageDetailVO vo) {
        try {
            if (null == vo || null == vo.getId()) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageDetailService.saveDetail(vo);
        } catch (Exception e) {
            log.error("保存详情异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"保存详情异常");
        }
    }

    @ApiOperation("命令行生成")
    @PostMapping("checkAddress")
    public ReturnT<String> checkAddress(@RequestBody AddressManageDetailVO vo) {
        try {
            if (null == vo || null == vo.getIdList()) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageDetailService.checkAddress(vo);
        } catch (Exception e) {
            log.error("命令行生成异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"命令行生成异常");
        }
    }

    @ApiOperation("获取设备下地址及地址组对象")
    @PostMapping("getDeviceAddr")
    public ReturnT<PageInfo<NetWorkGroupObjectShowVO>> getDeviceAddr(@RequestBody AddressManageTaskVO vo) {
        try {
            if (null == vo) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            AddressManageTaskEntity addressManageTaskEntity = new AddressManageTaskEntity();
            BeanUtils.copyProperties(vo, addressManageTaskEntity);
            PageInfo<NetWorkGroupObjectShowVO> detailVOList = addressManageTaskService.getAddressListByDevice(vo, vo.getPage(), vo.getLimit());
            return new ReturnT(detailVOList);
        } catch (Exception e) {
            log.error("获取设备下地址及地址组对象异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"获取设备下地址及地址组对象异常");
        }

    }

    @ApiOperation("通过场景获取当前地址组下发命令")
    @PostMapping("getPushCmdByScenes")
    public ReturnT<List<AddressManagePushVO>> getPushCmdByScenes(@RequestBody AddressManageDetailVO vo) {
        try {
            if (null == vo || null == vo.getId() || StringUtils.isEmpty(vo.getScenesUuid())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            List<AddressManagePushVO> pushCmdByScenes = addressManageDetailService.getPushCmdByScenes(vo);
            return new ReturnT(pushCmdByScenes);
        } catch (Exception e) {
            log.error("通过场景获取当前地址组下发命令，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"通过场景获取当前地址组下发命令异常");
        }

    }

    @ApiOperation("对象管理命令行下发")
    @PostMapping("pushcmd")
    public ReturnT<String> pushCmd(@RequestBody AddressManagePushVO vo) {
        try {
            if (null == vo || ObjectUtils.isEmpty(vo.getIdList())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageDetailService.pushCmd(vo);
        } catch (Exception e) {
            log.error("对象管理命令行下发，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,"对象管理命令行下发异常");
        }

    }

    @ApiOperation("根据地址详情id删除地址下内容")
    @PostMapping("deleteByDetail")
    public ReturnT<String> deleteByDetail(String ids) {
        try {
            if (StringUtils.isEmpty(ids)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageDetailService.deleteByIds(ids);
        } catch (Exception e) {
            log.error("删除对象管理任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,e.getMessage());
        }
    }

    @ApiOperation("根据具体条目id删除当前地址内容")
    @PostMapping("deleteSelectOne")
    public ReturnT<String> deleteSelectOne(@RequestBody AddressManageDetailVO vo) {
        try {
            if (StringUtils.isEmpty(vo.getAddressCategory()) || null == vo.getId()) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageDetailService.deleteSelectOne(vo);
        } catch (Exception e) {
            log.error("删除对象管理任务异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE,e.getMessage());
        }
    }

    @ApiOperation("根据地址对象名称模糊查询地址对象")
    @GetMapping("findAddressByName")
    public ReturnT<List<AddressManageDetailDTO>> findAddressByName(String name) {
        try {
            List<AddressManageDetailDTO> list = addressManageDetailService.findAddressByName(name);
            return new ReturnT(list);
        } catch (Exception e) {
            return new ReturnT(ReturnT.FAIL_CODE,"根据地址对象名称模糊查询地址对象异常");
        }
    }

    @ApiOperation("根据地对象名称查询对象条目")
    @GetMapping("findAddressEntityByName")
    public ReturnT<List<AddressDetailEntryEntity>> findAddressEntityByName(String name) {
        try {
            return addressManageDetailService.findAddressEntityByName(name);
        } catch (Exception e) {
            return new ReturnT(ReturnT.FAIL_CODE,"根据地对象名称查询对象条目异常");
        }
    }

    @ApiOperation("地址管理更换场景")
    @PostMapping("changeScenes")
    public ReturnT<String> changeScenes(@RequestBody AddressUpdateScenesDTO requestDTO) {
        try {
            if (requestDTO == null || CollectionUtils.isEmpty(requestDTO.getIdList())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            return addressManageTaskService.changeScenes(requestDTO);
        } catch (Exception e) {
            return new ReturnT(ReturnT.FAIL_CODE,"地址管理更换场景异常");
        }
    }

    @ApiOperation(value = "获取地址、地址组对象,只获取普通地址对象 ", httpMethod = "POST")
    @RequestMapping(value = "device/network-object-list", method = RequestMethod.POST)
    public ResultRO<List<NetWorkGroupObjectShowVO>> getNetWorkObjectList(@ApiParam(name = "deviceUuid", value = "设备uuid", required = false) @RequestParam(required = false) String deviceUuid,
                                                                         @ApiParam(name = "pageSize", value = "每页显示的记录条数", required = true, defaultValue = "20") @RequestParam(required = true) Integer pageSize,
                                                                         @ApiParam(name = "currentPage", value = "当前页", required = true, defaultValue = "1") @RequestParam(required = true) Integer currentPage,
                                                                         @ApiParam(name = "objectType", value = "类型：NETWORK_OBJECT地址、NETWORK_GROUP_OBJECT地址组", required = true, defaultValue = "NETWORK_OBJECT") @RequestParam(required = true) String objectType
    ) {
        DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();
        searchDTO.setDeviceUuid(deviceUuid);
        searchDTO.setPage(currentPage);
        searchDTO.setPsize(pageSize);
        return addressManageTaskService.getDeviceAddressList(searchDTO, DeviceObjectTypeEnum.getDeviceObjectTypeByCode(objectType));
    }

}
