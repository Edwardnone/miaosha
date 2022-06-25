package com.miaoshaproject.miaosha.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * @Author yangLe
 * @Description 当Spring容器内没有TomcatEmbeddedServletContainerFactory这个bean时，会把此bean加载进springboot
 * @Date 2022/6/25 23:31
 * @Version 1.0
 */
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        //使用对应工厂类提供给我们的接口定制化我们的tomcat connector

    }
}
