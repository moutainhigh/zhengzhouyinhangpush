package com.abtnetworks.totems.common.commandline2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zc
 * @date 2020/01/06
 */
@Slf4j
@Component
public class CommandFactory {

    @Resource
    private CommandGenerator commandGenerator;

    public CommandGenerator getCommandGenerator(String modelNumber) {
        return commandGenerator;
    }

}
