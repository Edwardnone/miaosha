package com.miaoshaproject.miaosha.controller;

import com.miaoshaproject.miaosha.controller.viewobject.ItemVO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.CacheService;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.PromoService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/17 10:56
 * @Version 1.0
 */
@Controller("item")
@RequestMapping("/item")
public class ItemController{

    private ItemService itemService;

    @Resource
    private RedisTemplate redisTemplate;

    private CacheService cacheService;

    @Resource
    private PromoService promoService;

    public ItemController(ItemService itemService, CacheService cacheService) {
        this.itemService = itemService;
        this.cacheService = cacheService;
    }

    @RequestMapping("/createItem")
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        ItemModel resItemModel = itemService.createItem(itemModel);
        ItemVO itemVO = convertItemVOFromItemModel(resItemModel);
        return CommonReturnType.create(itemVO);
    }



    @RequestMapping(value = "/listItem", method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = new ArrayList<>();
        for (ItemModel itemModel: itemModelList){
            itemVOList.add(convertItemVOFromItemModel(itemModel));
        }
        return CommonReturnType.create(itemVOList);
    }

    @RequestMapping(value = "/getItem", method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id){

        //读取本地缓存
        ItemModel itemModel = (ItemModel) cacheService.getFromCommonCache("item_" + id);
        if (itemModel == null){
            //读取redis缓存
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
            if (itemModel == null){
                //读mysql数据库
                itemModel = itemService.getItemById(id);
                //存入redis缓存
                redisTemplate.opsForValue().set("item_" + id, itemModel);
                redisTemplate.expire("item_" + id, 10, TimeUnit.MINUTES);
            }
            //存入本地缓存
            cacheService.setCommonCache("item_" + id, itemModel);
        }
        ItemVO itemVO = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO convertItemVOFromItemModel(ItemModel itemModel){
        if (itemModel == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if (itemModel.getPromoModel() != null){
            itemVO.setStatus(itemModel.getPromoModel().getPromoStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStatus(itemModel.getPromoModel().getPromoStatus());
            itemVO.setStartTime(itemModel.getPromoModel().getStartTime());
            itemVO.setEndTime(itemModel.getPromoModel().getEndTime());
            itemVO.setPromoItemPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setPromoName(itemModel.getPromoModel().getPromoName());
        }else{
            itemVO.setStatus(0);
        }
        return itemVO;
    }

    @RequestMapping(value = "/publishPromo", method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType publishPromo(@RequestParam(name = "id") Integer id){
        itemService.promoPublish(id);
        return CommonReturnType.create(null);
    }
}
