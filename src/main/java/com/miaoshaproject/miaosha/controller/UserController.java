package com.miaoshaproject.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.miaosha.controller.viewobject.UserVO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 18:53
 * @Version 1.0
 */
@Controller
//@CrossOrigin(allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/user")
public class UserController{

    private UserService userService;

    private HttpServletRequest httpServletRequest;

    @Resource
    private RedisTemplate redisTemplate;


    public UserController(UserService userService, HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
    }

    @RequestMapping("/login")
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, NoSuchAlgorithmException {
        //判断参数是否为空
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        UserModel userModel = userService.verifyLogin(telphone, getEncrptPassword(password));
        //若用户登录成功，将登录凭证加入到用户登录成功的session内
        //httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
        //httpServletRequest.getSession().setAttribute("IS_LOGIN", true);

        //生成登录凭据（token）
        String token = UUID.randomUUID().toString().replace("-", "");
        //将登录凭据存入redis中
        redisTemplate.opsForValue().set(token, userModel, 1, TimeUnit.HOURS);
        return CommonReturnType.create(token);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") byte gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "password") String password) throws BusinessException, NoSuchAlgorithmException {
        //检查验证码是否正确
        String sessionOtpCode = (String)httpServletRequest.getSession().getAttribute(telphone);
        if (otpCode == null || !StringUtils.equals(sessionOtpCode, otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码错误");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender);
        userModel.setEncrptPassword(getEncrptPassword(password));
        userModel.setTelphone(telphone);
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    private String getEncrptPassword(String password) throws NoSuchAlgorithmException {
        //加密方式
        MessageDigest md5 = MessageDigest.getInstance("md5");
        //BASE64Encoder base64Encoder = new BASE64Encoder();
        byte[] bytes = Base64.encodeBase64(md5.digest(password.getBytes(StandardCharsets.UTF_8)));
        return new String(bytes);
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
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST_ERROR);
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
