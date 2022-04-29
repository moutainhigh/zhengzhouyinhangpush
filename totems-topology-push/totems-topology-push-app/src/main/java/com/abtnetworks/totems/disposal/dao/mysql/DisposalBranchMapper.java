package com.abtnetworks.totems.disposal.dao.mysql;

import com.abtnetworks.totems.disposal.entity.DisposalBranchEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author hw
 * @Description
 * @Date 16:55 2019/11/11
 */
@Mapper
@Repository
public interface DisposalBranchMapper {

    /**
     * 新增
     */
    public int insert(DisposalBranchEntity disposalBranch);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(DisposalBranchEntity disposalBranch);

    /**
     * get查询 By Id
     */
    public DisposalBranchEntity getById(@Param("id") int id);

    /**
     * get By name and ip
     * @param name
     * @param ip
     * @return
     */
    DisposalBranchEntity getByNameAndIp(@Param("name") String name, @Param("ip") String ip);

    /**
     * get查询
     */
    public DisposalBranchEntity get(DisposalBranchEntity disposalBranch);

    /**
     * 查询 List Data
     */
    public List<DisposalBranchEntity> findList(DisposalBranchEntity disposalBranch);

    /**
     * 查询 List Data By uuids UUID的集合
     * @param uuids
     * @return
     */
    public List<DisposalBranchEntity> findByUUIDs(@Param("uuids") String[] uuids);

    /**
     * 查询Count
     */
    public int count();

}

