package com.abtnetworks.totems.disposal.service;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalHandleListDTO;
import com.abtnetworks.totems.disposal.entity.DisposalHandleEntity;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:22 2019/11/12
 */
public interface DisposalHandleService {

    /**
     * 新增
     */
     ReturnT<String> insert(String jsonStr);

    /**
     * 删除
     */
     ReturnT<String> delete(int id);

    /**
     * 更新
     */
     ReturnT<String> update(DisposalHandleEntity disposalHandle);

    /**
     * 查询 get By Id
     */
     DisposalHandleEntity getById(Long id);

    /**
     * 查询 get By Id
     */
     DisposalHandleEntity get(DisposalHandleEntity disposalHandle);

    /**
     * 分页查询
     */
     ResultRO<List<DisposalHandleListDTO>> findList(Integer category, Integer type,
                                                    Integer status, String content,
                                                    Boolean callbackFlag,
                                                    int pageNum, int pageSize);

     /**生成处置单**/
     ResultRO createHandle(String auditUser, Long id);

    /**工单关联场景
     * @param auditUser  操作用户
     * @param id  协作单ID
     * @param uuidList 场景uuid集合
     * **/
    ResultRO joinScenes(String auditUser, Long id, List<String> uuidList);

}
