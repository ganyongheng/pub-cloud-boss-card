package com.cn.auth.security;

import com.cn.auth.config.*;
import com.cn.auth.config.jwt.TokenProvider;
import com.pub.redis.service.RedisService;
import com.pub.redis.util.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by ganyongheng on 2017/6/14 0014.
 */
@Configuration
@EnableWebMvc
public class ServletInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private RedisCache redisCache;

    private final Logger log = LoggerFactory.getLogger(ServletInterceptorConfig.class);

    private  TokenProvider tokenProvider;

    public ServletInterceptorConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /** 不需要拦截地址 */
    public static final String[] excludeUrls_offline = {"/offline/userDo/login", "/offline/userDo/refreshToken", "/offline/goodFirstMeumDo/uploadImageTest" };
    public static final String[] excludeUrls_online = {"/online/userDo/login", "/online/userDo/refreshToken" , "/online/userDo/register","/online/userDo/sendEmail","/online/sysDataDictionaryDo/refreshCache","/online/userDo/forgetPwd","/online/userDo/sendEmailForgetPwd"};
    public static final String[] excludeUrls_school = {"/school/tripAreaDo/endTrips","/school/tripAreaDo/gethotTrips","/school/tripAreaDo/findTrips","/school/tripAreaDo/startTrips","/school/wx/wxCallback", "/school/wx/wxPayNotify" ,"/school/wx/payCallBack" ,  "/school/userDo/refreshToken",  "/school/wx/getImageByte","/school/tripAreaDo/uploadImage"};
    public static final String[] excludeUrls_heaven = {"/school/tripAreaDo/endTrips","/school/tripAreaDo/gethotTrips","/school/tripAreaDo/findTrips","/school/tripAreaDo/startTrips","/school/wx/wxCallback", "/school/wx/wxPayNotify" ,"/school/wx/payCallBack" ,  "/school/userDo/refreshToken",  "/school/wx/getImageByte","/school/tripAreaDo/uploadImage"};
    public static final String[] excludeUrls_workonline = {"/onlinework/wx/*","/onlinework/onlineUserDo/refreshToken"};
    public static final String[] excludeUrls_workoffline = {"/offlinework/offlineUserDo/login","/offlinework/offlineUserDo/refreshToken"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 离线的拦截器
         */
        registry.addInterceptor(new AuthorityInterceptor(tokenProvider,redisCache))
                .excludePathPatterns(excludeUrls_offline)
                .addPathPatterns("/offline/**");

        /**
         * 在线的拦截器
         */
        registry.addInterceptor(new OnlineAuthorityInterceptor(tokenProvider,redisCache))
                .excludePathPatterns(excludeUrls_online)
                .addPathPatterns("/online/**");
        /**
         * 在线的拦截器
         */
        registry.addInterceptor(new SchoolAuthorityInterceptor(tokenProvider,redisCache))
                .excludePathPatterns(excludeUrls_school)
                .addPathPatterns("/school/**");
        /**
         * 在线的纪念馆拦截器
         */
        registry.addInterceptor(new HeavenAuthorityInterceptor(tokenProvider,redisCache))
                .excludePathPatterns(excludeUrls_heaven)
                .addPathPatterns("/heaven/**");
        /**
         * 兼职小程序
         */
        registry.addInterceptor(new WorkOnlineAuthorityInterceptor(tokenProvider,redisCache))
                .excludePathPatterns(excludeUrls_workonline)
                .addPathPatterns("/onlinework/**");
        /**
         * 兼职小程序后台系统
         */
        registry.addInterceptor(new WorkOfflineAuthorityInterceptor(tokenProvider,redisCache))
                .excludePathPatterns(excludeUrls_workoffline)
                .addPathPatterns("/offlinework/**");



    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 设置访问源地址
        config.addAllowedOrigin("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);

    }


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(5000);
        return factory;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
