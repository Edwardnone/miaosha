package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.service.model.UserModel;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 18:56
 * @Version 1.0
 */

public interface UserService {

    /**
     * 通过用户id获取用户对象的方法
     * @param id
     */
    UserModel getUserById(Integer id);
}
