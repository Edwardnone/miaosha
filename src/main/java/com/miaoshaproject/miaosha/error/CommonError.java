package com.miaoshaproject.miaosha.error;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 20:44
 * @Version 1.0
 */
public interface CommonError {

    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
