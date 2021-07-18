package com.bonelf.frame.cloud.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.GlobalTransactionScanner;
import io.seata.spring.boot.autoconfigure.properties.SeataProperties;
import io.seata.spring.boot.autoconfigure.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Seata整合mybatis/mybatis-plus 配置
 * 不需要sqlSessionFactory
 * @author bonelf
 * @date 2021/7/8 10:49
 */
@Configuration
@ConditionalOnProperty(prefix = "seata", value = "enabled", havingValue = "true")
@EnableConfigurationProperties({SeataProperties.class})
public class SeataAutoConfig {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private SeataProperties seataProperties;
	@Autowired
	private SpringUtils springUtils;

	/**
	 * autowired datasource config
	 */
	@Autowired
	private DynamicDataSourceProperties dataSourceProperties;

	/**
	 * init durid datasource
	 * @Return: druidDataSource  datasource instance
	 */
	@Bean
	@Primary
	public DataSource druidDataSource() {
		DataSourceProperty dataSourceProperty = dataSourceProperties.getDatasource().get("master");
		DruidDataSource druidDataSource = new DruidDataSource();
		druidDataSource.setUrl(dataSourceProperty.getUrl());
		druidDataSource.setUsername(dataSourceProperty.getUsername());
		druidDataSource.setPassword(dataSourceProperty.getPassword());
		druidDataSource.setDriverClassName(dataSourceProperty.getDriverClassName());
		return new DataSourceProxy(druidDataSource);
	}

	/**
	 * init datasource proxy
	 * @Param: druidDataSource  datasource bean instance
	 * @Return: DataSourceProxy  datasource proxy
	 */
	// @Bean
	// @Primary
	// public DataSourceProxy dataSourceProxy(DruidDataSource druidDataSource) {
	// 	return new DataSourceProxy(druidDataSource);
	// }

	@Bean
	public DataSourceTransactionManager transactionManager(DataSource dataSourceProxy) {
		return new DataSourceTransactionManager(dataSourceProxy);
	}

	/**
	 * init mybatis sqlSessionFactory
	 * @Param: dataSourceProxy  datasource proxy
	 * @Return: DataSourceProxy  datasource proxy
	 */
	// @Bean
	// public MybatisSqlSessionFactoryBean sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception {
	// 	MybatisSqlSessionFactoryBean  factoryBean = new MybatisSqlSessionFactoryBean();
	// 	factoryBean.setDataSource(dataSourceProxy);
	// 	factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
	// 			.getResources("classpath*:/mapper/*.xml"));
	// 	factoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
	// 	return factoryBean;
	// }

	@ConditionalOnBean(SpringUtils.class)
	@Bean
	public GlobalTransactionScanner globalTransactionScanner() {
		if (SpringUtils.getApplicationContext() == null) {
			// 这个报空指针
			springUtils.setApplicationContext(applicationContext);
		}
		String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
		String txServiceGroup = seataProperties.getTxServiceGroup();
		if (StrUtil.isBlank(txServiceGroup)) {
			txServiceGroup = applicationName + "-seata-service-group";
			seataProperties.setTxServiceGroup(txServiceGroup);
		}
		return new GlobalTransactionScanner(applicationName, txServiceGroup);
	}
}
