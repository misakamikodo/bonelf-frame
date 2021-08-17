package com.bonelf.frame.web.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SqlMapper {
	List<Map<String, Object>> dynamicsQuery(@Param("paramSQL") String sql);

	void dynamicsInsert(@Param("paramSQL") String sql);

	void dynamicsUpdate(@Param("paramSQL") String sql);

	void dynamicsDelete(@Param("paramSQL") String sql);
}