package com.miaoshaproject.miaosha.dao;

import com.miaoshaproject.miaosha.dataobject.UserPasswordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPasswordDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Wed Jun 15 02:04:25 GMT+08:00 2022
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Wed Jun 15 02:04:25 GMT+08:00 2022
     */
    int insert(UserPasswordDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Wed Jun 15 02:04:25 GMT+08:00 2022
     */
    int insertSelective(UserPasswordDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Wed Jun 15 02:04:25 GMT+08:00 2022
     */
    UserPasswordDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Wed Jun 15 02:04:25 GMT+08:00 2022
     */
    int updateByPrimaryKeySelective(UserPasswordDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Wed Jun 15 02:04:25 GMT+08:00 2022
     */
    int updateByPrimaryKey(UserPasswordDO row);

    /**
     * 通过用户id查询用户密码信息
     * @param userId
     * @return
     */
    UserPasswordDO selectByUserId(Integer userId);
}