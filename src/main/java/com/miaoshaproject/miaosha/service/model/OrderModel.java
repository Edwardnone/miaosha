package com.miaoshaproject.miaosha.service.model;

import java.math.BigDecimal;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/19 22:11
 * @Version 1.0
 */
public class OrderModel {
    /**
     * 订单号：8位时间 + 6位序列号 + 2位分库分表号
     */
    public String id;

    public Integer userId;

    public Integer itemId;

    /**
     * 商品下单数量
     */
    public Integer amount;

    /**
     * 订单金额
     */
    public BigDecimal orderAmount;
}
