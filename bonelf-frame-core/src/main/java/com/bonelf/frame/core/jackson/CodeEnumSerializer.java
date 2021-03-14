/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.core.jackson;

import com.bonelf.cicada.enums.CodeValueEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 枚举类只返回code
 * @author bonelf
 * @date 2020-11-27 09:12:00
 */
@Slf4j
public class CodeEnumSerializer extends StdSerializer<CodeValueEnum<?>> {
	protected CodeEnumSerializer(Class<CodeValueEnum<?>> t) {
		super(t);
	}

	@Override
	public void serialize(CodeValueEnum value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value.getCode().getClass() == String.class) {
			gen.writeString(value.getCode().toString());
		} else if (value.getCode().getClass() == Long.class ||
				value.getCode().getClass() == Long.TYPE ||
				value.getCode().getClass() == BigDecimal.class ||
				value.getCode().getClass() == Double.class ||
				value.getCode().getClass() == Double.TYPE ||
				value.getCode().getClass() == Integer.class ||
				value.getCode().getClass() == Integer.TYPE) {
			gen.writeNumber(value.getCode().toString());
		} else {
			gen.writeObject(value.getCode());
		}
	}
}
