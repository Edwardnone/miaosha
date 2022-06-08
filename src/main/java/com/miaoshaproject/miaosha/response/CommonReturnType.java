package com.miaoshaproject.miaosha.response;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/8 20:16
 * @Version 1.0
 */
public class CommonReturnType {
    //表明对应请求的请求结果，有“success” 或“fail”
    private String status;

    //若status=success，则data内返回前端需要的json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;

    /**
     * 定义一个通用的创建方法
     * @param result
     * @return
     */
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status){
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setStatus(status);
        commonReturnType.setData(result);
        return commonReturnType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
