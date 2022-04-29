package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.entity.DisposalTeamBranchEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 10:03 2019/11/12
 */
@Mapper
@Repository
public interface DisposalTeamBranchMapper {

    /**
     * 新增
     */
    public int insert(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 批量插入，封堵协作单位关系表
     * @param list
     * @param centerUuid
     * @return
     */
    public int bulkInsert(@Param("list") List<DisposalTeamBranchEntity> list, @Param("centerUuid") String centerUuid);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 更新 派发下级单位处置单状态
     */
    public int updateHandleStatus(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 更新 派发下级单位回滚处置单状态
     */
    public int updateCallbackHandleStatus(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * get查询 By Id
     */
    public DisposalTeamBranchEntity getById(@Param("id") int id);

    /**
     * get查询
     */
    public List<DisposalTeamBranchEntity> findByCenterUuid(@Param("centerUuid") String centerUuid);

    /**
     * 查询 List Data
     */
    public List<DisposalTeamBranchEntity> findList(DisposalTeamBranchEntity disposalTeamBranch);

    /**
     * 查询Count
     */
    public int count();

}

