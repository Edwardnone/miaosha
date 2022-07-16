package com.miaoshaproject.miaosha.service;

import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.service.model.ItemModel;

import java.util.List;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/17 10:21
 * @Version 1.0
 */
public interface ItemService {


    /**
     * 创建商品
     * @param itemModel
     * @return
     */
    public ItemModel createItem(ItemModel itemModel) throws BusinessException;

    /**
     * 查看商品列表
     * @return
     */
    public List<ItemModel> listItem();

    /**
     * 查看指定id商品详情信息
     * @param id
     * @return
     */
    public ItemModel getItemById(Integer id);

    /**
     * 通过redis缓存查询商品信息
     * @param id
     * @return
     */
    public ItemModel getItemByIdInCache(Integer id);

    /**
     * 减少指定id商品的库存数量
     * @param amount
     * @param itemId
     * @return
     */
    public Boolean decreaseStock(Integer amount, Integer itemId);

    /**
     * 异步更新库存
     * @param amount
     * @param itemId
     * @return
     */
    //public boolean asyncDecreaseStock(Integer amount, Integer itemId);

    /**
     * redis库存回补
     * @param amount
     * @param itemId
     * @return
     */
    public Boolean increaseStock(Integer amount, Integer itemId);

    /**
     * 增加指定id商品的销量
     * @param itemId
     * @param amount
     */
    public void increaseSales(Integer itemId, Integer amount);
    ///**
    // * 发布活动，将活动商品库存存入redis缓存
    // * @param promoId
    // */
    //public void promoPublish(Integer promoId);

    /**
     * 初始化库存流水
     * @param itemId
     * @param amount
     * @return
     */
    public String initStockLog(Integer itemId, Integer amount);
}
