package com.abtnetworks.totems.translation.service.impl;

import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.lang.TotemsStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.translation.dao.TranslationTaskMappingMapper;
import com.abtnetworks.totems.translation.dao.TranslationTaskRecordMapper;
import com.abtnetworks.totems.translation.entity.TranslationTaskMappingEntity;
import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.enums.CommandLineTranslationStatus;
import com.abtnetworks.totems.translation.enums.TranslationCommandlineEnum;
import com.abtnetworks.totems.translation.service.TranslationDeviceCommandLineService;
import com.abtnetworks.totems.translation.service.TranslationTaskRecordService;
import com.abtnetworks.totems.translation.vo.TranslationTaskRecordVO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.abtnetworks.totems.disposal.BaseService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description ?????????????????????
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
@Service
public class TranslationTaskRecordServiceImpl extends BaseService implements TranslationTaskRecordService {

    @Resource
    private TranslationTaskRecordMapper pushTranslationTaskRecordDao;

    @Resource
    private TranslationTaskMappingMapper pushTranslationTaskMappingDao;

    @Resource
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    private NodeMapper nodeMapper;

    @Resource
    private TranslationDeviceCommandLineService translationDeviceCommandLineService;

    private static final String STOP_TYPE_INTERUPT = "interrupt";

    private static final String STOP_TYPE_STOP = "stop";

    @Autowired
    @Qualifier(value = "translationTaskExecutor")
    private ThreadPoolTaskExecutor translationTaskExecutor;

    @Value("${translation-directory.handler}")
    private String translationDirectoryHandler;

    @Value("${translation-directory.fileDir}")
    private String translationDirectoryFileDir;

