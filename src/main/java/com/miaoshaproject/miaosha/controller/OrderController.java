package com.miaoshaproject.miaosha.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/20 16:35
 * @Version 1.0
 */
@Controller("orderController")
@RequestMapping("/order")
public class OrderController extends BaseController {

    private OrderService orderService;

    private HttpServletRequest request;

    public OrderController(OrderService orderService, HttpServletRequest request) {
        this.orderService = orderService;
        this.request = request;
    }


    @RequestMapping("/createOrder")
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        //用户登录验证
        Object isLogin = request.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !(Boolean)isLogin){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN_ERROR);
        }
        UserModel userModel = (UserModel) request.getSession().getAttribute("LOGIN_USER");
        orderService.createOrder(userModel.getId(), itemId, amount);

        return CommonReturnType.create(null);
    }
}
