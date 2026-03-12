package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
* 自定义注解，用于标识某个方法需要进行公共字段自动填充处理
* */
@Target(ElementType.METHOD)  // 标识注解的作用目标
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 枚举值，用于指定填充逻辑类型
    // INSERT:插入数据时填充
    // UPDATE:更新数据时填充
    // INSERT_UPDATE:插入和更新数据时填充
    //数据库操作类型: 1.插入  2.更新
    OperationType value();
}
