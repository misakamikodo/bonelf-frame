/*
 * Copyright (c) 2020. Bonelf.
 */

package com.bonelf.frame.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bonelf.frame.core.domain.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;

/**
 * controller层
 * 理论不用@Component
 */
@Component
public abstract class BaseApiController<S extends IService<E>, E> extends BaseController {
	//随着继承BaseApiController 即注入 可以的话试试private
	@Autowired
	protected HttpServletRequest request;
	@Autowired
	protected S service;

	@ApiOperation("保存")
	@PostMapping("")
	public Result<E> save(@RequestBody E entity) {
		service.save(entity);
		return Result.ok(entity);
	}

	@ApiOperation("全量更新")
	@PutMapping("")
	public Result<Boolean> update(@RequestBody E entity) {
		return Result.ok(service.updateById(entity));
	}

	@ApiOperation("删除")
	@DeleteMapping("/{id}")
	public <ID extends Serializable> Result<Boolean> remove(@PathVariable ID id) {
		return Result.ok(service.removeById(id));
	}

	@ApiOperation("详情")
	@GetMapping("/{id}")
	public <ID extends Serializable> Result<E> load(@PathVariable ID id) {
		return Result.ok(service.getById(id));
	}

	@ApiOperation("全量列表")
	@GetMapping("/list")
	public Result<List<E>> find(E data) {
		return Result.ok(service.list(new QueryWrapper<E>(data)));
	}

	@ApiOperation("全量列表2")
	@GetMapping("/list")
	public Result<List<E>> find(QueryWrapper<E> data) {
		return Result.ok(service.list(data));
	}

	@Deprecated
	public Result<Page<E>> findPage(E entity) {
		return Result.ok(service.page(defaultPage(), new QueryWrapper<E>(entity)));
	}

	@ApiOperation("分页查询")
	@GetMapping("/page")
	public Result<Page<E>> findPage(Page<E> page, E entity) {
		return Result.ok(service.page(defaultPage(), new QueryWrapper<E>(entity)));
	}

	@ApiOperation("分页查询2")
	@GetMapping("/page")
	public Result<Page<E>> findPage(Page<E> page, QueryWrapper<E> data) {
		return Result.ok(service.page(defaultPage(), data));
	}

	@ApiOperation("批量更新、添加")
	@RequestMapping(path = "/batchUpdate", method = {RequestMethod.POST, RequestMethod.PUT})
	public Result<Boolean> batchUpdate(@RequestBody List<E> entities) {
		return Result.ok(service.saveOrUpdateBatch(entities));
	}

	@ApiOperation("批量删除")
	@DeleteMapping(path = "/batchRemove")
	public <ID extends Serializable> Result<Boolean> batchRemove(@RequestBody List<ID> entities) {
		return Result.ok(service.removeByIds(entities));
	}

	@ApiOperation("按id批量查")
	@GetMapping("/loadByKeys")
	public Result<List<E>> loadByPrimaryKeys(@RequestParam("ids") List<? extends Serializable> values) {
		return Result.ok(service.listByIds(values));
	}
}
