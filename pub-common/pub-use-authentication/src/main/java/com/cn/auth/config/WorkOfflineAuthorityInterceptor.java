package com.cn.auth.config;

import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.ResultMessageConstants;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.util.domain.AjaxResult;
import com.pub.redis.service.RedisService;
import com.pub.redis.util.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 *
 */
public class WorkOfflineAuthorityInterceptor extends HandlerInterceptorAdapter {

    private Logger log= LoggerFactory.getLogger("WorkOfflineAuthorityInterceptor");

    private RedisCache redisCache;

    @Autowired
    private RedisService redisService;

    protected Logger logger = LoggerFactory.getLogger(WorkOfflineAuthorityInterceptor.class);
    private TokenProvider tokenProvider;
    private UserContext userContext;
    private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<Long>("StopWatch-StartTime");

    public WorkOfflineAuthorityInterceptor(TokenProvider tokenProvider, RedisCache redisCache) {
        this.tokenProvider = tokenProvider;
        this.redisCache=redisCache;
    }

    private void responseMessage(HttpServletResponse response, Object obj) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter pw = response.getWriter();
        pw.write(JSONObject.toJSONString(obj));
        pw.close();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        long beginTime = System.currentTimeMillis();//1、开始时间
        startTimeThreadLocal.set(beginTime);//线程绑定变量（该数据只有当前请求的线程可见）
        if (!handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            //判断handler是不是指定类HandlerMethod
            return true;
        }

        String jwt = request.getHeader(TokenProvider.AUTHORIZATION_HEADER_OFFLINE);
        if(!StringUtils.hasText(jwt)){
            //未携带token
            log.info("未携带token");
            responseMessage(response, AjaxResult.error(ResultMessageConstants.B00008.message()));
            return false;
        }
        //校验jwt是否合法
        if (!jwt.startsWith("BearerWorkOffline")) {
            //格式不对
            log.info("格式不对");
            responseMessage(response, AjaxResult.error(ResultMessageConstants.B00011.message()));
            return false;
        }
         String  jwt_check= jwt.substring(16, jwt.length());
        JSONObject jsonObject = this.tokenProvider.validateTokenNew(jwt_check);
        String code = jsonObject.getString("code");
        if(!Constant.Online.Jwt.sucess_code.equals(code)){
            if(Constant.Online.Jwt.ExpiredJwtException_err_code.equals(code)){
                //说明AUTHORIZATION_HEADER_ONLINE已超时 ,通知前端刷新AUTHORIZATION_HEADER_ONLINE
                JSONObject token_json=new JSONObject();
                token_json.put("code","0");
                token_json.put("message","请刷新token!");
                log.info("请刷新token");
                responseMessage(response, token_json);
                return false;
            }
            responseMessage(response, AjaxResult.error(ResultMessageConstants.B00008.message()));
            return false;
        }
        User cache = redisCache.getCache(jwt, User.class);
        if(cache==null){
            log.info("redisCache不存在token");
            responseMessage(response, AjaxResult.error(ResultMessageConstants.B00003.message()));
            return false;
        }else{
            userContext = new UserContext(cache);
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (userContext != null) {
            userContext.close();
        }
        long endTime = System.currentTimeMillis();//2、结束时间
        long beginTime = startTimeThreadLocal.get();//得到线程绑定的局部变量（开始时间）
        long consumeTime = endTime - beginTime;//3、消耗的时间
        if (consumeTime > 2000) {//此处认为处理时间超过500毫秒的请求为慢请求
            //TODO 记录到日志
            logger.info(String.format("%s consume %d millis", request.getRequestURI(), consumeTime));
        }
        super.afterCompletion(request, response, handler, ex);
    }






}
