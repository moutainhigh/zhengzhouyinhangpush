package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.dptech.r004.SecurityDp004Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineDptechr004 extends TranslationCommandline {

    public TranslationCommandlineDptechr004() {
        this.generatorBean = new SecurityDp004Impl();
    }

}
