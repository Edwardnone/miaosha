package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.UserDOMapper;
import com.miaoshaproject.miaosha.dao.UserPasswordDOMapper;
import com.miaoshaproject.miaosha.dataobject.UserDO;
import com.miaoshaproject.miaosha.dataobject.UserPasswordDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import com.miaoshaproject.miaosha.validator.ValidationImpl;
import com.miaoshaproject.miaosha.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private ValidationImpl validationImpl;

    public UserServiceImpl(UserDOMapper userDOMapper, UserPasswordDOMapper userPasswordDOMapper, ValidationImpl validationImpl) {
        this.userDOMapper = userDOMapper;
        this.userPasswordDOMapper = userPasswordDOMapper;
        this.validationImpl = validationImpl;
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



    @Override
    @Transactional(rollbackFor = java.lang.Exception.class)
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //if(StringUtils.isEmpty(userModel.getName()) ||
        //    StringUtils.isEmpty(userModel.getEncrptPassword()) ||
        //    StringUtils.isEmpty(userModel.getTelphone()) ||
        //    userModel.getAge() == null ||
        //    userModel.getGender() == 0) {
        //    throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        //}
        ValidationResult validationResult = validationImpl.validate(userModel);
        if (validationResult.getErrMsgMap().size() > 0){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }
        //插入一条用户记录
        UserDO userDO = convertFromUserModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException e){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号已注册");
        }

        userModel.setId(userDO.getId());
        //插入一条密码记录
        UserPasswordDO userPasswordDO = convertPasswordFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    @Override
    public UserModel verifyLogin(String telphone, String encryptPassword) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null){
            throw new BusinessException(EmBusinessError.USER_TELPHONE_OR_PASSWORD_INCORRECT_ERROR);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        if (!StringUtils.equals((CharSequence) encryptPassword, (CharSequence) userPasswordDO.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_TELPHONE_OR_PASSWORD_INCORRECT_ERROR);
        }
        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);
        return userModel;
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

    private UserDO convertFromUserModel(UserModel userModel){
        if (userModel == null){
            return null;
        }

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    private UserPasswordDO convertPasswordFromUserModel(UserModel userModel){
        if (userModel == null){
            return null;
        }

        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setUserId(userModel.getId());
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        return userPasswordDO;
    }
}
