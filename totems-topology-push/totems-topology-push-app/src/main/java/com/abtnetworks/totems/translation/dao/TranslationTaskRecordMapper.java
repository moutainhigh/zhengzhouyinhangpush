package com.abtnetworks.totems.translation.dao;

import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.vo.TranslationTaskRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 策略迁移信息表
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
@Mapper
@Repository
public interface TranslationTaskRecordMapper {

    /**
     * 新增
     */
    public int insert(TranslationTaskRecordEntity entity);

    /**
     * 删除
     */
    public int delete(@Param("id") int id);

    /**
     * 更新
     */
    public int update(TranslationTaskRecordEntity entity);

    /**
     * 更新命令行
     * @param id
     * @param commandLineConfig
     * @return
     */
    public int updateCommandLineConfigById(@Param("id") int id, @Param("commandLineConfig") String commandLineConfig, @Param("status") String status);

    /**
     * 更新命令行文件和告警
     * @param id
     * @param commandLineConfig
     * @param status
     * @param warning
     * @return
     */
    public int updateCommandLineConfigAndWarningById(@Param("id") int id, @Param("commandLineConfig") String commandLineConfig,
                                                     @Param("status") String status,@Param("warning") String warning);

    /**
     * 更新状态
     * @param id
     * @param status
     * @return
     */
    public int updateStatusById(@Param("id") int id, @Param("status") String status);

    /**
     * 更新状态和告警
     * @param id
     * @param status
     * @return
     */
    public int updateStatusAndWarningById(@Param("id") int id, @Param("status") String status,@Param("warning") String warning);

    /**
     * get查询 By Id
     */
    public TranslationTaskRecordEntity getById(@Param("id") int id);

    /**
     * get查询 By Id
     */
    public List<TranslationTaskRecordEntity> getByIdList(@Param("idList") List<Integer> idList);

    /**
     * get查询
     */
    public TranslationTaskRecordEntity getByUUID(@Param("uuid") String uuid);

    /**
     * 获取翻译的命令行配置信息
     * @param uuid
     * @return
     */
    public String getCommandLineConfigByUUID(@Param("uuid") String uuid);

    /**
     * 查询 List Data
     */
    public List<TranslationTaskRecordEntity> findList(TranslationTaskRecordEntity entity);

    /**
     * 查询 List Data
     */
    public List<TranslationTaskRecordVO> findVOList(TranslationTaskRecordEntity entity);

    /**
     * 查询Count
     */
    public int count();

    /**
     * 通过状态查询
     * @param status
     * @return
     */
    List<TranslationTaskRecordEntity> getByStatus(String status);
}
