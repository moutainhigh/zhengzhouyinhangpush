package com.abtnetworks.totems.translation.service;

import java.util.List;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.vo.TranslationTaskRecordVO;
import com.github.pagehelper.PageInfo;

/**
 * @Description 策略迁移信息表
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
public interface TranslationTaskRecordService {


    /**
     * 开始命令行翻译
     * @param taskRecord
     * @return
     */
    public ReturnT startTranslation(TranslationTaskRecordEntity taskRecord) throws Exception;

    /**
     * 新增
     */
    public ReturnT<String> insert(TranslationTaskRecordEntity entity) throws Exception;

    /**
     * 删除
     */
    public ReturnT<String> delete(int id) throws Exception;

    /**
     * 更新
     */
    public ReturnT<String> update(TranslationTaskRecordEntity entity) throws Exception;

    /**
     * 查询 get By Id
     */
    public TranslationTaskRecordEntity getById(int id) throws Exception;

    /**
     * 查询 get By Id
     */
    public TranslationTaskRecordEntity getByUUID(String uuid) throws Exception;

    /**
     * 查询 命令行配置文件生成
     */
    public String getCommandLineConfigByUUID(String uuid) throws Exception;

    /**
     * 分页查询
     */
    public PageInfo<TranslationTaskRecordVO> findList(TranslationTaskRecordEntity entity, int pageNum, int pageSize) throws Exception;

    /**
     * 取消转换
     */
    public ReturnT<String> cancelTranslation(int id);

    /**
     * 获取策略转换进度条
     * @param id
     * @return
     */
    ReturnT getTranslationTaskProgress(Integer id);

    ReturnT<String> batchDelete(String ids);
}
