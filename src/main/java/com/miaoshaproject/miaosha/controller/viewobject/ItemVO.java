package com.miaoshaproject.miaosha.controller.viewobject;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Author yangLe
 * @Description 商品视图对象
 * @Date 2022/6/17 11:41
 * @Version 1.0
 */
public class ItemVO {
    private Integer id;

    /**
     * 商品名称
     */
    private String title;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品库存
     */
    private Integer stock;

    /**
     * 商品销量
     */
    private Integer sales;


    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片url地址
     */
    private String imgUrl;

    @Override
    public String toString() {
        return "ItemVO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", sales=" + sales +
                ", description='" + description + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
