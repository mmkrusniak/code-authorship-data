package com.miolean.arena.genetics;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GeneDescription {

    String description() default "";
    String arg0() default "";
    String arg1() default "";
    String arg2() default "";

}
