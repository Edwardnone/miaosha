package com.miaoshaproject.miaosha.controller;

import com.miaoshaproject.miaosha.controller.viewobject.ItemVO;
import com.miaoshaproject.miaosha.error.BusinessException;
import com.miaoshaproject.miaosha.response.CommonReturnType;
import com.miaoshaproject.miaosha.service.ItemService;
import com.miaoshaproject.miaosha.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/17 10:56
 * @Version 1.0
 */
@Controller("item")
@RequestMapping("/item")
public class ItemController extends BaseController{

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
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
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = convertItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO convertItemVOFromItemModel(ItemModel itemModel){
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        return itemVO;
    }
}
