package com.abtnetworks.totems.common.commandline.acl;

import com.abtnetworks.totems.common.commandline.PolicyGenerator;
import com.abtnetworks.totems.common.dto.CmdDTO;

import java.io.UnsupportedEncodingException;

/**
 * @author Administrator
 * @Title:
 * @Description: 扩展类
 * @date 2020/9/17
 */
public interface PolicyExtendGenerateService extends PolicyGenerator {
    /**
     * 获取id
     * @param cmdDto
     * @return
     * @throws UnsupportedEncodingException
     */
    String getZonePairId(CmdDTO cmdDto) throws UnsupportedEncodingException;
}
