package com.miaoshaproject.miaosha.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miaoshaproject.miaosha.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/30 9:25
 * @Version 1.0
 */
@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String,Object> commonCache = null;

    @PostConstruct
    public void init(){
        this.commonCache = CacheBuilder.newBuilder()
                //设置缓存容器的初始容量为10
                .initialCapacity(10)
                //设置缓存中最大存储容量，超过按照LRU规则移除缓存项
                .maximumSize(100)
                //设置过期时间为1分钟
                .expireAfterWrite(1, TimeUnit.MINUTES).build();
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }
}
