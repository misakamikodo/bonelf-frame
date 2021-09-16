package com.bonelf.frame.web.core.argresolver.databinder;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bonelf.frame.base.util.JsonUtil;
import lombok.Data;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.List;
import java.util.Locale;

/**
 * 动态查询参数适配（不建议使用，因为这相当于注入SQL，不过受限制）
 * query=[{field:name, op:=, value:ok}...]&order=[{field:name,sort:asc}]&select=[{field:name}]
 * @author bonelf
 * @date 2021/9/16 10:00
 */
public class QueryWrapperDataBinder extends ServletRequestDataBinder {

	private static final String PARAM_QUERY_DIR = "query";

	private static final String PARAM_ORDER_DIR = "order";

	private static final String PARAM_SELECT_DIR = "select";

	public QueryWrapperDataBinder(Object target) {
		super(target);
	}

	public QueryWrapperDataBinder(Object target, String objectName) {
		super(target, objectName);
	}

	@Override
	protected void doBind(@NonNull MutablePropertyValues mpvs) {
		String queryJsonStr = (String)mpvs.get(PARAM_QUERY_DIR);
		String orderJsonStr = (String)mpvs.get(PARAM_ORDER_DIR);
		String selectJsonStr = (String)mpvs.get(PARAM_SELECT_DIR);

		QueryWrapper<?> q = (QueryWrapper<?>)super.getTarget();
		if (q == null) {
			return;
		}

		if (StrUtil.isNotBlank(queryJsonStr)) {
			List<QueryArg> query = JsonUtil.parseArray(queryJsonStr, QueryArg.class);
			if (query == null) {
				return;
			}
			for (QueryArg queryArg : query) {
				QueryArg.Operator op = QueryArg.Operator.of(queryArg.getOp());
				if (op == null) {
					continue;
				}
				switch (op) {
					case IN:
						q.in(queryArg.getField(), (Object[])queryArg.getValue().split(","));
						break;
					case LESS:
						q.in(queryArg.getField(), queryArg.getValue());
						break;
					case EQUAL:
						q.eq(queryArg.getField(), queryArg.getValue());
						break;
					case NOT_EQUAL:
						q.ne(queryArg.getField(), queryArg.getValue());
						break;
					case CONTAINS:
						q.like(queryArg.getField(), queryArg.getValue());
						break;
					case BEGIN_WITH:
						q.likeLeft(queryArg.getField(), queryArg.getValue());
						break;
					case ENDWITH:
						q.likeRight(queryArg.getField(), queryArg.getValue());
						break;
					case GREATER:
						q.gt(queryArg.getField(), queryArg.getValue());
						break;
					case GREATER_OR_EQUAL:
						q.gt(queryArg.getField(), queryArg.getValue());
						break;
					case LESS_OR_EQUAL:
						q.le(queryArg.getField(), queryArg.getValue());
						break;
					case BETWEEN:
						String[] split = queryArg.getValue().split(",");
						if (split.length >= 2) {
							q.between(queryArg.getField(), split[0], split[1]);
						}
						break;
					default:
				}
			}
		}

		if (StrUtil.isNotBlank(orderJsonStr)) {
			List<OrderArg> order = JsonUtil.parseArray(orderJsonStr, OrderArg.class);
			if (order != null) {
				for (OrderArg orderArg : order) {
					if (OrderArg.DESC.equals(orderArg.getSort().toLowerCase(Locale.ROOT))) {
						q.orderByDesc(orderArg.getField());
					} else {
						q.orderByAsc(orderArg.getField());
					}
				}
			}
		}

		if (StrUtil.isNotBlank(selectJsonStr)) {
			List<SelectArg> select = JsonUtil.parseArray(selectJsonStr, SelectArg.class);
			if (select != null) {
				q.select(select.stream().map(SelectArg::getField).toArray(String[]::new));
			}
		}

	}

	/**
	 * 查询
	 */
	@Data
	private static class QueryArg {
		private String field;
		private String op;
		private String value;

		private enum Operator {
			/**
			 *
			 */
			EQUAL("="),
			NOT_EQUAL("!="),
			CONTAINS("*="),
			BEGIN_WITH("^="),
			ENDWITH("$="),
			GREATER(">"),
			GREATER_OR_EQUAL(">="),
			LESS("<"),
			LESS_OR_EQUAL("<="),
			IN("in"),
			BETWEEN("[]");

			private final String value;

			Operator(String value) {
				this.value = value;
			}

			public String getValue() {
				return value;
			}

			public static Operator of(String op) {
				for (Operator value : values()) {
					if (value.value.equals(op)) {
						return value;
					}
				}
				return null;
			}

		}

	}

	/**
	 * 排序
	 */
	@Data
	private static class OrderArg {
		private static String ASC = "asc";
		private static String DESC = "desc";

		private String field;
		private String sort;
	}

	/**
	 * 列
	 */
	@Data
	private static class SelectArg {
		private String field;
	}
}
