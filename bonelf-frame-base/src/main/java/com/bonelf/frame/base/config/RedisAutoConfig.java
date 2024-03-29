/**
 * Copyright 2018-2020 stylefeng & fengshuonan (sn93@qq.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bonelf.frame.base.config;

import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.jackson.RestObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.util.*;

/**
 * redis配置
 * RedisConnectionFactory 报找不到bean警告实际上可行~
 * @author bonelf
 */
@ConditionalOnProperty(prefix = "bonelf.cache", value = "strategy", havingValue = "redis", matchIfMissing = true)
@EnableCaching
@Configuration
@ConditionalOnClass({RedisTemplate.class})
public class RedisAutoConfig extends CachingConfigurerSupport {

	/**
	 * 默认键值生成策略 Cacheable不指定key时
	 * (value)::(full ClassName)-(methodName)
	 */
	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder redisKey = new StringBuilder();
				// 这个类名很长
				redisKey.append(target.getClass().getName()).append("#");
				redisKey.append(method.getName());
				if (params.length > 0) {
					redisKey.append("-").append(Arrays.deepToString(params));
				}
				return redisKey.toString();
			}
		};
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		// 生成一个默认配置，通过config对象即可对缓存进行自定义配置
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 设置缓存的默认过期时间，也是使用Duration设置
		// 过期时间5分钟
		config = config.entryTtl(CommonCacheConstant.CACHE_NAME_5_MINUTES_TIME);

		// 设置一个初始化的缓存空间set集合
		Set<String> cacheNames = new HashSet<>();
		cacheNames.add(CommonCacheConstant.CACHE_NAME_5_MINUTES);
		cacheNames.add(CommonCacheConstant.CACHE_NAME_7_DAY);

		// 对每个缓存空间应用不同的配置
		Map<String, RedisCacheConfiguration> configMap = new HashMap<>(10);
		configMap.put(CommonCacheConstant.CACHE_NAME_5_MINUTES, config);
		// 源码每次都是new 个config 所以不创建新config
		configMap.put(CommonCacheConstant.CACHE_NAME_7_DAY, config.entryTtl(CommonCacheConstant.CACHE_NAME_7_DAY_TIME));

		// 使用自定义的缓存配置初始化一个cacheManager
		return RedisCacheManager.builder(factory)
				// 注意这两句的调用顺序，一定要先调用该方法设置初始化的缓存名，再初始化相关的配置
				.initialCacheNames(cacheNames)
				.withInitialCacheConfigurations(configMap)
				.build();
	}

	/**
	 * XXX ObjectMapper 没有使用 @AutoWired ObjectMapper
	 * @return 序列化器
	 */
	@Bean
	public RedisSerializer<Object> jackson2JsonRedisSerializer() {
		Jackson2JsonRedisSerializer<Object> j = new Jackson2JsonRedisSerializer<>(Object.class);
		// ObjectMapper om = new ObjectMapper();
		// 不适用Bean，因为有小改会导致json参数接受异常
		ObjectMapper om = new RestObjectMapper();
		j.setObjectMapper(om);
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
				ObjectMapper.DefaultTyping.NON_FINAL);
		return j;
		//return new FastJson2JsonRedisSerializer<>(Object.class);
	}

	/**
	 * <p>
	 * 自定义redisTemplate Bean，防止hmget和hincr一起用的报错
	 * 重命名myRedisTemplate 使用 Primary指定优先级，是哪个gateway不知原因的redisTemplate已被定义错误（明明spring中已经使用@ConditionalOnMissingBean）
	 * </p>
	 * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
	 * @author bonelf
	 * @since 2020/8/31 16:54
	 */
	@Bean("myRedisTemplate")
	@Primary
	// @ConditionalOnMissingBean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory, RedisSerializer<?> redisSerializer) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		//键值对
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(redisSerializer);
		//hash值 不配置使用hincr后使用hmget获取值报错
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(redisSerializer);
		template.afterPropertiesSet();

		return template;
	}
}
