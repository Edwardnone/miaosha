package com.miaoshaproject.miaosha.validator;


import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @Author yangLe
 * @Description TODO
 * @Date 2022/6/16 20:32
 * @Version 1.0
 */
@Component
public class ValidationImpl implements InitializingBean {
    private Validator validator;

    //实现效验方法并返回结果
    public ValidationResult validate(Object bean){
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (constraintViolationSet.size() > 0){
            //有错误
            validationResult.setHasError(true);
            constraintViolationSet.forEach(objectConstraintViolation -> {
                String errMsg = objectConstraintViolation.getMessage();
                String propertyName = objectConstraintViolation.getPropertyPath().toString();
                validationResult.getErrMsgMap().put(propertyName, errMsg);
            });
        }
        return validationResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}