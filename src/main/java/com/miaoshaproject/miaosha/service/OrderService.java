package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.service.model.OrderModel;

import java.math.BigDecimal;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/20 15:21
 * @Version 1.0
 */
public interface OrderService {

    /**
     * 用户下单购买商品
     * @param userId
     * @param itemId
     * @param promoId
     * @param amount
     * @throws BusinessException
     * @return
     */
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException;
}
