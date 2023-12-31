package com.cn.school.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.cn.school.config.*;
import com.cn.school.entity.*;
import com.cn.school.entity.dto.UnlimitedQRCodeParam;
import com.cn.school.mapper.TripOrderMapper;
import com.cn.school.service.ITripOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.school.util.WXPayUtil;
import com.cn.school.util.WeixinApiClient;
import com.cn.school.util.WxPayRequstUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pub.core.common.OrderStatusEnum;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.CalculateUtil;
import com.pub.core.utils.DateUtils;
import com.pub.redis.util.RedisCache;
import com.sun.org.apache.regexp.internal.RE;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.cn.school.util.WXPayConstants.SignType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-16
 */
@Log4j2
@Service
public class TripOrderServiceImpl extends ServiceImpl<TripOrderMapper, TripOrderDo> implements ITripOrderService {

    private Logger addWXpayLog= LoggerFactory.getLogger("addWXpayLog");
    private Logger addOrderLog= LoggerFactory.getLogger("addOrderLog");
    private Logger logOnCarLog= LoggerFactory.getLogger("logOnCarLog");
    private Logger refundsTripOrderLog= LoggerFactory.getLogger("refundsTripOrderLog");

    @Autowired
    private FilePathOnlineConfig filePathOnlineConfig;


    @Autowired
    private TripProductServiceImpl tripProductService;
    @Autowired
    private TripCarServiceImpl tripCarServiceImpl;
    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseWxConfig baseWxConfig;

    @Autowired
    private SysDataDictionaryServiceImpl sysDataDictionaryServiceImpl;

    public List<TripOrderDo> myTripOrderDo(TripOrderDo tripOrderDo) {
        User currentUser = UserContext.getCurrentUser();
        Integer id = currentUser.getId();
        Integer status = tripOrderDo.getStatus();
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        if(status!=null){
            //我的订单status=1 成功  其它失败的
            if(status==1){
                wq.eq("status",Constant.OrderStatus.SUCESS);
            }else{
                wq.ne("status",Constant.OrderStatus.SUCESS);
            }
        }
        Integer onCarStatus = tripOrderDo.getOnCarStatus();
        if(onCarStatus!=null){
            //0 未出行  非0已出行
            if(onCarStatus==0){
                wq.eq("on_car_status", 0);
            }else{
                wq.ne("on_car_status", 0);
            }
            wq.eq("status",Constant.OrderStatus.SUCESS);
        }
         wq.eq("user_id", id);
        wq.orderByDesc("create_time");
        List<TripOrderDo> list = list(wq);
        return list;
    }
    public List<TripOrderDo> myInvitationTripOrder(TripOrderDo tripOrderDo) {
        Integer invitationStatus = tripOrderDo.getInvitationStatus();
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        if(invitationStatus!=null){
            wq.eq("invitation_status",invitationStatus);
        }
        wq.eq("invitation_openid", tripOrderDo.getInvitationOpenid());
        wq.orderByDesc("create_time");
        List<TripOrderDo> list = list(wq);
        return list;
    }

    /**
     * 这里一定要加上锁,此时一定要锁单
     * @param tripOrderDo
     */
    @Transactional
    public  void addTripOrder(TripOrderDo tripOrderDo) throws Exception{
        save(tripOrderDo);
        Integer num = tripOrderDo.getNum();
        tripCarServiceImpl.addTicket(tripOrderDo.getCarId(),num,Constant.TicketAddStatus.DEL);
    }
    /**
     * 这里一定要加上锁,此时一定要锁单 ,
     * @param tripOrderDo
     */
    @Transactional
    public  void addTripOrderByIntegral(TripOrderDo tripOrderDo) throws Exception{
        /**
         * 设置返现金额
         */
        setInvitationFee(tripOrderDo);
        save(tripOrderDo);
        Integer num = tripOrderDo.getNum();
        tripCarServiceImpl.addTicket(tripOrderDo.getCarId(),num,Constant.TicketAddStatus.DEL);
        /**
         * 积分扣除
         */
        userServiceImpl.addIntegral(tripOrderDo.getUserId(),tripOrderDo.getTotalFee(),Constant.TicketAddStatus.DEL);
    }

