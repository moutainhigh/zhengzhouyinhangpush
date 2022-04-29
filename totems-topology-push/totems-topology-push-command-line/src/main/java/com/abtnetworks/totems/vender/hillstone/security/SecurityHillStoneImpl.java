package com.abtnetworks.totems.vender.hillstone.security;

import com.abtnetworks.totems.command.line.enums.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version 山石命令行实现类
 * @Created by hw on '2021/8/9 14:39'.
 */
public class SecurityHillStoneImpl extends SecurityHillStoneR5Impl {

    @Override
    public String deleteSecurityPolicyByIdOrName(RuleIPTypeEnum ipTypeEnum,String id, String name, Map<String, Object> map, String[] args) {
        if(StringUtils.isBlank(id)){
            return StringUtils.EMPTY;
        }
        return String.format("no rule id %s \n",id);
    }

}
