package com.miaoshaproject.miaosha.controller;

import com.miaoshaproject.miaosha.controller.viewobject.UserVO;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 18:53
 * @Version 1.0
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/get")
    @ResponseBody
    public UserVO getUser(@RequestParam(name = "id") Integer id){
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //将核心领域模型用户对象转化为可供UI使用的viewobject
        return convertFromModel(userModel);
    }

    private UserVO convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }else{
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userModel, userVO);
            return userVO;
        }
    }
}
