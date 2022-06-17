package com.miaoshaproject.miaosha.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/16 20:16
 * @Version 1.0
 */
public class ValidationResult {
    private boolean hasError;
    public HashMap<String, String> errMsgMap = new HashMap<>();

    public ValidationResult() {
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public HashMap<String, String> getErrMsgMap() {
        return errMsgMap;
    }

    public void setErrMsgMap(HashMap<String, String> errMsgMap) {
        this.errMsgMap = errMsgMap;
    }

    public String getErrMsg(){
        return StringUtils.join(errMsgMap.values().toArray(), ",");
    }
}
