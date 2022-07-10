package com.miaoshaproject.miaosha.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.mq.MqProducer;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/20 16:35
 * @Version 1.0
 */
@Controller("orderController")
@RequestMapping("/order")
public class OrderController{

    private OrderService orderService;

    private HttpServletRequest httpServletRequest;

    @Resource
    private ItemService itemService;

    @Resource
    private MqProducer mqProducer;

    @Resource
    private RedisTemplate redisTemplate;

    public OrderController(OrderService orderService, HttpServletRequest httpServletRequest) {
        this.orderService = orderService;
        this.httpServletRequest = httpServletRequest;
    }


    @RequestMapping("/createOrder")
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoItemPrice", required=false)BigDecimal promoItemPrice) throws BusinessException {

        //用户登录验证
        //Object isLogin = request.getSession().getAttribute("IS_LOGIN");
        //if (isLogin == null || !(Boolean)isLogin){
        //    throw new BusinessException(EmBusinessError.USER_NOT_LOGIN_ERROR);
        //}
        //UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        //String paramArray = httpServletRequest.getParameterMap().get("token")[0];
        String[] tokenArray = httpServletRequest.getParameterMap().get("token");
        if (tokenArray == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "缺少token参数");
        }
        String token = tokenArray[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN_ERROR);
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN_ERROR);
        }


        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)){
            //若库存售罄，不进行后续操作
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        }
        //加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemId, amount);

        if (!mqProducer.asyncTransactionReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId)){
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        //orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        return CommonReturnType.create(null);
    }
}
