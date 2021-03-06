package com.miaoshaproject.miaosha.error;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 20:45
 * @Version 1.0
 */
public enum EmBusinessError implements CommonError{
    //通用错误类型00001
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    //未知错误
    UNKNOWN_ERROR(10002, "未知错误"),
    //20000开头为用户信息相关错误定义
    USER_NOT_EXIST_ERROR(20001, "用户不存在"),
    USER_TELPHONE_OR_PASSWORD_INCORRECT_ERROR(20002, "手机号或密码错误"),
    USER_NOT_LOGIN_ERROR(20003, "用户未登录"),
    //30000开头为订单信息相关错误描述
    ITEM_NOT_EXIST_ERROR(30001, "下单商品不存在"),
    ITEM_AMOUNT_ILLEGAL_ERROR(30002, "下单数量非法"),
    ITEM_STOCK_NOT_ENOUGH(30003, "库存不足"),
    MQ_SEND_FAIL(30004, "MQ发送消息失败"),
    ;

    private EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    private int errCode;
    private String errMsg;


    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
