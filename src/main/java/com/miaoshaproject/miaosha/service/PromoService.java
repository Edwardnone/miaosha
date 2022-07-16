package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.service.model.PromoModel;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/21 15:20
 * @Version 1.0
 */
public interface PromoService {

    /**
     * 通过商品id获取将要发生或正在发生的商品秒杀信息
     * @param itemId
     * @return
     */
    public PromoModel getPromoByItemId(Integer itemId);
    /**
     * 发布活动，将活动商品库存存入redis缓存
     * @param promoId
     */
    public void promoPublish(Integer promoId);

    /**
     * 生成秒杀令牌
     * @param promoId
     * @param userId
     * @param itemId
     * @return
     */
    public String generateToken(Integer promoId, Integer userId, Integer itemId);
}