    /**
     * 拉起微信支付
     * @param tripOrderDo
     */
    public void submitTripOrder(TripOrderDo tripOrderDo){
        Integer userId = tripOrderDo.getUserId();
        UserDo userDo = userServiceImpl.getById(userId);
        String openid = userDo.getOpenid();
            String url = wxPayConfig.getUrl();
            String reqStr = getReqStr(openid,tripOrderDo); //组装预下单的请求数据
            String results = WeixinApiClient.weixinPay(url,reqStr);//发送post数据到微信预下单
            Map<String,String> return_data = null;
            try {
                return_data = WXPayUtil.xmlToMap(results);//微信的一个工具类
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
            String return_code = return_data.get("return_code");
            log.info("return_code=" + return_code);
            if("SUCCESS".equals(return_code)){
                String prepay_id = return_data.get("prepay_id");
            }else{
                results ="{\"return_code\":\"fail\"}";
            }

    }



    public  String getReqStr(String openid,TripOrderDo tripOrderDo){
        JSONObject data = new JSONObject();
        data.put("appid", wxPayConfig.getAppid());
        data.put("mchid",wxPayConfig.getMchid());
        data.put("out_trade_no", tripOrderDo.getOrderId());
        data.put("notify_url",wxPayConfig.getNotifyUrl());
        data.put("description","校园旅行大巴服务");
        //金额
        JSONObject amount = new JSONObject();
        amount.put("currency","CNY");
        amount.put("total",Integer.valueOf(tripOrderDo.getTotalFee()));
        data.put("amount",amount);
        //支付者
        JSONObject payer = new JSONObject();
        payer.put("openid",openid);
        data.put("payer",payer);
        data.put("sign_type", "MD5");
        try {
            String sign = WXPayUtil.generateSignature(null, "merKey", SignType.MD5);
            data.put("sign", sign);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("sign error");
        }
        String reqBody = null;
        try {
            reqBody = WXPayUtil.mapToXml(null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reqBody;
    }

    public  String createTaskNo() {
        String yyyyMMddStr = DateUtils.dateTime();
        //设置两天过期  1000*60*60*48
        String hincr = redisCache.hincr(yyyyMMddStr, yyyyMMddStr, 1, 1000*60*60*48)+"";
        if(hincr.contains(".")){
            String[] split = hincr.split("\\.");
            String rtn = String.format("%08d", Integer.valueOf(split[0]));
            return "JF"+yyyyMMddStr+rtn;
        }else{
            String rtn = String.format("%08d", Integer.valueOf(hincr));
            return "JF"+yyyyMMddStr+rtn;
        }
    }

    /**
     * 生成退款订单号
     * @return
     */
    public  String createRefundsTaskNo() {
        String yyyyMMddStr = DateUtils.dateTime();
        //设置两天过期  1000*60*60*48
        String hincr = redisCache.hincr(yyyyMMddStr, yyyyMMddStr, 1, 1000*60*60*48)+"";
        if(hincr.contains(".")){
            String[] split = hincr.split("\\.");
            String rtn = String.format("%08d", Integer.valueOf(split[0]));
            return "RF"+yyyyMMddStr+rtn;
        }else{
            String rtn = String.format("%08d", Integer.valueOf(hincr));
            return "RF"+yyyyMMddStr+rtn;
        }
    }


    public String tets(TripOrderDo tripOrderDo,String openid) throws Exception {
        HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-type","application/json; charset=utf-8");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("mchid",wxPayConfig.getMchid())
                .put("appid", wxPayConfig.getAppid())
                .put("description", "校园车租车小程序预支付申请")
                .put("notify_url", wxPayConfig.getNotifyUrl())
                .put("out_trade_no", tripOrderDo.getOrderId());
        rootNode.putObject("amount")
                .put("total",Integer.valueOf(tripOrderDo.getTotalFee()));
        rootNode.putObject("payer")
                .put("openid", openid);

        objectMapper.writeValue(bos, rootNode);
        httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
        /**
         *
         merchantId商户号。
         merchantSerialNumber商户API证书的证书序列号。
         merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题。
         wechatPayCertificates微信支付平台证书列表。你也可以使用后面章节提到的“定时更新平台证书功能”，而不需要关心平台证书的来龙去脉。

         */
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(Constant.privateKey);
        X509Certificate wechatPayCertificate = PemUtil.loadCertificate(
                new ByteArrayInputStream(Constant.certificate.getBytes(StandardCharsets.UTF_8)));

        ArrayList<X509Certificate> listCertificates = new ArrayList<>();
        listCertificates.add(wechatPayCertificate);

        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(wxPayConfig.getMchid(), wxPayConfig.getBusinessPayId(), merchantPrivateKey)
                .withWechatPay(listCertificates);

        CloseableHttpClient httpClient = builder.build();
        CloseableHttpResponse response = httpClient.execute(httpPost);

        String prepay_id = EntityUtils.toString(response.getEntity());

        return prepay_id;
    }

    public static RSAAutoCertificateConfig config = null ;
    String filePath ="C:/Users/Administrator/Desktop/card/cert/apiclient_key.pem";//测试环境可放到resource目录下
    public static JsapiServiceExtension service = null ;
    public PrepayWithRequestPaymentResponse WeChartPay(TripOrderDo tripOrderDo,String openid) {
        //元转换为分
        Integer amountInteger = Integer.valueOf(WxPayRequstUtil.getMoney(tripOrderDo.getTotalFee()));
        // 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
        if (config == null) {
            synchronized ("createConfig"){
                if (config == null) {
                    config = new RSAAutoCertificateConfig.Builder()
                            .merchantId(wxPayConfig.getMchid())
                            .privateKey(Constant.privateKey)
                            .merchantSerialNumber(wxPayConfig.getBusinessPayId())
                            //.apiV3Key("llikjdkYY2546525llYkjdk2546525ll")
                            .apiV3Key(wxPayConfig.getApiV3Key())
                            .build();
                    // 构建service
                    if (service == null) {
                        service = new JsapiServiceExtension.Builder().config(config).build();
                    }
                }

            }

        }
        //组装预约支付的实体
        PrepayRequest request = new PrepayRequest();
        //计算金额
        Amount amount = new Amount();
        amount.setTotal(amountInteger);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        //公众号appId
        request.setAppid(wxPayConfig.getAppid());
        //商户号
        request.setMchid(wxPayConfig.getMchid());
        //支付者信息
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);
        //描述
        request.setDescription(wxPayConfig.getDescription());
        //微信回调地址，需要是https://开头的，必须外网可以正常访问
        //本地测试可以使用内网穿透工具，网上很多的
        request.setNotifyUrl(wxPayConfig.getNotifyUrl());
        //订单号
        request.setOutTradeNo(tripOrderDo.getOrderId());
        // 加密
        addWXpayLog.info("{}订单号拉起支付,请求支付参数为{}",tripOrderDo.getOrderId(),JSONObject.toJSONString(request));
        PrepayWithRequestPaymentResponse payment = service.prepayWithRequestPayment(request);
        //默认加密类型为RSA
        payment.setSignType("MD5");
        //返回数据，前端调起支付
        addWXpayLog.info("{}订单号拉起支付,返回结果为{}",tripOrderDo.getOrderId(),JSONObject.toJSONString(payment));
        return payment;
    }

    public void createTripOrder(TripOrderDo tripOrderDo) throws Exception{
        /**
         * 同一个班次订票不付款超过2次需要在1个小时后才能购票，超过3次不付款直接禁止当天所有的购票
         */
        User currentUser = UserContext.getCurrentUser();
        Integer user_id = currentUser.getId();
        addOrderLog.info("用户id为{}下单,下单请求参数为{}",user_id,JSONObject.toJSONString(tripOrderDo));
        UserDo userDo = userServiceImpl.getById(user_id);
        QueryWrapper<TripOrderDo> wq_chech=new QueryWrapper<>();
        wq_chech.eq("user_id",user_id);
        /*wq_chech.eq("product_id",tripOrderDo.getProductId());
        wq_chech.eq("car_id",tripOrderDo.getCarId());*/
        wq_chech.eq("status",Constant.OrderStatus.REFUND);
        wq_chech.like("create_time", DateUtils.getDate());
        wq_chech.orderByDesc("id");
        List<TripOrderDo> list_check = list(wq_chech);
        String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("refunds_notice_msg", "refunds_notice_msg");
        JSONObject refunds_notice_msg = JSONObject.parseObject(sysBaseParam);
        Integer blackCount = refunds_notice_msg.getInteger("blackCount");
        if(list_check.size()>blackCount){
            throw new BusinessException("当天超过"+blackCount+"次退票,已被禁止购票！");
        }
        Integer waitCount = refunds_notice_msg.getInteger("waitCount");
        Integer waitTimeCount = refunds_notice_msg.getInteger("waitTimeCount");
        if(list_check.size()>waitCount){
            TripOrderDo tripOrderDo_num = list_check.get(0);
            Date createTime = tripOrderDo_num.getCreateTime();
            if(createTime.before(DateUtils.addHours(new Date(),waitTimeCount))){
                throw new BusinessException("该天超过"+waitCount+"次退票,已被禁止"+waitTimeCount+"小时后购票！");
            }
        }

        TripCarDo tripCarDo = tripCarServiceImpl.getById(tripOrderDo.getCarId());
        Integer orderNum = tripCarDo.getOrderNum();
        Integer sellNum = tripCarDo.getSellNum();
        if( orderNum-sellNum-tripOrderDo.getNum()<0){
            //说明没票了
            throw  new BusinessException("余票不足！请刷新购买页面");
        }

        /**
         * 校验购买金额是否一致
         */
        TripProductDo tripProductDo = tripProductService.getById(tripOrderDo.getProductId());
        String fee = tripProductDo.getFee();
       /* if(!fee.equals(tripOrderDo.getPrice())){
            throw  new BusinessException("车票单价被改动！");
        }*/
        String totalFee = tripOrderDo.getTotalFee();
        String cal_totalFee = CalculateUtil.cal(new StringBuilder(fee).append("*").append(tripOrderDo.getNum()).toString())+"";
        if(!totalFee.equals(cal_totalFee)){
            throw  new BusinessException("车票总价格被改动！");
        }
        String taskNo = createTaskNo();
        tripOrderDo.setStarTime(tripCarDo.getStartTime());
        tripOrderDo.setEndTime(tripCarDo.getEndTime());
        tripOrderDo.setUserId(userDo.getId());
        tripOrderDo.setPhone(userDo.getPhone());
        tripOrderDo.setIdentityName(userDo.getIdentityName());
        tripOrderDo.setSchool(userDo.getSchool());
        tripOrderDo.setOrderId(taskNo);
        /**
         * 初始化
         */
        tripOrderDo.setStatus(Constant.OrderStatus.WAIT);
        /**
         * 未上车
         */
        tripOrderDo.setOnCarStatus(0);
        /**
         * 未释放车票
         */
        tripOrderDo.setTicketStatus(Constant.TicketStatus.NO);
        /**
         * 发车前多少小时，默认未通知
         */
        tripOrderDo.setNoticeStatus(-1);
        tripOrderDo.setCreateTime(new Date());
        tripOrderDo.setPayType(Constant.PayType.wx);

    }
    public void createTripOrderByIntegral(TripOrderDo tripOrderDo) throws Exception{
        /**
         * 同一个班次订票不付款超过2次需要在1个小时后才能购票，超过3次不付款直接禁止当天所有的购票
         */
        User currentUser = UserContext.getCurrentUser();
        Integer user_id = currentUser.getId();
        addOrderLog.info("用户id为{}下单,下单请求参数为{}",user_id,JSONObject.toJSONString(tripOrderDo));
        UserDo userDo = userServiceImpl.getById(user_id);
        QueryWrapper<TripOrderDo> wq_chech=new QueryWrapper<>();
        wq_chech.eq("user_id",user_id);
        /*wq_chech.eq("product_id",tripOrderDo.getProductId());
        wq_chech.eq("car_id",tripOrderDo.getCarId());*/
        wq_chech.eq("status",Constant.OrderStatus.REFUND);
        wq_chech.like("create_time", DateUtils.getDate());
        wq_chech.orderByDesc("id");
        List<TripOrderDo> list_check = list(wq_chech);
        String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("refunds_notice_msg", "refunds_notice_msg");
        JSONObject refunds_notice_msg = JSONObject.parseObject(sysBaseParam);
        Integer blackCount = refunds_notice_msg.getInteger("blackCount");
        if(list_check.size()>blackCount){
            throw new BusinessException("当天超过"+blackCount+"次退票,已被禁止购票！");
        }
        Integer waitCount = refunds_notice_msg.getInteger("waitCount");
        Integer waitTimeCount = refunds_notice_msg.getInteger("waitTimeCount");
        if(list_check.size()>waitCount){
            TripOrderDo tripOrderDo_num = list_check.get(0);
            Date createTime = tripOrderDo_num.getCreateTime();
            if(createTime.before(DateUtils.addHours(new Date(),waitTimeCount))){
                throw new BusinessException("该天超过"+waitCount+"次退票,已被禁止"+waitTimeCount+"小时后购票！");
            }
        }

        TripCarDo tripCarDo = tripCarServiceImpl.getById(tripOrderDo.getCarId());
        Integer orderNum = tripCarDo.getOrderNum();
        Integer sellNum = tripCarDo.getSellNum();
        if( orderNum-sellNum-tripOrderDo.getNum()<0){
            //说明没票了
            throw  new BusinessException("余票不足！请刷新购买页面");
        }

        /**
         * 校验购买金额是否一致
         */
        TripProductDo tripProductDo = tripProductService.getById(tripOrderDo.getProductId());
        String fee = tripProductDo.getFee();
       /* if(!fee.equals(tripOrderDo.getPrice())){
            throw  new BusinessException("车票单价被改动！");
        }*/
        String totalFee = tripOrderDo.getTotalFee();
        String cal_totalFee = CalculateUtil.cal(new StringBuilder(fee).append("*").append(tripOrderDo.getNum()).toString())+"";
        if(!totalFee.equals(cal_totalFee)){
            throw  new BusinessException("车票总价格被改动！");
        }
        /**
         * 积分是否够用
         */
        String integral = userDo.getIntegral();
        BigDecimal cal = CalculateUtil.cal(new StringBuilder(integral).append("-").append(totalFee).toString());
        if(cal.compareTo(BigDecimal.ZERO) < 0){
            //说明积分不够
            throw  new BusinessException("积分不够!");
        }
        String taskNo = createTaskNo();
        tripOrderDo.setStarTime(tripCarDo.getStartTime());
        tripOrderDo.setEndTime(tripCarDo.getEndTime());
        tripOrderDo.setUserId(userDo.getId());
        tripOrderDo.setPhone(userDo.getPhone());
        tripOrderDo.setIdentityName(userDo.getIdentityName());
        tripOrderDo.setSchool(userDo.getSchool());
        tripOrderDo.setOrderId(taskNo);
        /**
         * 初始化
         */
        tripOrderDo.setStatus(Constant.OrderStatus.SUCESS);
        /**
         * 未上车
         */
        tripOrderDo.setOnCarStatus(0);
        /**
         * 未释放车票
         */
        tripOrderDo.setTicketStatus(Constant.TicketStatus.NO);
        /**
         * 发车前多少小时，默认未通知
         */
        tripOrderDo.setNoticeStatus(-1);
        /**
         * 积分支付方式
         */
        tripOrderDo.setPayType(Constant.PayType.jf);
        tripOrderDo.setCreateTime(new Date());
    }
    @Transactional
    public void wxPayNotify(TripOrderDo tripOrderDo) throws Exception{
        // 订单状态   0 初始  1成功  -1 失败
        tripOrderDo.setPayTime(new Date());
        Integer status = tripOrderDo.getStatus();
        if(1==status){
            /**
             * 生成二维码
             */

        }else{
            /**
             * 释放车票
             */
            Integer ticketStatus = tripOrderDo.getTicketStatus();
            if(ticketStatus!=9){
                tripOrderDo.setTicketStatus(9);
                Integer num = tripOrderDo.getNum();
                Integer carId = tripOrderDo.getCarId();
                tripCarServiceImpl.addTicket(carId,num,Constant.TicketAddStatus.ADD);
            }
        }
        updateById(tripOrderDo);
    }


    public String getAccessToken(){
        String access_token_school = redisCache.getStringCache("access_token_school");
        if(StringUtils.isNotBlank(access_token_school)){
            return access_token_school;
        }else{
            //1.通过code获取access_token
            String url = "https://api.weixin.qq.com/cgi-bin/token?appid=APPID&secret=SECRET&grant_type=client_credential";
            url = url.replace("APPID", wxPayConfig.getAppid()).replace("SECRET", wxPayConfig.getWxAppSecret());
            ResponseEntity<String> tokenData = restTemplate.getForEntity(url, String.class);
            String tokenInfoStr = tokenData.getBody();
            if(StringUtils.isNotBlank(tokenInfoStr)){
                JSONObject tokenInfoObject = JSONObject.parseObject(tokenInfoStr);
                log.info("getAccessToken:{}", tokenInfoObject);
                String access_token = tokenInfoObject.getString("access_token");
                Long expires_in = tokenInfoObject.getLong("expires_in");
                if(StringUtils.isNotBlank(access_token)){
                    //提前十分钟过期
                    redisCache.putCacheWithExpireTime("access_token_school",access_token,expires_in-600);
                    return access_token;
                }
            }
            return null;

        }



    }
    public byte[] getImageByte(UnlimitedQRCodeParam body) throws Exception{
        String accessToken = getAccessToken();
        if(StringUtils.isBlank(accessToken)){
            throw new BusinessException("获取token异常！");
        }
        //1.通过code获取access_token
        ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(wxPayConfig.getUrlToken() + "?access_token=" + accessToken, body, byte[].class);
        byte[] urls = responseEntity.getBody();
        return urls;
    }

    /**
     * 单机部署，不存在分布式，加个锁就行了
     * @param carId
     * @throws Exception
     */
    public synchronized TripOrderDo checkTripOrder(Integer id, Integer carId) throws Exception{
        TripOrderDo tripOrderDo = getById(id);
        Integer status = tripOrderDo.getStatus();
        if(status==Constant.OrderStatus.FAIL){
            throw new BusinessException("未支付！");
        }else if(status==Constant.OrderStatus.REFUND){
            throw new BusinessException("已退票！");
        }else if(status==Constant.OrderStatus.WAIT){
            throw new BusinessException("未支付！");
        }
        Integer onCarStatus = tripOrderDo.getOnCarStatus();
        Integer num = tripOrderDo.getNum();
        logOnCarLog.info("订单号为{}验票上车,车票总数{},当前验票人数为{}",tripOrderDo.getOrderId(),tripOrderDo.getNum(),tripOrderDo.getOnCarStatus()+1);
        Integer carIdDb = tripOrderDo.getCarId();
        if(carIdDb!=carId){
            throw new BusinessException("车次不正确！");
        }

        /**
         * 0 未上车  1 已上车1人  2 上车两人  n上车n人
         */
        if(onCarStatus>=num){
            throw new BusinessException("该车票已被核销完成！");
        }
        tripOrderDo.setOnCarStatus(onCarStatus+1);
        tripOrderDo.setOncarTime(new Date());
        updateById(tripOrderDo);
        /**
         * 设置已发车
         */
       /* TripCarDo tripCarDo = tripCarServiceImpl.getById(carId);
        //-1未发车   9 已发车
        tripCarDo.setIsDeparted(9);
        tripCarServiceImpl.updateById(tripCarDo);*/
        return tripOrderDo;
    }

    /**
     * 不允许并发，防止出错
     * @param orderId
     * @throws Exception
     */
    public synchronized void refundsTripOrder(String orderId) throws Exception {
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        wq.eq("order_id",orderId);
        TripOrderDo tripOrderDo = getOne(wq);
        Integer ticketStatus = tripOrderDo.getStatus();
        if(Constant.OrderStatus.SUCESS!=ticketStatus){
            throw  new  BusinessException("订单已全额退款或者未支付成功！");
        }
        /**
         * 积分支付，微信支付
         */
        Integer payType = tripOrderDo.getPayType();
        if(Constant.PayType.wx==payType){
            refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},",orderId,tripOrderDo.getIdentityName());
            //发车前一小时不允许退票
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type","application/json; charset=utf-8");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("out_trade_no",tripOrderDo.getOrderId());
            /**
             * 退款订单号
             */
            String refundsTaskNo = createRefundsTaskNo();
            tripOrderDo.setRefundOrderId(refundsTaskNo);
            rootNode.put("out_refund_no", refundsTaskNo);

            rootNode.putObject("amount")
                    /**
                     * 退款金额,需要转换
                     */
                    .put("refund", Integer.valueOf(WxPayRequstUtil.getMoney(tripOrderDo.getRefundFee())))
                    /**
                     * 原订单金额
                     */
                    .put("total", Integer.valueOf(WxPayRequstUtil.getMoney(tripOrderDo.getTotalFee())))
                    .put("currency", "CNY");
            refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},请求参数{}",orderId,tripOrderDo.getIdentityName(),JSONObject.toJSONString(rootNode));
            objectMapper.writeValue(bos, rootNode);
            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
            /**
             *
             merchantId商户号。
             merchantSerialNumber商户API证书的证书序列号。
             merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题。
             wechatPayCertificates微信支付平台证书列表。你也可以使用后面章节提到的“定时更新平台证书功能”，而不需要关心平台证书的来龙去脉。

             */
            CloseableHttpClient httpClient = baseWxConfig.getHttpClient();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String prepay_id = EntityUtils.toString(response.getEntity());
            refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},返回结果{}",orderId,tripOrderDo.getIdentityName(),prepay_id);
            JSONObject jsonObject = JSONObject.parseObject(prepay_id);
            String status = jsonObject.getString("status");
            /**
             * SUCCESS：退款成功
             * CLOSED：退款关闭
             * PROCESSING：退款处理中
             * ABNORMAL：退款异常
             */
            if(StringUtils.isNotBlank(status)&&("SUCCESS".equals(status)||"PROCESSING".equals(status))){
                opertorRefundsTripOrderSucess(tripOrderDo);
            }else{
                throw  new  BusinessException("退款失败，返回为{}！",prepay_id);
            }
        }else{
            opertorRefundsTripOrderSucessByInvitation(tripOrderDo);
        }


    }

    /**
     * 积分退票处理
     * @param tripOrderDo
     * @throws Exception
     */
    @Transactional
    public void opertorRefundsTripOrderSucessByInvitation( TripOrderDo tripOrderDo) throws Exception {
        tripOrderDo.setRefundTime(new Date());
        tripOrderDo.setStatus(Constant.OrderStatus.REFUND);
        /**
         * 释放车票
         */
        Integer ticketStatus = tripOrderDo.getTicketStatus();
        if(ticketStatus!=9){
            tripOrderDo.setTicketStatus(9);
            Integer num = tripOrderDo.getNum();
            Integer carId = tripOrderDo.getCarId();
            tripCarServiceImpl.addTicket(carId,num,Constant.TicketAddStatus.ADD);
        }
        /**
         * 添加积分
         */
        userServiceImpl.addIntegral(tripOrderDo.getUserId(),tripOrderDo.getRefundFee(),Constant.TicketAddStatus.ADD);
        updateById(tripOrderDo);
    }
    @Transactional
    public void opertorRefundsTripOrderSucess( TripOrderDo tripOrderDo) throws Exception {
        tripOrderDo.setRefundTime(new Date());
        tripOrderDo.setStatus(Constant.OrderStatus.REFUND);
        /**
         * 释放车票
         */
        Integer ticketStatus = tripOrderDo.getTicketStatus();
        if(ticketStatus!=9){
            tripOrderDo.setTicketStatus(9);
            Integer num = tripOrderDo.getNum();
            Integer carId = tripOrderDo.getCarId();
            tripCarServiceImpl.addTicket(carId,num,Constant.TicketAddStatus.ADD);
        }
        updateById(tripOrderDo);
    }

    private JSONObject getRefundsFee( TripOrderDo tripOrderDo,JSONObject refunds_notice_msg ) {
        JSONObject jsRtn=new JSONObject();
        Integer num = tripOrderDo.getNum();
        String totalFee = tripOrderDo.getTotalFee();
        jsRtn.put("totalFee",totalFee);
        jsRtn.put("num",num);
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        wq.eq("status",Constant.OrderStatus.REFUND);
        wq.eq("user_id",tripOrderDo.getUserId());
        wq.eq("car_id",tripOrderDo.getCarId());
        wq.eq("product_id",tripOrderDo.getProductId());
        List<TripOrderDo> list = list(wq);
        Integer refundOne = refunds_notice_msg.getInteger("refundOne");
        StringBuilder sb=new StringBuilder("0");
        jsRtn.put("refundCont",list.size());
        /**
         * 退费次数要算上本次，所以必须加1
         */
        if(list!=null&&list.size()+1>refundOne){
            //超过一次以上每次每张票扣除10元
            String refundOneFee = refunds_notice_msg.getString("refundOneFee");
            sb.append("+").append(refundOneFee).append("*").append(num+"");
            refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},超过一次以上每次扣除{}元！",tripOrderDo.getOrderId(),tripOrderDo.getIdentityName(),refundOneFee);
            BigDecimal cal = CalculateUtil.cal(refundOneFee + "*" + num);
            jsRtn.put("refundFee",cal+"");

        }else{
            jsRtn.put("refundFee","0");
        }
        //发车前2小时每张票扣除20元
        Date starTime = tripOrderDo.getStarTime();
        jsRtn.put("starTime",DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS,starTime));
        Integer noRefundCarTime = refunds_notice_msg.getInteger("noRefundCarTime");
        if((DateUtils.addHours(new Date(),noRefundCarTime).after(starTime))){
            String refundCarFee = refunds_notice_msg.getString("refundCarFee");
            sb.append("+").append(refundCarFee).append("*").append(num+"");;
            refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},发车前{}小时扣除{}元！",tripOrderDo.getOrderId(),tripOrderDo.getIdentityName(),noRefundCarTime,refundCarFee);
            BigDecimal cal = CalculateUtil.cal(refundCarFee + "*" + num);
            jsRtn.put("overTimeFee",cal+"");
        }else{
            jsRtn.put("overTimeFee","0");
        }
        BigDecimal cal = CalculateUtil.cal(sb.toString());
        String refundTotalFee = CalculateUtil.cal(totalFee + "-" + cal)+"";
        jsRtn.put("refundTotalFee",refundTotalFee);
        tripOrderDo.setRefundFee(refundTotalFee);
        updateById(tripOrderDo);
        return jsRtn;
    }


    public Integer checkPaySucess(String orderId) {
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        wq.eq("order_id",orderId);
        TripOrderDo one = getOne(wq);
        /**
         * 订单状态   0 初始  1成功  -1 失败
         */
        if(one.getStatus()== 1){
            return 1;
        }
        return 0;
    }

    public List<TripOrderDo> getPageList(TripOrderDo req) {
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        String orderId = req.getOrderId();
        if(StringUtils.isNotBlank(orderId)){
            wq.eq("order_id",orderId);
        }
        Integer status = req.getStatus();
        if(status!=null){
            wq.eq("status",status);
        }
        Integer carId = req.getCarId();
        if(carId!=null){
            wq.eq("car_id",carId);
        }
        Integer onCarStatus = req.getOnCarStatus();
        if(onCarStatus!=null){
            if(onCarStatus==0){
                wq.eq("on_car_status",onCarStatus);
            }else{
                //已上车不等于0即可
                wq.ne("on_car_status",0);
            }

        }
        String identityName = req.getIdentityName();
        if(StringUtils.isNotBlank(identityName)){
            identityName=identityName.trim();
            wq.eq("identity_name",identityName);
        }
        String phone = req.getPhone();
        if(StringUtils.isNotBlank(phone)){
            phone=phone.trim();
            wq.eq("phone",phone);
        }
        String createTime = req.getCreateTimeStr();
        if(StringUtils.isNotBlank(createTime)){
            createTime=createTime.trim();
            wq.like("create_time",createTime);
        }
        String origin = req.getOrigin();
        if(StringUtils.isNotBlank(origin)){
            origin=origin.trim();
            wq.like("origin",origin);
        }
        String destination = req.getDestination();
        if(StringUtils.isNotBlank(destination)){
            destination=destination.trim();
            wq.like("destination",destination);
        }
        BaseController.startPage();
        List<TripOrderDo> list = list(wq);
        return list;
    }

    public String importExcel(TripOrderDo req) {
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        String orderId = req.getOrderId();
        if(StringUtils.isNotBlank(orderId)){
            wq.eq("order_id",orderId);
        }
        Integer status = req.getStatus();
        if(status!=null){
            wq.eq("status",status);
        }
        Integer carId = req.getCarId();
        if(carId!=null){
            wq.eq("car_id",carId);
        }
        Integer onCarStatus = req.getOnCarStatus();
        if(onCarStatus!=null){
            if(onCarStatus==0){
                wq.eq("on_car_status",onCarStatus);
            }else{
                //已上车不等于0即可
                wq.ne("on_car_status",0);
            }

        }
        String identityName = req.getIdentityName();
        if(StringUtils.isNotBlank(identityName)){
            identityName=identityName.trim();
            wq.eq("identity_name",identityName);
        }
        String phone = req.getPhone();
        if(StringUtils.isNotBlank(phone)){
            phone=phone.trim();
            wq.eq("phone",phone);
        }
        String createTime = req.getCreateTimeStr();
        if(StringUtils.isNotBlank(createTime)){
            createTime=createTime.trim();
            wq.like("create_time",createTime);
        }
        String origin = req.getOrigin();
        if(StringUtils.isNotBlank(origin)){
            origin=origin.trim();
            wq.like("origin",origin);
        }
        String destination = req.getDestination();
        if(StringUtils.isNotBlank(destination)){
            destination=destination.trim();
            wq.like("destination",destination);
        }
        List<TripOrderDo> list = list(wq);
        if(list.size()>0){
            List<TripOrderExcelDo> listTripOrderExcelDo=new ArrayList<>();
            for (TripOrderDo tripOrderDo : list) {
                TripOrderExcelDo tripOrderExcelDo=new  TripOrderExcelDo();
                tripOrderExcelDo.setOrderId(tripOrderDo.getOrderId());
                Integer carId_list = tripOrderDo.getCarId();
                TripCarDo byId = tripCarServiceImpl.getById(carId_list);
                tripOrderExcelDo.setCarNumber(byId.getCarNumber());
                tripOrderExcelDo.setCreateTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS,tripOrderDo.getCreateTime()));
                tripOrderExcelDo.setDestination(tripOrderDo.getDestination());
                tripOrderExcelDo.setOrigin(tripOrderDo.getOrigin());
                tripOrderExcelDo.setIdentityName(tripOrderDo.getIdentityName());
                tripOrderExcelDo.setNum(tripOrderDo.getNum());
                tripOrderExcelDo.setPhone(tripOrderDo.getPhone());
                tripOrderExcelDo.setSchool(tripOrderDo.getSchool());
                Integer onCarStatus1 = tripOrderDo.getOnCarStatus();
                if(onCarStatus1!=null){
                    if(onCarStatus1==0){
                        tripOrderExcelDo.setOnCarStatus("未上车");
                    }else{
                        tripOrderExcelDo.setOnCarStatus("上车"+onCarStatus1+"人");
                    }
                }
                tripOrderExcelDo.setTotalFee(tripOrderDo.getTotalFee());
                tripOrderExcelDo.setPrice(tripOrderDo.getPrice());
                Date oncarTime = tripOrderDo.getOncarTime();
                if(oncarTime!=null){
                    tripOrderExcelDo.setOncarTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS,oncarTime));
                }
                tripOrderExcelDo.setRefundFee(tripOrderDo.getRefundFee());
                Date refundTime = tripOrderDo.getRefundTime();
                if(refundTime!=null){
                    tripOrderExcelDo.setRefundTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS,refundTime));
                }
                Integer status1 = tripOrderDo.getStatus();
                if(status1==0){
                    tripOrderExcelDo.setStatus("待支付");
                }else if(status1==1){
                    tripOrderExcelDo.setStatus("支付成功");
                }else if(status1==-1){
                    tripOrderExcelDo.setStatus("支付失败");
                }else if(status1==-2){
                    tripOrderExcelDo.setStatus("已退款");
                }
                listTripOrderExcelDo.add(tripOrderExcelDo);
            }
            if(listTripOrderExcelDo.size()>0){
                //存储文件路径
                String root = filePathOnlineConfig.getRoot();
                String baseUrl = filePathOnlineConfig.getBaseUrl();
                String fileName = DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS,new Date())+".xlsx";
                String absolutePath=root+"/"+fileName;
                // 创建ExcelWriter对象
                ExcelWriter excelWriter = EasyExcel.write(absolutePath, TripOrderExcelDo.class).build();

                // 创建WriteSheet对象
                WriteSheet writeSheet = EasyExcel.writerSheet("订单导出").build();

                // 将数据写入Excel
                excelWriter.write(listTripOrderExcelDo, writeSheet);

                // 关闭ExcelWriter对象
                excelWriter.finish();
                return baseUrl+"/"+fileName;
            }
        }
        return null;
    }

    public synchronized JSONObject refundsTripOrderBefore(String orderId) throws Exception{
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        wq.eq("order_id",orderId);
        TripOrderDo tripOrderDo = getOne(wq);
        Integer ticketStatus = tripOrderDo.getStatus();
        if(Constant.OrderStatus.SUCESS!=ticketStatus){
            throw  new  BusinessException("订单已全额退款或者未支付成功！");
        }
        refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},",orderId,tripOrderDo.getIdentityName());
        //发车前一小时不允许退票
        Date starTime = tripOrderDo.getStarTime();
        String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("refunds_notice_msg", "refunds_notice_msg");
        JSONObject refunds_notice_msg = JSONObject.parseObject(sysBaseParam);
        Integer noRefundTime = refunds_notice_msg.getInteger("noRefundTime");
        Date date = DateUtils.addHours(new Date(), noRefundTime);
        if((date.after(starTime))){
            refundsTripOrderLog.info("订单号{}发起退票申请,退票用户为{},发车前{}小时不允许退票！",orderId,tripOrderDo.getIdentityName(),noRefundTime);
            throw  new  BusinessException("发车前"+noRefundTime+"小时不允许退票！");
        }
        /**
         * 获取退费的费用
         */
        return getRefundsFee(tripOrderDo, refunds_notice_msg);
    }

    /**
     * 统一设置返现金额，初始化
     * @param tripOrderDo
     */
    public void setInvitationFee(TripOrderDo tripOrderDo) throws Exception {
        /**
         * 设置返现金额
         */
        UserDo userDo = userServiceImpl.getById(tripOrderDo.getUserId());
        String invitationOpenid = userDo.getInvitationOpenid();
        if (StringUtils.isNotBlank(invitationOpenid)) {
            tripOrderDo.setInvitationOpenid(invitationOpenid);
            String invitation_rate = sysDataDictionaryServiceImpl.getSysBaseParam("invitation_rate", "invitation_rate");
            BigDecimal cal = CalculateUtil.cal(new StringBuilder().append(tripOrderDo.getTotalFee()).append("*").append(invitation_rate).append("/100").toString());

            tripOrderDo.setInvitationFee(cal + "");
            /**
             * 初始化，真正返现，等到无法退票以后，发车以后
             */
            tripOrderDo.setInvitationStatus(Constant.InvitationStatus.WAIT);
        }else{
            //没有邀请码，就没有返现
            tripOrderDo.setInvitationStatus(Constant.InvitationStatus.NO);
        }
    }
}
