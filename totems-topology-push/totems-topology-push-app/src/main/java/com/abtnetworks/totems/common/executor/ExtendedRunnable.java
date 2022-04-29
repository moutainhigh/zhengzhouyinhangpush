package com.abtnetworks.totems.common.executor;

import com.abtnetworks.totems.common.config.ThreadPoolExecutorConfig;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hw
 * @Description
 * @Date 18:03 2019/3/25
 */
public abstract class ExtendedRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedRunnable.class);


    /**
     * 初始化启动线程执行的具体方法
     * 在start执行过程中，try catch中的异常信息要throw出来，或者不要使用try catch
     * @throws InterruptedException
     * @throws Exception
     */
    protected abstract void start() throws InterruptedException, Exception;

    /**
     * 线程info数据信息
     */
    private ExecutorDto executorDto;

    public ExtendedRunnable(ExecutorDto executorDto) {
        this.executorDto = executorDto;
    }

    public ExecutorDto getExecutorDto() {
        return executorDto;
    }

    public void setExecutorDto(ExecutorDto executorDto) {
        this.executorDto = executorDto;
    }

    @Override
    public void run() {
        if (AliStringUtils.isEmpty(executorDto.getId())){
            try {
                throw new Exception("请命名启动线程的id！");
            } catch (Exception e) {
                logger.error("线程Id为空",e);
            }
            return;
        }
        if (ThreadPoolExecutorConfig.RUNNING_MAP.containsKey(executorDto.getId())
                && ThreadPoolExecutorConfig.INFO_MAP.containsKey(executorDto.getId())){
            try {
                throw new Exception("请勿重复提交相同的线程id！");
            } catch (Exception e) {
                logger.error("线程Id重复",e);
            }
            return;
        }
        try {
            beforeExecute(executorDto, Thread.currentThread());
            this.start();
        } catch (InterruptedException e){
            logger.error("线程InterruptedException:"+executorDto.toString(),e);
        } catch (Exception e) {
            logger.error("线程未知Exception:"+executorDto.toString(),e);
        } finally {
            afterExecute(executorDto);
        }
    }

    protected void beforeExecute(ExecutorDto dto,Thread t){
        ThreadPoolExecutorConfig.RUNNING_MAP.put(dto.getId(), t);
        ThreadPoolExecutorConfig.INFO_MAP.put(dto.getId(),dto);
        logger.info("beforeExecute>> RUNING_MAP.size():"+ThreadPoolExecutorConfig.RUNNING_MAP.size()+", INFO_MAP.size():"+ThreadPoolExecutorConfig.INFO_MAP.size()+", 线程info"+dto.toString());
    }

    protected void afterExecute(ExecutorDto dto){
        ThreadPoolExecutorConfig.RUNNING_MAP.remove(dto.getId());
        ThreadPoolExecutorConfig.INFO_MAP.remove(dto.getId());
        logger.info("afterExecute>> RUNING_MAP.size():"+ThreadPoolExecutorConfig.RUNNING_MAP.size()+", INFO_MAP.size():"+ThreadPoolExecutorConfig.INFO_MAP.size()+", 线程info"+dto.toString());
    }
}
