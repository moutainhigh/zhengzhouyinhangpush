package com.abtnetworks.totems.common.commandline.acl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.commandline.SecurityPolicyGenerator;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.ActionEnum;
import com.abtnetworks.totems.common.utils.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.abtnetworks.totems.common.commandline.NatPolicyGenerator.DO_NOT_SUPPORT;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/15
 */
@Slf4j
@Service
public class AclH3cSecPathV7 extends SecurityPolicyGenerator implements PolicyGenerator {
    @Override
    public String generate(CmdDTO cmdDTO) {

        return DO_NOT_SUPPORT;
    }

    @Override
    public String generatePreCommandline(CommandlineDTO dto) {

        return "";
    }


    @Override
    public String generateCommandline(CommandlineDTO dto) {

        return "";
    }

    @Override
    public String generatePostCommandline(CommandlineDTO dto) {
        return "";
    }


}
