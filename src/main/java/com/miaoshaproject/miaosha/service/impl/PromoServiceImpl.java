package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.ItemDOMapper;
import com.miaoshaproject.miaosha.dao.ItemStockDOMapper;
import com.miaoshaproject.miaosha.dao.PromoDOMapper;
import com.miaoshaproject.miaosha.dataobject.ItemDO;
import com.miaoshaproject.miaosha.dataobject.ItemStockDO;
import com.miaoshaproject.miaosha.dataobject.PromoDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.PromoService;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.service.model.PromoModel;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.apache.catalina.User;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/21 15:23
 * @Version 1.0
 */
@Service
public class PromoServiceImpl implements PromoService {

    private PromoDOMapper promoDOMapper;

    @Resource
    private ItemDOMapper itemDOMapper;

    @Resource
    private ItemStockDOMapper itemStockDOMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

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
            //?????????
            promoModel.setPromoStatus(0);
        } else{
            if (DateTime.now().isBefore(promoModel.getStartTime())){
                //?????????
                promoModel.setPromoStatus(1);
            }else{
                //????????????
                promoModel.setPromoStatus(2);
            }
        }
        return promoModel;
    }

    @Override
    public void promoPublish(Integer promoId) {
        //????????????id????????????id
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if (promoDO == null || promoDO.getItemId().intValue() == 0){
            return;
        }
        ItemModel itemModel = itemService.getItemByIdInCache(promoDO.getItemId());
        if (itemModel != null){
            //???????????????????????????
            redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
        }
        //????????????????????????????????????redis
        redisTemplate.opsForValue().set("promo_door_count_" + promoDO.getId(), itemModel.getStock() * 5);
    }

    //??????????????????, itemId??????????????????
    @Override
    public String generateToken(Integer promoId, Integer userId, Integer itemId) {
        //??????????????????????????????
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)){
            //???????????????????????????????????????
            return null;
        }
        //?????????????????????count??????
        Long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1);
        if (result < 0){
            return null;
        }
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        PromoModel promoModel = convertFromDataObject(promoDO);
        if (promoModel == null) {
            return null;
        }
        if ( DateTime.now().isAfter(promoModel.getEndTime())){
            //?????????
            promoModel.setPromoStatus(0);
        } else{
            if (DateTime.now().isBefore(promoModel.getStartTime())){
                //?????????
                promoModel.setPromoStatus(1);
            }else{
                //????????????
                promoModel.setPromoStatus(2);
            }
        }
        //??????????????????????????????
        if (promoModel.getPromoStatus() != 2){
            return null;
        }
        //????????????Id????????????
        UserModel userModel = userService.getUserByIdInCache(userId);
        if (userModel == null){
            return null;
        }
        //????????????Id????????????
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            return null;
        }
        //????????????Id????????????
        if (promoId.intValue() != itemModel.getPromoModel().getId()){
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("promo_" + promoDO.getId() + "_item_" + itemId + "_user_" + userId + "_token_", token);
        redisTemplate.expire("promo_" + promoDO.getId() + "_item_" + itemId + "_user_" + userId + "_token_", 5, TimeUnit.MINUTES);
        return token;
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
