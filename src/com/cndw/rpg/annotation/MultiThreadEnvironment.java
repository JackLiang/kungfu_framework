/**
* Filename    : MultiThreadEnvironment.java
* Author      : Jack
* Create time : 2015-4-14 下午2:14:29
* Description :
*/
package com.cndw.rpg.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MultiThreadEnvironment
{
}