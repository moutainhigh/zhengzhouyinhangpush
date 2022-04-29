package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalWhiteSaveDTO;
import com.abtnetworks.totems.disposal.entity.DisposalWhiteListEntity;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 16:23 2019/11/11
 */
 public interface DisposalWhiteListService {

    /**
     * 新增
     */
    ResultRO insert(DisposalWhiteSaveDTO dto);

    /**
     * 删除
     */
    ResultRO delete(String modifiedUser, String ids);

    /**
     * 更新
     */
    ResultRO update(DisposalWhiteSaveDTO dto);

    /**
     * 查询 get By Id
     */
    ResultRO<DisposalWhiteListEntity> getById(Long id);

    /**
     * 查询 get By Id
     */
    List<DisposalWhiteListEntity> get(DisposalWhiteListEntity disposalWhiteList);

    /**
     * 分页查询
     */
    ResultRO<List<DisposalWhiteListEntity>> findList(Integer type, String name, String content, Integer pageNum, Integer pageSize);


    ResultRO<List<DisposalWhiteListEntity>> findAll();

    /**批量插入**/
    ResultRO batchInsert(List<DisposalWhiteSaveDTO> list) throws Exception;

}
