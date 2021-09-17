package com.bonelf.frame.web.core.argresolver.databinder;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * 分页参数适配
 * @author bonelf
 * @date 2021/9/16 10:00
 */
public class PageDataBinder extends ServletRequestDataBinder {
	private String[] currentArgs = new String[]{};
	private String[] sizeArgs = new String[]{};
	private Long defaultPage = 0L;
	private Long defaultSize = 10L;

	public PageDataBinder(Object target) {
		super(target);
	}

	public PageDataBinder(Object target, String objectName,
						  Long defaultPage, Long defaultSize,
						  String[] currentArgs, String[] sizeArgs) {
		super(target, objectName);
		this.defaultPage = defaultPage;
		this.defaultSize = defaultSize;
		this.currentArgs = currentArgs;
		this.sizeArgs = sizeArgs;
	}

	@Override
	protected void doBind(@NonNull MutablePropertyValues mpvs) {
		// String page = (String)mpvs.get("page");
		// String current = (String)mpvs.get("current");

		// String size = (String)mpvs.get("size");
		// String pageSize = (String)mpvs.get("pageSize");
		// String limit = (String)mpvs.get("limit");
		// String rows = (String)mpvs.get("rows");

		Page<?> paging = new Page<>();
		paging.setCurrent(getFirstNotBlankValueOrElse(defaultPage, currentArgs));
		paging.setSize(getFirstNotBlankValueOrElse(defaultSize, sizeArgs));
		if (super.getTarget() != null) {
			BeanUtils.copyProperties(paging, super.getTarget());
		}
	}

	private Long getFirstNotBlankValueOrElse(Long elseVal, String... args) {
		for (String arg : args) {
			if (StrUtil.isNotBlank(arg)) {
				return Long.parseLong(arg);
			}
		}
		return elseVal;
	}
}
