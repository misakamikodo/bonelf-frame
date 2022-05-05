package com.bonelf.frame.core.validator;

import com.bonelf.frame.core.validator.constraints.ByteLength;
import com.bonelf.frame.core.validator.constraints.EnumValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * <p>
 * 字节长度校验
 * </p>
 * @author bonelf
 * @since 2020/7/9 9:21
 */
public class ByteLengthValidator implements ConstraintValidator<ByteLength, Object> {
    private ByteLength annotation;

    @Override
    public void initialize(ByteLength constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String val = value.toString();
        int length = val.getBytes(Charset.forName(annotation.charset())).length;
        return length <= annotation.max() && length >= annotation.min();
    }
}
