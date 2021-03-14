package com.bonelf.cicada.util;

import com.bonelf.cicada.enums.CodeValueEnum;

/**
 * 枚举工厂
 * @author bonelf
 * @since 2020/8/31 17:37
 */
public class EnumUtil {
    
    /**
     * 注释：通过code 获取value
     */
    public static <T extends CodeValueEnum<?>> T getByCode(Object code, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            if (each.getCode().toString().equals(code.toString())) {
                return each;
            }
        }
        throw new NullPointerException("invalid enum code");
    }

    public static <T extends CodeValueEnum<?>> T getByCodeCanNull(Object code, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            if (each.getCode().equals(code)) {
                return each;
            }
        }
        return null;
    }

    /**
     * <p>
     * 获得中文值
     * </p>
     * @author bonelf
     * @since 2020/8/31 17:37
     */
    public static <T extends CodeValueEnum<?>> String getEnumString(Object code, Class<T> enumClass) {
        T enums = getByCodeCanNull(code, enumClass);
        return enums == null ? "-" : enums.getValue();
    }
}
