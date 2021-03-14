package com.bonelf.support.feign.factory;

import com.bonelf.support.feign.SupportFeignClient;
import com.bonelf.support.feign.fallback.SupportFeignFallback;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class SupportFeignFallbackFactory implements FallbackFactory<SupportFeignClient> {

    @Override
    public SupportFeignClient create(Throwable throwable) {
        // 处理throwable
        // 集成了SupportFeignClient的fallback
        SupportFeignFallback fallback = new SupportFeignFallback();
        fallback.setCause(throwable);
        return fallback;
    }
}