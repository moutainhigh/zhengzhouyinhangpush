package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.send.SensitiveWordCommonService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-17
 */
@Service
public class SensitiveWordCommonServiceImpl implements SensitiveWordCommonService {
    @Override
    public boolean checkSensitiveWord(String regStr) {
        Pattern pattern = Pattern.compile(SendCommandStaticAndConstants.PASSWORD, Pattern.CASE_INSENSITIVE);
        boolean boolPattern = pattern.matcher(regStr).find();
        return boolPattern;
    }
}
