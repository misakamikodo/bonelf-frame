package com.bonelf.support.web.controller;

import com.bonelf.frame.core.domain.Result;
import com.bonelf.support.feign.domain.request.DictValueRequest;
import com.bonelf.support.feign.domain.response.DictTextResponse;
import com.bonelf.support.web.service.DictItemService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 定时任务在线管理
 * @author bonelf
 * @date  2019-01-02
 */
@RestController
@RequestMapping("/sys/dbdict")
@Slf4j
@Api(tags = "数据库字典接口")
public class DbDictController {
	@Autowired
	private DictItemService dictItemService;


	@GetMapping(value = "/v1/getTextByValue")
	public Result<String> getTextByValue(@RequestParam("dictId") String dictId, @RequestParam("itemValue") String itemValue){
		return Result.ok(dictItemService.getTextByValue(dictId, itemValue));
	}

	/**
	 * 复杂对象使用Post
	 * @param query
	 * @return
	 */
	@PostMapping(value = "/v1/getTextByValueBatch")
	public Result<Set<DictTextResponse>> getTextByValueBatch(@RequestBody Set<DictValueRequest> query){
		if(query.isEmpty()){
			return Result.error("参数为空");
		}
		return Result.ok(dictItemService.getTextByValueBatch(query));
	}

}
