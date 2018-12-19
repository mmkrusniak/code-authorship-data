package com.miolean.arena.genetics;


//Denotes that a method is a reflected command (a gene).

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GeneCommand{
    int args();
    double cost();
    int weight();
    int bonus() default 0;
    boolean defined() default false;
}
