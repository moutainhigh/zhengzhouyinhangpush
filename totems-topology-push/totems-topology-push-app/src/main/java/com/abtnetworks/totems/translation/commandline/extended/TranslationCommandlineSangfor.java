package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.sangfor.SecuritySangforImpl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineSangfor extends TranslationCommandline {

    public TranslationCommandlineSangfor() {
        this.generatorBean = new SecuritySangforImpl();
    }

}