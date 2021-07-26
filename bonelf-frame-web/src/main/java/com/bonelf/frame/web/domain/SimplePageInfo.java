package com.bonelf.frame.web.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 简单分页
 * @author bonelf
 * @date 2021/6/8 17:54
 */
@Data
@AllArgsConstructor
public class SimplePageInfo<T> {
	protected List<T> records;

	protected long total;

	public static <E> SimplePageInfo<E> of(Page<E> mybatisPage) {
		return new SimplePageInfo<>(mybatisPage.getRecords(), mybatisPage.getTotal());
	}
}
