package com.miaoshaproject.miaosha.service;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/30 9:23
 * @Version 1.0
 */
public interface CacheService {

    //取本地缓存
    public Object getFromCommonCache(String key);

    //存本地缓存
    public void setCommonCache(String key, Object value);
}
