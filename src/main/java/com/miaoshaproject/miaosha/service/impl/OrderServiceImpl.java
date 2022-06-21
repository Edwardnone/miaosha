package com.miaoshaproject.miaosha.service.impl;

import com.miaoshaproject.miaosha.dao.OrderDOMapper;
import com.miaoshaproject.miaosha.dataobject.OrderDO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.error.EmBusinessError;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.OrderService;
import com.miaoshaproject.miaosha.service.SequenceService;
import com.miaoshaproject.miaosha.service.UserService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import com.miaoshaproject.miaosha.service.model.UserModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/19 22:26
 * @Version 1.0
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private UserService userService;

    private ItemService itemService;

    private SequenceService sequenceService;

    private OrderDOMapper orderDOMapper;

    public OrderServiceImpl(UserService userService, ItemService itemService, SequenceService sequenceService, OrderDOMapper orderDOMapper) {
        this.userService = userService;
        this.itemService = itemService;
        this.sequenceService = sequenceService;
        this.orderDOMapper = orderDOMapper;
    }

    @Override
    @Transactional(rollbackFor = java.lang.Exception.class)
    public void createOrder(Integer userId, Integer itemId, Integer amount, BigDecimal promoItemPrice) throws BusinessException {
        //校验参数
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST_ERROR);
        }
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST_ERROR);
        }
        if (amount <= 0 || amount >= 100){
            throw new BusinessException(EmBusinessError.ITEM_AMOUNT_ILLEGAL_ERROR);
        }
        //下单锁定库存（这里不用支付锁定）
        boolean result = itemService.decreaseStock(amount, itemId);
        if (!result){
            throw new BusinessException(EmBusinessError.ITEM_STOCK_NOT_ENOUGH);
        }
        //创建订单记录
        //生成订单id
        StringBuilder orderId = new StringBuilder();
        //加上时间
        String time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        orderId.append(time.replace("-", ""));
        //加上序列号
        //orderId.append(sequenceService.getAndUpdateSequence("order"));
        Integer sequence = sequenceService.getAndUpdateSequence("order");
        for (int i=0; i<6-String.valueOf(sequence).length(); i++){
            orderId.append("0");
        }
        orderId.append(sequence);
        //加上分库分表号
        orderId.append("00");
        OrderDO orderDO = new OrderDO();
        orderDO.setId(orderId.toString());
        orderDO.setUserId(userId);
        orderDO.setItemId(itemId);
        orderDO.setAmount(amount);
        if (promoItemPrice == null){
            orderDO.setOrderAmount(itemModel.getPrice().multiply(new BigDecimal(amount)));
        }else {
            orderDO.setOrderAmount(promoItemPrice.multiply(new BigDecimal(amount)));
        }

        orderDOMapper.insertSelective(orderDO);
        //增加销量数
        itemService.increaseSales(itemId, amount);
    }
}
