package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.ItemDOMapper;
import com.miaoshaproject.miaosha.dao.ItemStockDOMapper;
import com.miaoshaproject.miaosha.dataobject.ItemDO;
import com.miaoshaproject.miaosha.dataobject.ItemStockDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.validator.ValidationImpl;
import com.miaoshaproject.miaosha.validator.ValidationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yangLe
 * @Description 商品ServiceImpl
 * @Date 2022/6/17 10:26
 * @Version 1.0
 */
@Service
public class ItemServiceImpl implements ItemService {

    private ValidationImpl validationImpl;
    private ItemDOMapper itemDOMapper;
    private ItemStockDOMapper itemStockDOMapper;

    public ItemServiceImpl(ValidationImpl validation, ItemDOMapper itemDOMapper, ItemStockDOMapper itemStockDOMapper) {
        this.validationImpl = validation;
        this.itemDOMapper = itemDOMapper;
        this.itemStockDOMapper = itemStockDOMapper;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validationImpl.validate(itemModel);
        if(result.isHasError()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        //插入商品表
        ItemDO itemDO = convertItemDOFromItemModel(itemModel);
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());
        //插入商品库存表
        ItemStockDO itemStockDO = convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回商品模型
        ItemDO itemDO1 = itemDOMapper.selectByPrimaryKey(itemDO.getId());
        ItemStockDO itemStockDO1 = itemStockDOMapper.selectByItemId(itemStockDO.getId());
        ItemModel returnItemModel = convertItemModelFromDataObject(itemDO1, itemStockDO1);
        return returnItemModel;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.selectAll();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convertItemModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        ItemModel itemModel = convertItemModelFromDataObject(itemDO, itemStockDO);
        return itemModel;
    }

    @Transactional(rollbackFor = java.lang.Exception.class)
    @Override
    public Boolean decreaseStock(Integer amount, Integer itemId) {
        int res = itemStockDOMapper.decreaseStock(amount, itemId);
        if (res > 0){
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = java.lang.Exception.class)
    @Override
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseSales(itemId, amount);
    }

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemModel convertItemModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }

}
