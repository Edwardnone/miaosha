package com.miaoshaproject.miaosha.dao;

import com.miaoshaproject.miaosha.dataobject.ItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jun 17 09:58:16 GMT+08:00 2022
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jun 17 09:58:16 GMT+08:00 2022
     */
    int insert(ItemDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jun 17 09:58:16 GMT+08:00 2022
     */
    int insertSelective(ItemDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jun 17 09:58:16 GMT+08:00 2022
     */
    ItemDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jun 17 09:58:16 GMT+08:00 2022
     */
    int updateByPrimaryKeySelective(ItemDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jun 17 09:58:16 GMT+08:00 2022
     */
    int updateByPrimaryKey(ItemDO row);
}