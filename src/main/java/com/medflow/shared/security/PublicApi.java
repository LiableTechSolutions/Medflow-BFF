package com.medflow.shared.security;
import java.lang.annotation.*;
@Target({ElementType.TYPE, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) @Documented
public @interface PublicApi { }
