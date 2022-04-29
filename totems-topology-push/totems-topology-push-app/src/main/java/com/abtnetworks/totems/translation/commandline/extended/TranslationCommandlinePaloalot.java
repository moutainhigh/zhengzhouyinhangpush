package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.paloalot.security.SecurityPaloalotImpl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlinePaloalot extends TranslationCommandline {

    public TranslationCommandlinePaloalot() {
        this.generatorBean = new SecurityPaloalotImpl();
    }

}
