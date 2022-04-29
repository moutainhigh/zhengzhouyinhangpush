package com.abtnetworks.totems.recommend.dto.task;

public class ElapseTime {
    Long count = 0L;
    Long max = 0L;
    Long min = 0L;
    Long total = 0L;

    public Long getCount() {
        return count;
    }

    public Long getMax() {
        return max;
    }

    public Long getMin() {
        return min;
    }

    public Long getTotal() {
        return total;
    }

    public void setMaxTime(Long time) {
        if (time>max) {
            max = time;
        }
    }

    public void setMinTime(Long time) {
        if(min == 0) {
            min = time;
            return;
        }
        if(time < min) {
            min = time;
        }
    }

    public void setCount() {
        count = count + 1;
    }

    public void setTotal(Long time) {
        total = total + time;
    }

    @Override
    public String toString() {
        return String.format("Max：%d, Min:%d, Average:%d, count:%d\n", max, min, count == 0?0:total/count, count);
    }

}
