package com.miaoshaproject.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.miaosha.controller.viewobject.UserVO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 18:53
 * @Version 1.0
 */
@Controller
@CrossOrigin
@RequestMapping("/user")
public class UserController extends BaseController{

    private UserService userService;

    private HttpServletRequest httpServletRequest;

    public UserController(UserService userService, HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") byte gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "password") String password) throws BusinessException {
        //检查验证码是否正确
        if (otpCode == null || !StringUtils.equals((String)httpServletRequest.getSession().getAttribute(telphone), otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码错误");
        }
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender);
        userModel.setEncrptPassword(MD5Encoder.encode(password.getBytes()));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }


    @RequestMapping(value = "/getotp", method = RequestMethod.POST)
    @ResponseBody
    public CommonReturnType getOpt(@RequestParam(name = "telphone") String telphone){
        //生成opt验证码
        Random random = new Random();
        int code = random.nextInt(899999);
        String strCode = String.valueOf(100000 + code);
        //将opt验证码与手机号绑定(企业中使用redis，这里使用httpSession）
        httpServletRequest.getSession().setAttribute(telphone, strCode);
        //将验证码发送至用户手机（这里打印出来）
        System.out.println("code=" + strCode + ", telphone=" + telphone);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //userModel = null;
        //userModel.setEncrptPassword("123");
        //若获取的对应用户信息不存在
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
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
