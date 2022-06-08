package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.UserDOMapper;
import com.miaoshaproject.miaosha.dao.UserPasswordDOMapper;
import com.miaoshaproject.miaosha.dataobject.UserDO;
import com.miaoshaproject.miaosha.dataobject.UserPasswordDO;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 18:56
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    private UserDOMapper userDOMapper;

    private UserPasswordDOMapper userPasswordDOMapper;


    public UserServiceImpl(UserDOMapper userDOMapper, UserPasswordDOMapper userPasswordDOMapper) {
        this.userDOMapper = userDOMapper;
        this.userPasswordDOMapper = userPasswordDOMapper;
    }

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取对应用户dataobject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);

        if(userDO == null){
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO, userPasswordDO);


    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null){
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
