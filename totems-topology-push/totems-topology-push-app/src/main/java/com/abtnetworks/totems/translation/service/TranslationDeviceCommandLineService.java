package com.abtnetworks.totems.translation.service;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.translation.entity.TranslationTaskMappingEntity;
import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;

import java.util.List;
import java.util.Map;

/**
 * @Description 策略迁移信息表
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
public interface TranslationDeviceCommandLineService {

    /**
     * 创建 策略集 策略对象 命令行
     * @param taskRecord
     * @param translationCommandlineBean
     * @param args
     * @return
     * @throws Exception
     */
    public Map<String,String> createFilterListAndRuleList(TranslationTaskRecordEntity taskRecord, List<TranslationTaskMappingEntity> deviceZoneMappingList , TranslationCommandline translationCommandlineBean, Object... args) throws Exception;

    /**
     * 开始生成策略
     * @param taskRecord
     * @param securityClass
     * @return
     * @throws Exception
     */
    String startTranslation(TranslationTaskRecordEntity taskRecord, Class securityClass) throws Exception;

    /**
     * 获取策略总数
     * @param sourceDeviceUuid
     * @return
     */
    public int getPolicyTotalNum(String sourceDeviceUuid);
}
