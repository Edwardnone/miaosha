package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.ItemDOMapper;
import com.miaoshaproject.miaosha.dao.ItemStockDOMapper;
import com.miaoshaproject.miaosha.dao.PromoDOMapper;
import com.miaoshaproject.miaosha.dao.StockLogDOMapper;
import com.miaoshaproject.miaosha.dataobject.ItemDO;
import com.miaoshaproject.miaosha.dataobject.ItemStockDO;
import com.miaoshaproject.miaosha.dataobject.PromoDO;
import com.miaoshaproject.miaosha.dataobject.StockLogDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.mq.MqProducer;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.PromoService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.service.model.PromoModel;
import com.miaoshaproject.miaosha.validator.ValidationImpl;
import com.miaoshaproject.miaosha.validator.ValidationResult;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author yangLe
 * @Description 商品ServiceImpl
 * @Date 2022/6/17 10:26
 * @Version 1.0
 */
@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private ValidationImpl validationImpl;
    private ItemDOMapper itemDOMapper;
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Resource
    private StockLogDOMapper stockLogDOMapper;

    //@Resource
    //private MqProducer mqProducer;

    @Resource
    private RedisTemplate redisTemplate;

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
        if (itemDO == null){
            return null;
        }
        //获取库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        ItemModel itemModel = convertItemModelFromDataObject(itemDO, itemStockDO);
        //获取商品活动信息
        PromoModel promoModel = promoService.getPromoByItemId(itemDO.getId());
        if (promoModel != null && promoModel.getPromoStatus() != 0){
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null){
            itemModel = getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id, itemModel);
            redisTemplate.expire("item_validate_"+id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    //@Transactional(rollbackFor = java.lang.Exception.class)
    @Override
    public Boolean decreaseStock(Integer amount, Integer itemId) {
        long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount.intValue()*-1);
        if (result > 0){
            return true;
        }else if (result == 0){
            //设置库存售罄标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, true);
            return true;
        }
        else {
            increaseStock(amount, itemId);
            return false;
        }
    }

    //@Override
    //public boolean asyncDecreaseStock(Integer amount, Integer itemId) {
    //    boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
    //    return mqResult;
    //}

    @Override
    public Boolean increaseStock(Integer amount, Integer itemId) {
        redisTemplate.opsForValue().increment("promo_item_stock_"+ itemId, amount.intValue());
        return true;
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

    //@Override
    //public void promoPublish(Integer promoId) {
    //    //通过活动id获取商品id
    //    PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
    //    if (promoDO == null || promoDO.getItemId().intValue() == 0){
    //        return;
    //    }
    //    ItemModel itemModel = getItemById(promoDO.getItemId());
    //    if (itemModel != null){
    //        //讲库存同步到缓存中
    //        redisTemplate.opsForValue().set("promo_item_stock_" + itemModel.getId(), itemModel.getStock());
    //    }
    //}

    @Transactional(rollbackFor = java.lang.Exception.class)
    @Override
    public String initStockLog(Integer itemId, Integer amount) {
        String id = UUID.randomUUID().toString().replace("-", "");
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setStockLogId(id);
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStatus(1);
        stockLogDOMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }


}
