package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.topsec.TOS_010.SecurityTopsec010Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineTopsecTos006 extends TranslationCommandline {

    public TranslationCommandlineTopsecTos006() {
        this.generatorBean = new SecurityTopsec010Impl();
    }

}
