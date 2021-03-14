package com.bonelf.frame.core.validator;

import com.bonelf.frame.core.validator.annotation.EnumValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>
 * 枚举值校验
 * </p>
 * @author bonelf
 * @since 2020/7/9 9:21
 */
public class EnumValidator implements ConstraintValidator<EnumValid, Object> {
    private EnumValid annotation;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (annotation.permitCode().length > 1) {
            for (String s : annotation.permitCode()) {
                if (Objects.equals(s, value.toString())) {
                    return true;
                }
            }
            return false;
        }

        Object[] objects = annotation.clazz().getEnumConstants();
        try {
            Method method = annotation.clazz().getMethod(annotation.method());
            for (Object o : objects) {
                if (Objects.equals(value, method.invoke(o))) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
