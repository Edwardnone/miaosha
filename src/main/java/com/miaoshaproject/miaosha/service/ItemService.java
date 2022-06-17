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
     * 查看商品详情信息
     * @param id
     * @return
     */
    public ItemModel getItem(String id);
}
