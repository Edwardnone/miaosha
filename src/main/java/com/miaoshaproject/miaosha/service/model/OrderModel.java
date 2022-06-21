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
    private String id;

    private Integer userId;

    private Integer itemId;

    /**
     * 商品下单数量
     */
    private Integer amount;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }
}
