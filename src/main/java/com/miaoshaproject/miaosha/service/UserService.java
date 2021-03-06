package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.dataobject.UserDO;
import com.miaoshaproject.miaosha.error.BusinessException;
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

    /**
     * 从redis缓存获取用户对象信息
     * @param id
     * @return
     */
    UserModel getUserByIdInCache(Integer id);

    /**
     * 用户注册
     * @param userModel
     * @return
     */
    void register(UserModel userModel) throws BusinessException;

    /**
     * 验证手机号为telphone的密码是否为password
     * @param telphone
     * @param password
     * @return
     */
    public UserModel verifyLogin(String telphone, String encrptPassword) throws BusinessException;
}
