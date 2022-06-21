package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.PromoDOMapper;
import com.miaoshaproject.miaosha.dataobject.PromoDO;
import com.miaoshaproject.miaosha.service.PromoService;
import com.miaoshaproject.miaosha.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/21 15:23
 * @Version 1.0
 */
@Service
public class PromoServiceImpl implements PromoService {

    private PromoDOMapper promoDOMapper;

    public PromoServiceImpl(PromoDOMapper promoDOMapper) {
        this.promoDOMapper = promoDOMapper;
    }

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = convertFromDataObject(promoDO);
        if (promoModel == null) {
            return null;
        }
        if ( DateTime.now().isAfter(promoModel.getEndTime())){
            //已过期
            promoModel.setPromoStatus(0);
        } else{
            if (DateTime.now().isBefore(promoModel.getStartTime())){
                //未开始
                promoModel.setPromoStatus(1);
            }else{
                promoModel.setPromoStatus(2);
            }
        }
        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if (promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setStartTime(new DateTime(promoDO.getStartTime()));
        promoModel.setEndTime(new DateTime(promoDO.getEndTime()));
        return promoModel;
    }
}
