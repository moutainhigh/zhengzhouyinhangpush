package com.abtnetworks.totems.recommend.utils;

import java.util.Set;

/**
 * @desc
 * @author liuchanghao
 * @date 2020-12-31 10:51
 */
public class ObjectUtils {

    /**
     * 判断2个set集合是否一样
     * @param left
     * @param right
     * @param <T>
     * @return
     */
    public static <T> boolean isSameSet(Set<T> left, Set<T> right) {
        if ((left == null) && (right == null)) {
            return true;
        }

        if ((left == null) && (right != null) && (right.isEmpty())) {
            return true;
        }
        if ((left != null) && (left.isEmpty()) && (right == null)) {
            return true;
        }

        if ((left != null) && (right == null)) {
            return false;
        }

        if ((left == null) && (right != null)) {
            return false;
        }

        return left.equals(right);
    }
}
