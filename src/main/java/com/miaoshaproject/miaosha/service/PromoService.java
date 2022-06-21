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
}
