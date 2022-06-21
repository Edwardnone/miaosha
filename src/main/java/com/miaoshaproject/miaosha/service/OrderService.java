package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.error.BusinessException;

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
     * @param amount
     * @throws BusinessException
     */
    public void createOrder(Integer userId, Integer itemId, Integer amount, BigDecimal promoItemPrice) throws BusinessException;
}
