package com.abtnetworks.totems.disposal.service;

import java.util.List;
import java.util.Map;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.entity.DisposalBranchEntity;
import com.github.pagehelper.PageInfo;

/**
 * @Author hw
 * @Description
 * @Date 16:54 2019/11/11
 */
public interface DisposalBranchService {

    /**
     * 新增
     */
    public ReturnT<String> insert(DisposalBranchEntity disposalBranch);

    /**
     * 删除
     */
    public ReturnT<String> delete(int id);

    /**
     * 更新
     */
    public ReturnT<String> update(DisposalBranchEntity disposalBranch);

    /**
     * 查询 get By Id
     */
    public DisposalBranchEntity getById(int id);

     DisposalBranchEntity getByNameAndIp(String name, String ip);

    /**
     * 查询 get By Id
     */
    public DisposalBranchEntity get(DisposalBranchEntity disposalBranch);

    /**
     * 分页查询
     */
    public PageInfo<DisposalBranchEntity> findList(DisposalBranchEntity disposalBranch, int pageNum, int pageSize);

    /**
     * 查询 List Data By uuids UUID的集合
     * @param uuids
     * @return
     */
    public List<DisposalBranchEntity> findByUUIDs(String[] uuids);

}

