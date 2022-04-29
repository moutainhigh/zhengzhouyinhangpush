package com.abtnetworks.totems.auto.schedule;

import com.abtnetworks.totems.auto.dao.mysql.AutoRecommendTaskMapper;
import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.service.PushAutoTaskEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @desc 自动开通即将过期策略定时任务类
 * @author zhoumuhua
 * @date 2021-07-08
 */
@Component
@Slf4j
public class WillExpirePolicySchedule {

    @Autowired
    PushAutoTaskEmailService pushAutoTaskEmailService;

    @Autowired
    AutoRecommendTaskMapper autoRecommendTaskMapper;

    /**
     * 定时任务执行方法
     * 每隔5分种执行一次：“0 * /5 * * * ?”
     * 每天中午12点执行："0 0 12 * * ?"
     * 每天晚上12点执行：”0 0 0 ? * ?“
     */
    //@Scheduled(cron = "0 */5 * * * ?")
    //@Scheduled(cron = "0 0 0 ? * ?")
    @KafkaListener(topics = "sendWillExpirePolicyEmail")
    public void sendWillExpirePolicyEmailTask() {

        log.info("定时发送即将过期策略邮件--->开始");

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 7);
        //获取七天后日期
        Date expireTime = c.getTime();
        //获取所有设置了过期时间且下发成功的任务工单，考虑达梦数据库兼容性，不再sql中使用日期函数
        List<AutoRecommendTaskEntity> autoTaskEntityList = autoRecommendTaskMapper.getWillExpireTask();
        //获取7天后即将过期的数据--结束时间在七天后的时间之前且在当前时间之后
        List<AutoRecommendTaskEntity> sendEmailList = autoTaskEntityList.stream().filter(task -> task.getEndTime().before(expireTime) && task.getEndTime().after(new Date())).collect(Collectors.toList());

        for (AutoRecommendTaskEntity autoTaskEntity : sendEmailList) {
            log.info("当前发送工单主题{}", autoTaskEntity.getTheme());
            //执行发送邮件逻辑
            pushAutoTaskEmailService.startWillExpirePolicyEmail(autoTaskEntity);
        }

        log.info("定时发送即将过期策略邮件--->结束");
    }

}
