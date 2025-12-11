package com.twog.shopping.domain.log.aop;

import com.twog.shopping.domain.log.entity.HistoryActionType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogHistory {
    HistoryActionType actionType();
}
