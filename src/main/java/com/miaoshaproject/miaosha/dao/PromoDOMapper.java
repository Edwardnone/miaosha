package com.miaoshaproject.miaosha.dao;

import com.miaoshaproject.miaosha.dataobject.PromoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromoDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Tue Jun 21 15:15:10 GMT+08:00 2022
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Tue Jun 21 15:15:10 GMT+08:00 2022
     */
    int insert(PromoDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Tue Jun 21 15:15:10 GMT+08:00 2022
     */
    int insertSelective(PromoDO row);

    /**
     * 通过商品id查询商品秒杀信息
     * @param itemId
     * @return
     */
    PromoDO selectByItemId(Integer itemId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Tue Jun 21 15:15:10 GMT+08:00 2022
     */
    int updateByPrimaryKeySelective(PromoDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Tue Jun 21 15:15:10 GMT+08:00 2022
     */
    int updateByPrimaryKey(PromoDO row);
}