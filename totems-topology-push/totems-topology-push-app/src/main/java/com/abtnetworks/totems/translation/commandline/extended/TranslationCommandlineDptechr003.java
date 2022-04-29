package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.dptech.r003.SecurityDp003Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineDptechr003 extends TranslationCommandline {

    public TranslationCommandlineDptechr003() {
        this.generatorBean = new SecurityDp003Impl();
    }

}
