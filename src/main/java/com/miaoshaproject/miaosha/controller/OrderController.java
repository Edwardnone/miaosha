package com.miaoshaproject.miaosha.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.mq.MqProducer;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.PromoService;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.*;

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

    @Autowired
    private PromoService promoService;

    public OrderController(OrderService orderService, HttpServletRequest httpServletRequest) {
        this.orderService = orderService;
        this.httpServletRequest = httpServletRequest;
    }

    private ExecutorService executorService;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);

    }

    @RequestMapping("/createOrder")
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken") String promoToken) throws BusinessException {

        //用户登录验证
        //Object isLogin = request.getSession().getAttribute("IS_LOGIN");
        //if (isLogin == null || !(Boolean)isLogin){
        //    throw new BusinessException(EmBusinessError.USER_NOT_LOGIN_ERROR);
        //}
        //UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        //String paramArray = httpServletRequest.getParameterMap().get("token")[0];
        //验证用户token
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
        //验证秒杀令牌
        boolean validateResult = validateToken(promoToken, promoId, userModel.getId(), itemId);
        if (!validateResult){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "缺少秒杀令牌");
        }

        //同步调用线程池的submit方法
        //拥塞窗口为20的等待队列，用来队列滑泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                //加入库存流水init状态
                String stockLogId = itemService.initStockLog(itemId, amount);

                if (!mqProducer.asyncTransactionReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId)){
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }


        //orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/generatePromoToken")
    @ResponseBody
    public CommonReturnType generatePromoToken(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId") Integer promoId) throws BusinessException {
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
        String promoToken = promoService.generateToken(promoId, userModel.getId(), itemId);
        return CommonReturnType.create(promoToken);
    }


    private boolean validateToken(String promoToken, Integer promoId, Integer userId, Integer itemId){
        if (promoToken == null){
            return false;
        }
        if (promoToken.equals((String) redisTemplate.opsForValue().get("promo_" + promoId + "_item_" + itemId + "_user_" + userId + "_token_"))){
            return true;
        }
        return false;
    }
}
