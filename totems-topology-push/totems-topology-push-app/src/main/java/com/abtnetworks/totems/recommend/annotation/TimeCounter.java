package com.abtnetworks.totems.recommend.annotation;

import javax.swing.text.Element;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeCounter {
}
