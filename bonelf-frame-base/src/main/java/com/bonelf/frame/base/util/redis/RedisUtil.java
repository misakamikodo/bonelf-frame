package com.bonelf.frame.base.util.redis;

import cn.hutool.core.collection.CollectionUtil;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 * 熟练的话直接使用redisTemplate即可，这种封装可用于复杂的execution
 *
 * 方法，参考以下分界线方法
 * {@link this#expire(String, long)} Common 获取键值对的信息
 * {@link this#get(String)} String 简单redis键值对存储信息
 * {@link this#hget(String, String)}  Map 可根据id来缓存每个单位的数据
 * {@link this#sGet(String)} Set 唯一性
 * {@link this#lGet(String, long, long)}  List 存储列表数据并管理 (建议保存索引值方便操作)
 * {@link this#zAdd(String, String, double)}   zSet 按照点击量排序等使用的缓存技术
 * {@link this#gAdd(String, String, double, double)}   GEO 经纬度计算、按经纬度排序分页
 *
 * redis还有其他应用 如
 * 发布订阅：见test模块websocket RedisSubscriptionConfig；(opsForCluster)
 * 锁：RedisLock
 * @author bonelf
 **/
@Slf4j
@Component
public class RedisUtil {
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	/**
	 * 指定缓存失效时间
	 * @param key 键
	 * @param time 时间(秒)
	 * @return boolean
	 */
	public boolean expire(String key, long time) {
		try {
			if (time > 0) {
				redisTemplate.expire(key, time, TimeUnit.SECONDS);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 根据key 获取过期时间
	 * @param key 键 不能为null
	 * @return 时间(秒) 返回0代表为永久有效
	 */
	public Long getExpire(String key) {
		return redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	/**
	 * 判断key是否存在
	 * @param key 键
	 * @return true 存在 false不存在
	 */
	public Boolean hasKey(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除缓存
	 * @param key 可以传一个值 或多个
	 */
	public void del(String... key) {
		if (key != null && key.length > 0) {
			if (key.length == 1) {
				redisTemplate.delete(key[0]);
			} else {
				redisTemplate.delete(CollectionUtil.toList(key));
			}
		}
	}

	/*============================String=============================*/

	/**
	 * 普通缓存获取
	 * @param key 键
	 * @return 值
	 */
	public Object get(String key) {
		return key == null ? null : redisTemplate.opsForValue().get(key);
	}

	/**
	 * 普通缓存放入
	 * @param key 键
	 * @param value 值
	 * @return true成功 false失败
	 */
	public boolean set(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 普通缓存放入并设置时间
	 * @param key 键
	 * @param value 值
	 * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
	 * @return true成功 false 失败
	 */
	public boolean set(String key, Object value, long time) {
		try {
			if (time > 0) {
				redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
			} else {
				set(key, value);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 递增
	 * @param key 键
	 * @param delta 要增加几(大于0)
	 */
	public Long incr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递增因子必须大于0");
		}
		return redisTemplate.opsForValue().increment(key, delta);
	}

	/**
	 * 递减
	 * @param key 键
	 * @param delta 要减少几(小于0)
	 */
	public Long decr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递减因子必须大于0");
		}
		return redisTemplate.opsForValue().increment(key, -delta);
	}


	/*================================Map=================================*/

	/**
	 * HashGet
	 * @param key 键 不能为null
	 * @param item 项 不能为null
	 * @return 值
	 */
	public <MV> MV hget(String key, String item) {
		return redisTemplate.<String, MV>opsForHash().get(key, item);
	}

	/**
	 * 获取hashKey对应的所有键值
	 * @param key 键
	 * @return 对应的多个键值
	 */
	public <MK, MV> Map<MK, MV> hmget(String key) {
		return redisTemplate.<MK, MV>opsForHash().entries(key);
	}

	/**
	 * HashSet
	 * @param key 键
	 * @param map 对应多个键值
	 * @return true 成功 false 失败
	 */
	public <MV> boolean hmset(String key, Map<String, MV> map) {
		try {
			redisTemplate.<String, MV>opsForHash().putAll(key, map);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * HashSet 并设置时间
	 * @param key 键
	 * @param map 对应多个键值
	 * @param time 时间(秒)
	 * @return true成功 false失败
	 */
	public <MV> boolean hmset(String key, Map<String, MV> map, long time) {
		try {
			redisTemplate.<String, MV>opsForHash().putAll(key, map);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 * @param key 键
	 * @param item 项
	 * @param value 值
	 * @return true 成功 false失败
	 */
	public <MV> boolean hset(String key, String item, MV value) {
		try {
			redisTemplate.<String, MV>opsForHash().put(key, item, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 * @param key 键
	 * @param item 项
	 * @param value 值
	 * @param time 时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
	 * @return true 成功 false失败
	 */
	public <MV> boolean hset(String key, String item, MV value, long time) {
		try {
			redisTemplate.<String, MV>opsForHash().put(key, item, value);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除hash表中的值
	 * @param key 键 不能为null
	 * @param item 项 可以使多个 不能为null
	 */
	public <MV> long hdel(String key, MV... item) {
		return redisTemplate.<String, MV>opsForHash().delete(key, (Object[])item);
	}

	/**
	 * 判断hash表中是否有该项的值
	 * @param key 键 不能为null
	 * @param item 项 不能为null
	 * @return true 存在 false不存在
	 */
	public boolean hHasKey(String key, String item) {
		return redisTemplate.opsForHash().hasKey(key, item);
	}

	/**
	 * hash递增 如果不存在,就会创建一个 并把新增后的值返回
	 * @param key 键
	 * @param item 项
	 * @param by 要增加几(大于0)
	 */
	public double hincr(String key, String item, double by) {
		return redisTemplate.opsForHash().increment(key, item, by);
	}

	/**
	 * hash递减
	 * @param key 键
	 * @param item 项
	 * @param by 要减少记(小于0)
	 */
	public double hdecr(String key, String item, double by) {
		return redisTemplate.opsForHash().increment(key, item, -by);
	}


	/**
	 * Hash key
	 * @param key
	 * @return
	 */
	public <MK> Set<MK> hKeys(String key) {
		return redisTemplate.<MK, Object>opsForHash().keys(key);
	}
	/*============================set=============================*/

	/**
	 * 根据key获取Set中的所有值
	 * @param key 键
	 */
	public Set<Object> sGet(String key) {
		try {
			return redisTemplate.opsForSet().members(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据value从一个set中查询,是否存在
	 * @param key 键
	 * @param value 值
	 * @return true 存在 false不存在
	 */
	public <SV> Boolean sHasKey(String key, SV value) {
		try {
			return redisTemplate.opsForSet().isMember(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将数据放入set缓存
	 * @param key 键
	 * @param values 值 可以是多个
	 * @return 成功个数
	 */
	public Long sSet(String key, Object... values) {
		try {
			return redisTemplate.opsForSet().add(key, values);
		} catch (Exception e) {
			e.printStackTrace();
			return -1L;
		}
	}

	/**
	 * 将set数据放入缓存
	 * @param key 键
	 * @param time 时间(秒)
	 * @param values 值 可以是多个
	 * @return 成功个数
	 */
	public Long sSetAndTime(String key, long time, Object... values) {
		try {
			Long count = redisTemplate.opsForSet().add(key, values);
			if (time > 0) {
				expire(key, time);
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	/**
	 * 获取set缓存的长度
	 * @param key 键
	 */
	public Long sGetSetSize(String key) {
		try {
			return redisTemplate.opsForSet().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return -1L;
		}
	}

	/**
	 * 移除值为value的
	 * @param key 键
	 * @param values 值 可以是多个
	 * @return 移除的个数
	 */
	public <SV> Long setRemove(String key, SV... values) {
		try {
			return redisTemplate.opsForSet().remove(key, (Object[])values);
		} catch (Exception e) {
			e.printStackTrace();
			return -1L;
		}
	}

	/*===============================list=================================*/

	/**
	 * 获取list缓存的内容
	 * @param key 键
	 * @param start 开始
	 * @param end 结束  0 到 -1代表所有值
	 */
	public List<Object> lGet(String key, long start, long end) {
		try {
			return redisTemplate.opsForList().range(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Object> lGetAll(String key) {
		return this.lGet(key, 0, -1);
	}

	/**
	 * 获取list缓存的长度
	 * @param key 键
	 */
	public Long lGetListSize(String key) {
		try {
			return redisTemplate.opsForList().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return -1L;
		}
	}

	/**
	 * 通过索引 获取list中的值
	 * @param key 键
	 * @param index 索引  index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
	 */
	public Object lGetIndex(String key, long index) {
		try {
			return redisTemplate.opsForList().index(key, index);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将list放入缓存
	 * @param key 键
	 * @param value 值
	 * @return boolean
	 */
	public boolean lSet(String key, Object value) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将list放入缓存
	 * @param key 键
	 * @param value 值
	 * @param time 时间(秒)
	 */
	public boolean lSet(String key, Object value, long time) {
		try {
			redisTemplate.opsForList().rightPush(key, value);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将list放入缓存
	 * @param key 键
	 * @param value 值
	 */
	public boolean lSet(String key, List<Object> value) {
		try {
			redisTemplate.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将list放入缓存
	 * @param key 键
	 * @param value 值
	 * @param time 时间(秒)
	 */
	public boolean lSet(String key, List<Object> value, long time) {
		try {
			redisTemplate.opsForList().rightPushAll(key, value);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 根据索引修改list中的某条数据
	 * @param key 键
	 * @param index 索引
	 * @param value 值
	 */
	public boolean lUpdateIndex(String key, long index, Object value) {
		try {
			redisTemplate.opsForList().set(key, index, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 移除N个值为value
	 * @param key 键
	 * @param count 移除多少个
	 * @param value 值
	 * @return 移除的个数
	 */
	public Long lRemove(String key, long count, Object value) {
		try {
			return redisTemplate.opsForList().remove(key, count, value);
		} catch (Exception e) {
			e.printStackTrace();
			return -1L;
		}
	}

	/**
	 * 读取数据
	 * @param listKey key
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return java.util.List
	 */
	public List<Object> rangeList(String listKey, long start, long end) {
		//绑定操作
		BoundListOperations<Object, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
		//查询数据
		return boundValueOperations.range(start, end);
	}


	/*===============================ZSet=================================*/

	/**
	 * 添加一个元素, zset与set最大的区别就是每个元素都有一个score，因此有个排序的辅助功能;  zadd
	 * @param key 键
	 * @param value 存储对象键值 如果需要为对象 可以改为对象，一般只存键值再根据键值从库中获取数据
	 * @param score 排序值
	 */
	public void zAdd(String key, String value, double score) {
		redisTemplate.opsForZSet().add(key, value, score);
	}

	/**
	 * 删除元素 zrem
	 * @param key
	 * @param value
	 */
	public void zRemove(String key, String value) {
		redisTemplate.opsForZSet().remove(key, value);
	}

	/**
	 * score的增加or减少 zincrby
	 * @param key
	 * @param value
	 * @param score
	 */
	public Double zIncrScore(String key, String value, double score) {
		return redisTemplate.opsForZSet().incrementScore(key, value, score);
	}

	/**
	 * 查询value对应的score   zscore
	 * @param key
	 * @param value
	 * @return
	 */
	public Double zScore(String key, String value) {
		return redisTemplate.opsForZSet().score(key, value);
	}

	/**
	 * 判断value在zset中的排名  zrank ( 升序)
	 * @param key
	 * @param value
	 * @return
	 */
	public Long zRank(String key, String value) {
		Long rank = redisTemplate.opsForZSet().rank(key, value);
		//idx：0 开始 但是习惯1开始 所以加一
		return rank == null ? null : rank + 1L;
	}

	/**
	 * 返回集合的长度
	 * @param key
	 * @return
	 */
	public Long size(String key) {
		return redisTemplate.opsForZSet().zCard(key);
	}

	/**
	 * 查询集合中指定顺序的值， 0 -1 表示获取全部的集合内容  zrange
	 * 返回有序的集合，score小的在前面
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Object> range(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(key, start, end);
	}

	/**
	 * 查询集合中指定顺序的值和score，0, -1 表示获取全部的集合内容
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<ZSetOperations.TypedTuple<Object>> rangeWithScores(String key, long start, long end) {
		return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
	}

	/**
	 * 查询集合中指定顺序的值  zrevrange
	 * 返回有序的集合中，score大的在前面
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Object> zRevRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().reverseRange(key, start, end);
	}

	/**
	 * 根据score的值，来获取满足条件的集合  zrangebyscore
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<Object> zSortRange(String key, long min, long max) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}


	/*===============================GEO=================================*/

	/**
	 * 位置添加
	 * @param key
	 * @param name 名称
	 * @param lng
	 * @param lat
	 * @return
	 */
	public Long gAdd(String key, String name, double lng, double lat) {
		return redisTemplate.opsForGeo().add(key, new Point(lng, lat), name);
	}

	/**
	 * 位置添加
	 * @param key
	 * @param name 名称
	 * @return
	 */
	public Long gDel(String key, String... name) {
		return redisTemplate.opsForGeo().remove(key, name);
	}


	/***
	 * 分页获取最近附近的人地理位置
	 * storedist() 函数的意义:使用距离排序
	 * 请在外面通过hasKey判断缓存结果，
	 * 还有通过修改距离控制数量调用redisTemplate.opsForGeo().radius
	 * XXX 看上去临时Key这种做法可优化
	 * @param page 第几页
	 * @param limit 每页条数
	 * @param key redis key "city",
	 //* @param name 位置名称 "深圳",
	 * @param lat 纬度,
	 * @param lng 经度,
	 * @param distance 距离 "8000",
	 * 以下可设置默认值
	 * @param distanceUnit 距离单位 "km",
	 * @param sort 排序 "asc",
	 * @param nearDataKey 新的redis key "shenzhennewkey"
	 * @throws RedisCommandExecutionException ERR could not decode requested zset member ：元素不存在 检查 name
	 */
	public Set<ZSetOperations.TypedTuple<Object>> gPage(int page, int limit,
														String key, double lat, double lng,
														double distance, Metrics distanceUnit,
														String sort, String nearDataKey) throws RedisCommandExecutionException {
		//将附近的人存储到一个key里
		//'bonelf:shopGeo' 'shopName1' '1000' 'km' 'asc' 'storedist' 'shopName1SearBy'
		Object execute = execute("return redis.call('georadius',KEYS[1],KEYS[2],KEYS[3],KEYS[4],KEYS[5],KEYS[6],'storedist',KEYS[7])",
				key, String.valueOf(lng), String.valueOf(lat), String.valueOf(distance), distanceUnit.getAbbreviation(), sort, nearDataKey);
		//RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
		//redisTemplate.opsForGeo().radius(key, new Circle(new Point(lat, lng), new Distance(distance, distanceUnit)), "asc".equals(sort) ? geoRadiusCommandArgs.sortAscending() : geoRadiusCommandArgs.sortDescending()));
		//给新key设置失效时间 反正这里需要删除
		redisTemplate.expire(nearDataKey, 1, TimeUnit.HOURS);
		//开始条数
		int startPage = (page - 1) * limit;
		//结束条数
		int endPage = page * limit - 1;
		//获取分页信息
		Set<ZSetOperations.TypedTuple<Object>> result = rangeWithScores(nearDataKey, startPage, endPage);
		// 删除临时Key
		del(nearDataKey);
		return result;
	}
	public Set<ZSetOperations.TypedTuple<Object>> gPage(int page, int limit,
														String key, String id,
														double distance, Metrics distanceUnit,
														String sort, String nearDataKey, long nearDataKeyExpireHours) throws RedisCommandExecutionException {
		//将附近的人存储到一个key里
		//'bonelf:shopGeo' 'shopName1' '1000' 'km' 'asc' 'storedist' 'shopName1SearBy'
		Object execute = execute("return redis.call('georadiusbymember',KEYS[1],KEYS[2],KEYS[3],KEYS[4],KEYS[5],KEYS[6],'storedist',KEYS[7])",
				key, id, String.valueOf(distance), distanceUnit.getAbbreviation(), sort, nearDataKey);
		//RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
		//redisTemplate.opsForGeo().radius(key, new Circle(new Point(lat, lng), new Distance(distance, distanceUnit)), "asc".equals(sort) ? geoRadiusCommandArgs.sortAscending() : geoRadiusCommandArgs.sortDescending()));
		//给新key设置失效时间
		redisTemplate.expire(nearDataKey, nearDataKeyExpireHours, TimeUnit.HOURS);
		//删除自己
		redisTemplate.opsForGeo().remove(nearDataKey, id);
		//开始条数
		int startPage = (page - 1) * limit;
		//结束条数
		int endPage = page * limit - 1;
		//获取分页信息
		Set<ZSetOperations.TypedTuple<Object>> result = rangeWithScores(nearDataKey, startPage, endPage);
		//如果是通过点位id(比如门店经纬度：固定经纬度)则缓存一段时间(6Hour，建议外部传递常量)，如果其他点位变动小时间长，否则短，可考虑使用@Cacheable
		return result;
	}


	/**
	 * page:页数 limit:第一次的半径->每次增加 limit²π的面积
	 * 分页可以把距离（半径）改为Math.pow(page, -2)*limit返回所有结果；Math.pow(page-1, -2)*limit是上一页的
	 * range:Distance.between(new Distance(Math.pow(page, -2)*limit, distanceUnit), new Distance(Math.pow(page-1, -2)*limit, distanceUnit))
	 * 但是radius好像不能搜索一个圆环内的信息-_-
	 * 可以考虑MongoDB
	 * 记得排除自己
	 * @param key
	 * @param lat
	 * @param lng
	 * @param distance
	 * @param distanceUnit 单位
	 * @param sort "asc"/"desc"
	 * @return
	 * @throws RedisCommandExecutionException ERR could not decode requested zset member ：元素不存在 检查 name（如果radius是按名查）
	 */
	public GeoResults<RedisGeoCommands.GeoLocation<Object>> gRadius(String key, double lat, double lng,
																	double distance, Metrics distanceUnit,
																	String sort) throws RedisCommandExecutionException {
		RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
		return redisTemplate.opsForGeo().radius(key, new Circle(new Point(lat, lng), new Distance(distance, distanceUnit)), "asc".equals(sort) ? geoRadiusCommandArgs.sortAscending() : geoRadiusCommandArgs.sortDescending());
	}

	/**
	 * 计算距离
	 * @param key
	 * @param name1
	 * @param name2
	 * @return
	 */
	public Distance gDistance(String key, String name1, String name2) {
		return redisTemplate.opsForGeo().distance(key, name1, name2, RedisGeoCommands.DistanceUnit.METERS);
	}

	/**
	 * 执行lua脚本
	 * @param text lua 脚本
	 * @param str lua脚本的参数
	 */
	private Object execute(String text, String... str) {
		//参数处理
		List<Object> params = Arrays.asList(str);
		DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
		//设置返回值
		defaultRedisScript.setResultType(Long.class);
		//设置脚本
		defaultRedisScript.setScriptText(text);
		//执行命令
		return redisTemplate.execute(defaultRedisScript, params);
	}
}