    /**
     * ??????????????????
     * @param taskRecord
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT startTranslation(TranslationTaskRecordEntity taskRecord) throws Exception {
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(taskRecord.getDeviceUuid());

        if (deviceRO == null || deviceRO.getData() == null || deviceRO.getData().size() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "???????????????????????????????????????");
        }

        TranslationCommandlineEnum translationCommandlineEnum = TranslationCommandlineEnum.fromString(taskRecord.getTargetDeviceModelNumber());

        if (translationCommandlineEnum == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "?????????????????????????????????????????????");
        }

        translationTaskExecutor.execute(new ExtendedRunnable(new ExecutorDto(taskRecord.getUuid(),taskRecord.getTitleName(),taskRecord.getId()+"_"+taskRecord.getTitleName(),new Date())){
            @Override
            protected void start() throws InterruptedException, Exception {

                String commandLine = null;
                try {
                    commandLine = translationDeviceCommandLineService.startTranslation(taskRecord,translationCommandlineEnum.getTranslationImplClass());
                } catch (Exception e) {
                    logger.error("translationTaskExecutor error:",e);
                    pushTranslationTaskRecordDao.updateStatusAndWarningById(taskRecord.getId(), CommandLineTranslationStatus.FAIL.getCode(),e.getMessage());
                    return;
                } finally {
                    TranslationDeviceCommandLineServiceImpl.progressMap.remove(taskRecord.getUuid());
                }
                try {
                    logger.info("??????????????????{}?????????????????????????????????????????????",taskRecord.getTitleName());
                    String fileName = "????????????-"+taskRecord.getTitleName()+".conf";
                    File file = new File(translationDirectoryFileDir+fileName);
                    if (file.exists() && !FileUtils.deleteQuietly(file)) {
                        logger.error("?????????????????????"+fileName);
                    }
                    FileUtils.writeStringToFile(file,commandLine, Charset.forName("UTF-8"));
                    String downloadFileName = translationDirectoryHandler+fileName;

                    logger.info("??????????????????{} ?????????????????????????????????",taskRecord.getTitleName());
                    // ????????????warning???????????????text??????????????????65,535,??????????????????????????????6000????????????????????????????????????warning??????
                    if(TotemsStringUtils.isNotBlank(taskRecord.getWarning()) && taskRecord.getWarning().length() > 60000){
                        String warningFileName = translationDirectoryFileDir+File.separator+"warning"+File.separator+"????????????-"+taskRecord.getTitleName()+".error.log";
                        File warningFile = new File(warningFileName);
                        if(!warningFile.getParentFile().exists()){
                            warningFile.getParentFile().mkdirs();
                        }
                        if (warningFile.exists() && !FileUtils.deleteQuietly(warningFile)) {
                            logger.error("?????????????????????"+warningFile.getAbsolutePath());
                        }
                        FileUtils.writeStringToFile(warningFile,taskRecord.getWarning(), Charset.forName("UTF-8"));
                        taskRecord.setWarning(warningFileName);
                    }
                    pushTranslationTaskRecordDao.updateCommandLineConfigAndWarningById(taskRecord.getId(), downloadFileName, CommandLineTranslationStatus.SUCCESS.getCode(),taskRecord.getWarning());
                } catch (Exception e){
                    logger.error("??????????????????"+taskRecord.getTitleName()+"??????????????????",e);
                    pushTranslationTaskRecordDao.updateStatusAndWarningById(taskRecord.getId(), CommandLineTranslationStatus.FAIL.getCode(),e.getMessage());
                }finally {
                    TranslationDeviceCommandLineServiceImpl.progressMap.remove(taskRecord.getUuid());
                }
            }
        });
        pushTranslationTaskRecordDao.updateStatusById(taskRecord.getId(),CommandLineTranslationStatus.CONVERTING.getCode());
        return ReturnT.SUCCESS;
    }

    /**
     * ??????????????????
     * @param id
     * @return
     */
    @Override
    public ReturnT cancelTranslation(int id){
        TranslationTaskRecordEntity entity = pushTranslationTaskRecordDao.getById(id);
        //????????????
        boolean stopped = ExtendedExecutor.stop(entity.getUuid(), STOP_TYPE_INTERUPT);

        if(stopped){
            //???????????????
            TranslationDeviceCommandLineServiceImpl.progressMap.remove(entity.getUuid());
            //?????????????????????
            pushTranslationTaskRecordDao.updateStatusById(id,CommandLineTranslationStatus.FAIL.getCode());
            return ReturnT.SUCCESS;
        } else{
            return ReturnT.FAIL;
        }
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> insert(TranslationTaskRecordEntity entity) {

        // valid
        if (StringUtils.isAnyBlank(entity.getTitleName(), entity.getDeviceUuid(), entity.getDeviceName(),
                entity.getTargetDeviceVendorId(),entity.getTargetDeviceModelNumber())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "???????????????????????????");
        }

        entity.setUuid(IdGen.uuid());
        entity.setStatus(CommandLineTranslationStatus.NOT_STARTED.getCode());
        pushTranslationTaskRecordDao.insert(entity);
        //???????????????????????????
        pushTranslationTaskMappingDao.deleteByTaskId(entity.getUuid());
        List<TranslationTaskMappingEntity> mappingList = entity.getMappingList();
        if(CollectionUtils.isNotEmpty(mappingList)){
            List<TranslationTaskMappingEntity> needAddMappingList = new ArrayList<>();
            for (TranslationTaskMappingEntity mappingEntity : mappingList) {
                // ???????????????
                if(StringUtils.isBlank(mappingEntity.getSourceValue()) || StringUtils.isBlank(mappingEntity.getTargetValue())){
                    continue;
                }
                mappingEntity.setUuid(IdGen.uuid());
                mappingEntity.setTaskUuid(entity.getUuid());
                mappingEntity.setCreateTime(new Date());
                needAddMappingList.add(mappingEntity);
            }
            if(CollectionUtils.isNotEmpty(needAddMappingList)){
                //??????
                pushTranslationTaskMappingDao.insertList(needAddMappingList);
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> delete(int id) {
        TranslationTaskRecordEntity entity = pushTranslationTaskRecordDao.getById(id);
        if(entity == null){
            return ReturnT.FAIL;
        }
        if(entity.getStatus().equalsIgnoreCase(CommandLineTranslationStatus.CONVERTING.getCode())){
            return new ReturnT<String>(ReturnT.FAIL_CODE, "?????????????????????????????????");
        }

        //??????????????????
        int mappingNum = pushTranslationTaskMappingDao.deleteByTaskId(entity.getUuid());
        //??????????????????
        int ret = pushTranslationTaskRecordDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ????????????
     * @param ids
     * @return
     */
    @Override
    public ReturnT<String> batchDelete(String ids){
        if(StringUtils.isBlank(ids)){
            return new ReturnT<String>(ReturnT.FAIL_CODE, "ids??????????????????");
        }
        String[] idArr = StringUtils.split(ids, ",");
        List<Integer> idList = new ArrayList<>();
        for (String idStr : idArr) {
            int id = Integer.parseInt(idStr);
            idList.add(id);
        }
        List<TranslationTaskRecordEntity> taskList = pushTranslationTaskRecordDao.getByIdList(idList);
        for (TranslationTaskRecordEntity translationTaskRecordEntity : taskList) {
            if(translationTaskRecordEntity.getStatus().equalsIgnoreCase(CommandLineTranslationStatus.CONVERTING.getCode())){
                return new ReturnT<String>(ReturnT.FAIL_CODE, "?????????????????????????????????");
            }
        }
        for (Integer id : idList) {
            this.delete(id);
        }
       return ReturnT.SUCCESS;
    }

    /**
     * ??????
     */
    @Override
    public ReturnT<String> update(TranslationTaskRecordEntity entity) {

        // valid
        if (StringUtils.isAnyBlank(entity.getUuid(), entity.getTitleName(), entity.getDeviceUuid(), entity.getDeviceName(),
                entity.getTargetDeviceVendorId(), entity.getTargetDeviceModelNumber())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "???????????????????????????");
        }

        TranslationTaskRecordEntity oldEntity = pushTranslationTaskRecordDao.getById(entity.getId());
        //????????????????????????
        if(oldEntity.getStatus().equalsIgnoreCase(CommandLineTranslationStatus.CONVERTING.getCode())){
            return new ReturnT<String>(ReturnT.FAIL_CODE, "???????????????????????????");
        }
        //?????????????????????????????????
        if(!oldEntity.getDeviceUuid().equals(entity.getDeviceUuid()) || !oldEntity.getTargetDeviceUuid().equals(entity.getTargetDeviceUuid())){
            oldEntity.setStatus(CommandLineTranslationStatus.NOT_STARTED.getCode());
        }

        int ret = pushTranslationTaskRecordDao.update(entity);
        pushTranslationTaskRecordDao.updateStatusById(entity.getId(),oldEntity.getStatus());

        //???????????????????????????
        pushTranslationTaskMappingDao.deleteByTaskId(entity.getUuid());

        List<TranslationTaskMappingEntity> mappingList = entity.getMappingList();
        if(CollectionUtils.isNotEmpty(mappingList)){
            List<TranslationTaskMappingEntity> needAddMappingList = new ArrayList<>();
            for (TranslationTaskMappingEntity mappingEntity : mappingList) {
                // ???????????????
                if(StringUtils.isBlank(mappingEntity.getSourceValue()) || StringUtils.isBlank(mappingEntity.getTargetValue())){
                    continue;
                }
                mappingEntity.setUuid(IdGen.uuid());
                mappingEntity.setTaskUuid(entity.getUuid());
                mappingEntity.setCreateTime(new Date());
                needAddMappingList.add(mappingEntity);
            }
            if(CollectionUtils.isNotEmpty(needAddMappingList)){
                //??????
                pushTranslationTaskMappingDao.insertList(needAddMappingList);
            }
        }

        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    /**
     * ?????? get By Id
     */
    @Override
    public TranslationTaskRecordEntity getById(int id) {
        TranslationTaskRecordEntity taskRecordEntity = pushTranslationTaskRecordDao.getById(id);
        if(taskRecordEntity != null){
            taskRecordEntity.setMappingList(pushTranslationTaskMappingDao.findVOListByTaskUuid(taskRecordEntity.getUuid()));
        }
        return pushTranslationTaskRecordDao.getById(id);
    }

    /**
     * ?????? get
     */
    @Override
    public TranslationTaskRecordEntity getByUUID(String uuid) {
        return pushTranslationTaskRecordDao.getByUUID(uuid);
    }

    @Override
    public String getCommandLineConfigByUUID(String uuid) throws Exception {
        return pushTranslationTaskRecordDao.getCommandLineConfigByUUID(uuid);
    }

    /**
     * ????????????
     */
    @Override
    public PageInfo<TranslationTaskRecordVO> findList(TranslationTaskRecordEntity entity, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        // result
        List<TranslationTaskRecordVO> list = pushTranslationTaskRecordDao.findVOList(entity);
        PageInfo<TranslationTaskRecordVO> pageInfo = new PageInfo<>(list);
        if(CollectionUtils.isNotEmpty(list)){
            Map<String, String> deviceTypeMap = nodeMapper.getNodeList().stream().filter(t -> StringUtils.isNotBlank(t.getUuid())).collect(Collectors.toMap(t -> t.getUuid(), t -> t.getType()));
            for (TranslationTaskRecordVO translationTaskRecordVO : list) {
                if(StringUtils.isNotBlank(translationTaskRecordVO.getDeviceUuid())){
                    translationTaskRecordVO.setDeviceType(deviceTypeMap.get(translationTaskRecordVO.getDeviceUuid()));
                    if(!deviceTypeMap.containsKey(translationTaskRecordVO.getDeviceUuid())){
                        translationTaskRecordVO.setDeviceUuid(null);
                        translationTaskRecordVO.setDeviceName("????????????");
                        translationTaskRecordVO.setDeviceIsDelete(true);
                    }
                }
                if(StringUtils.isNotBlank(translationTaskRecordVO.getTargetDeviceUuid())){
                    translationTaskRecordVO.setTargetDeviceType(deviceTypeMap.get(translationTaskRecordVO.getTargetDeviceUuid()));
                    if(!deviceTypeMap.containsKey(translationTaskRecordVO.getTargetDeviceUuid())){
                        translationTaskRecordVO.setTargetDeviceUuid(null);
                        translationTaskRecordVO.setTargetDeviceName("????????????");
                        translationTaskRecordVO.setTargetDeviceIsDelete(true);
                    }
                }
                translationTaskRecordVO.setMappingList(pushTranslationTaskMappingDao.findVOListByTaskUuid(translationTaskRecordVO.getUuid()));

                if(TotemsStringUtils.isNotBlank(translationTaskRecordVO.getWarning()) && translationTaskRecordVO.getWarning().startsWith(translationDirectoryFileDir)){
                    try {
                        File warningFile = new File(translationTaskRecordVO.getWarning());
                        if(warningFile.exists()){
                            translationTaskRecordVO.setWarning(FileUtils.readFileToString(warningFile,"UTF-8"));
                        }
                    } catch (Exception e) {
                        logger.error("?????????????????????????????????",e);
                    }
                }
            }
        }
        return pageInfo;
    }

    @Override
    public ReturnT getTranslationTaskProgress(Integer id) {
        TranslationTaskRecordEntity taskEntity = pushTranslationTaskRecordDao.getById(id);
        if(taskEntity == null || !TranslationDeviceCommandLineServiceImpl.progressMap.containsKey(taskEntity.getUuid())){
            return new ReturnT(null);
        }
        return new ReturnT(TranslationDeviceCommandLineServiceImpl.progressMap.get(taskEntity.getUuid()));
    }


    @PostConstruct
    public void deleteErrorTask(){
        List<TranslationTaskRecordEntity> taskEntitys = pushTranslationTaskRecordDao.getByStatus(CommandLineTranslationStatus.CONVERTING.getCode());
        if(CollectionUtils.isEmpty(taskEntitys)){
            return;
        }
        for (TranslationTaskRecordEntity taskEntity : taskEntitys) {
            if(!TranslationDeviceCommandLineServiceImpl.progressMap.containsKey(taskEntity.getUuid())){
                pushTranslationTaskRecordDao.updateStatusById(taskEntity.getId(),CommandLineTranslationStatus.NOT_STARTED.getCode());
            }
        }
    }
}
