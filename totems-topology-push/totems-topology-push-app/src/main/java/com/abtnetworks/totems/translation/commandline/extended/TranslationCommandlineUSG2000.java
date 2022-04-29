package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.dto.TranslationTaskProgressDTO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg2100Impl;
import com.abtnetworks.totems.vender.Usg.security.SecurityUsg6000Impl;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Yyh
 * @Description
 * @Date: 2021/5/26
 */
@Slf4j
public class TranslationCommandlineUSG2000 extends TranslationCommandline {

    public TranslationCommandlineUSG2000() {
        this.generatorBean = new SecurityUsg2100Impl();
    }
}
