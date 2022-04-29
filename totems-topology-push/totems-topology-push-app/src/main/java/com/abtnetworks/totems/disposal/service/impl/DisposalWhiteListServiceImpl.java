package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dao.mysql.DisposalWhiteListMapper;
import com.abtnetworks.totems.disposal.dto.DisposalWhiteSaveDTO;
import com.abtnetworks.totems.disposal.entity.DisposalWhiteListEntity;
import com.abtnetworks.totems.disposal.enums.DisposalCategoryEnum;
import com.abtnetworks.totems.disposal.service.DisposalWhiteListService;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.abtnetworks.totems.disposal.BaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 16:25 2019/11/11
 */
@Service
public class DisposalWhiteListServiceImpl extends BaseService implements DisposalWhiteListService {

    @Resource
    private DisposalWhiteListMapper disposalWhiteListDao;

    @Autowired
    private LogClientSimple logClientSimple;

    /**
     * 新增
     */
    @Override
    public ResultRO insert(DisposalWhiteSaveDTO dto) {
        ResultRO resultRO = checkEntity(dto, true);
        if(!resultRO.getSuccess()){
            return resultRO;
        }

        DisposalWhiteListEntity entity = dtoConvertEntity(dto);

        int count = disposalWhiteListDao.insert(entity);
        resultRO.setSuccess(count > 0 ? true : false);
        String message = String.format("添加白名单%s", count > 0 ? dto.getName() + "成功" : dto.getName() + "失败");
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), message);
        return resultRO;
    }

    @Override
    public ResultRO delete(String modifiedUser, String ids) {
        if (StringUtils.isBlank(ids)) {
            return new ResultRO(false, "参数不能为空");
        }

        String[] arr = ids.split(",");
        for (String idStr : arr) {
            Long id = Long.valueOf(idStr);
            DisposalWhiteListEntity entity = disposalWhiteListDao.getById(id);
            if(entity == null){
                logger.warn("根据id:{}查询白名单为空", id);
                continue;
            }

            entity.setModifiedUser(modifiedUser);
            entity.setModifiedTime(DateUtil.getCurrentTimestamp());
            entity.setDeleted(true);
            disposalWhiteListDao.update(entity);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), String.format("删除白名单%s成功", entity.getName()));
        }

        return new ResultRO(true);
    }

    /**
     * 更新
     */
    @Override
    public ResultRO update(DisposalWhiteSaveDTO dto) {
        //参数校验
        ResultRO resultRO = checkEntity(dto, false);
        if(!resultRO.getSuccess()){
            return resultRO;
        }

        if(dto.getType().intValue() == DisposalCategoryEnum.POLICY.getCode().intValue()) {
            if (AliStringUtils.isEmpty(dto.getSrcIp())) {
                if (dto.getIpv6()) {
                    dto.setSrcIp(PolicyConstants.IPV6_ANY);
                } else {
                    dto.setSrcIp(PolicyConstants.IPV4_ANY);
                }
            }
            if (AliStringUtils.isEmpty(dto.getDstIp())) {
                if (dto.getIpv6()) {
                    dto.setDstIp(PolicyConstants.IPV6_ANY);
                } else {
                    dto.setDstIp(PolicyConstants.IPV4_ANY);
                }
            }
        }

        DisposalWhiteListEntity entity = new DisposalWhiteListEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setServiceList(dto.getServiceList() == null ? null : ServiceDTOUtils.toString(dto.getServiceList()));

        //修改字段
        DisposalWhiteListEntity po = disposalWhiteListDao.getById(entity.getId());
        po.setName(entity.getName());
        po.setSrcIp(entity.getSrcIp());
        po.setDstIp(entity.getDstIp());
        po.setServiceList(entity.getServiceList());
        po.setRoutingIp(entity.getRoutingIp());
        po.setRemarks(entity.getRemarks());
        po.setModifiedUser(entity.getModifiedUser());
        po.setModifiedTime(DateUtil.getCurrentTimestamp());
        int ret = disposalWhiteListDao.update(po);
        resultRO.setSuccess(ret > 0 ? true : false);
        String message = String.format("修改白名单%s", ret > 0 ? dto.getName() + "成功" : dto.getName() + "失败");

        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), message);
        return resultRO;
    }

    /**
     * 查询 get By Id
     */
    @Override
    public ResultRO<DisposalWhiteListEntity> getById(Long id) {
        ResultRO<DisposalWhiteListEntity> resultRO = new ResultRO<>(true);
        DisposalWhiteListEntity entity = disposalWhiteListDao.getById(id);
        resultRO.setData(entity);
        return resultRO;
    }

    /**
     * 查询 get
     */
    @Override
    public List<DisposalWhiteListEntity> get(DisposalWhiteListEntity disposalWhiteList) {
        return disposalWhiteListDao.get(disposalWhiteList);
    }


    /**
     * 分页查询
     */
    @Override
    public ResultRO<List<DisposalWhiteListEntity>> findList(Integer type, String name, String content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<DisposalWhiteListEntity> list = disposalWhiteListDao.findList(type, name, content);
        if(list != null && !list.isEmpty()){
            for(DisposalWhiteListEntity po : list){
                po.setCreateTimeDesc(DateUtil.dateToString(po.getCreateTime(), DateUtil.timeStamp_STANDARD));

                int ipv4CheckOK = 0;
                //路由时，将黑洞路由IP 放在目的地址中
                if(po.getType().equals(DisposalCategoryEnum.ROUT.getCode())){
                    po.setDstIp(po.getRoutingIp());
                    ipv4CheckOK = InputValueUtils.checkIp(po.getRoutingIp());
                }else if(po.getType().equals(DisposalCategoryEnum.POLICY.getCode())){
                    ipv4CheckOK = InputValueUtils.checkIp(po.getSrcIp());
                }

                //判断IP类型
                if(ipv4CheckOK == 0){
                    po.setIpv6(false);
                }else{
                    po.setIpv6(true);
                }
            }
        }
        PageInfo<DisposalWhiteListEntity> pageInfo = new PageInfo<>(list);
        ResultRO<List<DisposalWhiteListEntity>> resultRO = new ResultRO(true, Integer.valueOf(pageInfo.getTotal()+""));
        resultRO.setData(pageInfo.getList());
        return resultRO;
    }


    @Override
    public ResultRO<List<DisposalWhiteListEntity>> findAll() {
        ResultRO<List<DisposalWhiteListEntity>> resultRO = new ResultRO<>(true);
        List<DisposalWhiteListEntity> list = disposalWhiteListDao.findList(null, null, null);
        resultRO.setData(list);
        return resultRO;
    }


    private ResultRO checkEntity(DisposalWhiteSaveDTO entity, boolean isAddFlag){
        //参数校验
        if (entity == null) {
            return new ResultRO(false, "必要参数缺失");
        }

        if(!isAddFlag){
            if(entity.getId() == null){
                return new ResultRO(false, "参数ID不能为空");
            }
        }

        if(StringUtils.isBlank(entity.getName())){
            return new ResultRO(false, "白名单名称不能为空");
        }

        if(isAddFlag){
            DisposalWhiteListEntity po = disposalWhiteListDao.getByName(entity.getName());
            if(po != null){
                return new ResultRO(false, "白名单名称已存在，不可重复填写");
            }
        }else{
            DisposalWhiteListEntity po = disposalWhiteListDao.getByNameNotId(entity.getName(), entity.getId());
            if(po != null){
                return new ResultRO(false, "白名单名称已存在，不可重复填写");
            }
        }

        String ipType = null;
        if (entity.getIpv6()) {
            ipType = ImportExcelVerUtils.IP_TYPE_IPV6;
        } else {
            ipType = ImportExcelVerUtils.IP_TYPE_IPV4;
        }

        //路由白名单，校验IP
        if(entity.getType().intValue() == DisposalCategoryEnum.ROUT.getCode().intValue()){
            if(StringUtils.isBlank(entity.getRoutingIp())){
                logger.error("路由白名单，必须指定路由IP地址");
                return new ResultRO(false, "黑洞路由IP不能为空");
            }

            ResultRO<String> resultRO = ImportExcelVerUtils.checkIpByIpv46(entity.getRoutingIp(), "黑洞路由IP", ImportExcelVerUtils.IP_TYPE_IPV46);
            if(resultRO.getSuccess()){
                entity.setRoutingIp(resultRO.getData());
            }else{
                return resultRO;
            }

        }else if(entity.getType().intValue() == DisposalCategoryEnum.POLICY.getCode().intValue()){
            //策略白名单，校验五元组
            if (StringUtils.isNotBlank(entity.getSrcIp())) {
                ResultRO<String> resultRO = ImportExcelVerUtils.checkIpByIpv46(entity.getSrcIp(), "源地址", ipType);
                if (resultRO.getSuccess()) {
                    entity.setSrcIp(resultRO.getData());
                } else {
                    return resultRO;
                }
            }

            if (StringUtils.isNotBlank(entity.getDstIp())) {
                ResultRO<String> resultRO = ImportExcelVerUtils.checkIpByIpv46(entity.getDstIp(), "目的地址", ipType);
                if (resultRO.getSuccess()) {
                    entity.setDstIp(resultRO.getData());
                } else {
                    return resultRO;
                }

            }

            if (entity.getServiceList() != null && !entity.getServiceList().isEmpty()) {
                for (ServiceDTO service : entity.getServiceList()) {
                    if (StringUtils.isNotBlank(service.getDstPorts())) {
                        //仅校验端口即可
                        service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                    }
                }
            }

        }else{
            return new ResultRO(false, "参数type非法");
        }

        return new ResultRO(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultRO batchInsert(List<DisposalWhiteSaveDTO> list) throws Exception {
        if (list == null || list.isEmpty()) {
            return new ResultRO(false, "参数为空");
        }

        StringBuilder nameSb = new StringBuilder();
        for (DisposalWhiteSaveDTO dto : list) {
            DisposalWhiteListEntity entity = dtoConvertEntity(dto);
            disposalWhiteListDao.insert(entity);
            nameSb.append("," + dto.getName());
        }

        if (nameSb.length() > 0) {
            nameSb = nameSb.deleteCharAt(0);
        }

        String message = String.format("批量导入白名单%s成功", nameSb.toString());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), message);

        return new ResultRO(true);
    }

    private DisposalWhiteListEntity dtoConvertEntity(DisposalWhiteSaveDTO dto){
        if (dto == null) {
            return new DisposalWhiteListEntity();
        }
        // 源ip 目的ip 判断ipv6 ipv4 赋值any
        if(dto.getType().intValue() == DisposalCategoryEnum.POLICY.getCode().intValue()) {
            if (AliStringUtils.isEmpty(dto.getSrcIp())) {
                if (dto.getIpv6()) {
                    dto.setSrcIp(PolicyConstants.IPV6_ANY);
                } else {
                    dto.setSrcIp(PolicyConstants.IPV4_ANY);
                }
            }
            if (AliStringUtils.isEmpty(dto.getDstIp())) {
                if (dto.getIpv6()) {
                    dto.setDstIp(PolicyConstants.IPV6_ANY);
                } else {
                    dto.setDstIp(PolicyConstants.IPV4_ANY);
                }
            }
        }

        DisposalWhiteListEntity entity = new DisposalWhiteListEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setServiceList(dto.getServiceList() == null ? null : ServiceDTOUtils.toString(dto.getServiceList()));

        entity.setUuid(UUIDUtil.getUuid());
        entity.setDeleted(false);
        entity.setCreateTime(DateUtil.getCurrentTimestamp());
        return entity;
    }
}
