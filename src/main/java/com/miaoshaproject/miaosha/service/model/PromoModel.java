package com.miaoshaproject.miaosha.service.model;

import com.fasterxml.jackson.datatype.joda.deser.DateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/21 14:41
 * @Version 1.0
 */
public class PromoModel implements Serializable {
    private Integer id;
    /**
     * 秒杀活动名称
     */
    private String promoName;
    /**
     * 秒杀状态：0表示已过期，1表示活动未开始，2表示活动正在进行中
     */
    private Integer promoStatus;
    /**
     * 秒杀的商品id
     */
    private Integer itemId;
    /**
     * 秒杀的商品价格
     */
    private BigDecimal promoItemPrice;

    /**
     * 秒杀开始时间
     */
    private DateTime startTime;

    /**
     * 秒杀结束时间
     */
    private DateTime endTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPromoStatus() {
        return promoStatus;
    }

    public void setPromoStatus(Integer promoStatus) {
        this.promoStatus = promoStatus;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }
}
