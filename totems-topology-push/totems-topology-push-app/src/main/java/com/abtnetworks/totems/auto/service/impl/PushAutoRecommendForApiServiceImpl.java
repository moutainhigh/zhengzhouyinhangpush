package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.service.PushAutoRecommendForApiService;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.auto.vo.AutoRecommendBatchTaskVO;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskApiVO;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuchanghao
 * @desc 自动开通工单接口实现类
 * @date 2022-01-11 14:03
 */
@Service
public class PushAutoRecommendForApiServiceImpl implements PushAutoRecommendForApiService {

    private static Logger logger = LoggerFactory.getLogger(PushAutoRecommendForApiServiceImpl.class);

    @Autowired
    private PushAutoRecommendService pushAutoRecommendService;

    @Override
    public ReturnT<List<String>> createTask(AutoRecommendTaskApiVO vo) throws Exception {
        List<String> resultName = new ArrayList<>();
        try{
            // 1.生成任务-命令行
            logger.info("----------开始OA对接，生成自动开通工单任务，请求数据：{}----------", JSON.toJSONString(vo));
            if(CollectionUtils.isEmpty(vo.getBatchTaskVOList())){
                logger.error("当前未填写任务五元组信息数据，不开通");
                return new ReturnT(ReturnT.FAIL_CODE, "\"当前未填写任务五元组信息数据，不开通\"");
            }
            List<AutoRecommendBatchTaskVO> batchTaskVOList = vo.getBatchTaskVOList();
            for(AutoRecommendBatchTaskVO batchTaskVO : batchTaskVOList ){
                AutoRecommendTaskVO taskVO = new AutoRecommendTaskVO();
                BeanUtils.copyProperties(vo, taskVO);
                taskVO.setSrcIp(batchTaskVO.getSrcIp());
                taskVO.setDstIp(batchTaskVO.getDstIp());
                taskVO.setServiceList(batchTaskVO.getServiceList());
                String theme = vo.getTheme() + "-" + batchTaskVO.getId();
                taskVO.setTheme(theme);
                taskVO.setAccessType(batchTaskVO.getAccessType());
                taskVO.setStartTime(batchTaskVO.getStartTime());
                taskVO.setEndTime(batchTaskVO.getEndTime());
                // 将拼接的主题名称添加到返回结果里面
                resultName.add(theme);
                ReturnT<Integer> createRecord = pushAutoRecommendService.addTask(taskVO);
                if(createRecord.getCode() == ReturnT.FAIL_CODE){
                    logger.error("OA对接创建工单任务异常，异常原因：", createRecord.getMsg());
                    return new ReturnT(ReturnT.FAIL_CODE, createRecord.getMsg());
                }
                Integer id = createRecord.getData();
                logger.info("----------OA对接创建工单成功，工单ID:{}, 需求信息:{}----------", id, JSON.toJSONString(batchTaskVO));
            }
            logger.info("----------IT网站对接自动下发命令行成功，工单名称:{},流程结束----------", resultName);
            return new ReturnT(resultName);
        } catch (Exception e){
            logger.error("OA对接创建自动开通工单任务异常，异常原因：", e);
            throw e;
        }
    }


}
