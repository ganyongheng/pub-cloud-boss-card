package work.util;

import com.alibaba.fastjson.JSONObject;
import com.pub.core.utils.RandomUtilSendMsg;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import work.config.ConstantSmsUtils;

/**
 * 腾讯发送短信的工具类
 */
@Log4j2
@Component
public class SendSmsTx {

    private Logger sendSmsTxLog= LoggerFactory.getLogger("sendSmsTxLog");

    public static void main(String [] args) {
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential("AKIDnaVWbgIe2Gbu3GkEcrCUcaVOBrRwFPsP", "Y5aHxX8nLm8QzW7bZQ7hEpLoDcuytmaK");
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
          /*  ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);*/
            // 实例化要请求产品的client对象,clientProfile是可选的   第二个参数是地域信息
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            //设置固定的参数
            req.setSmsSdkAppId("1400852690");// 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId
            req.setSignName("河源中科教育");//短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名
            req.setTemplateId("1918863");//模板 ID: 必须填写已审核通过的模板 ID
            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
//            String sessionContext = "xxx";
//            req.setSessionContext(sessionContext);
            //设置发送相关的参数
            String[] phoneNumberSet1 = {"19925799382"};
            req.setPhoneNumberSet(phoneNumberSet1);//发送的手机号
            //生成6位数随机验证码
            String[] templateParamSet1 = {RandomUtilSendMsg.getSixBitRandom()};//模板的参数 第一个是验证码，第二个是过期时间
            req.setTemplateParamSet(templateParamSet1);//发送验证码
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            // 输出json格式的字符串回包
            System.out.println(SendSmsResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
    public SendSmsResponse sendMsg(String phone,String randomCode){
        try{
            sendSmsTxLog.info("手机号{}发送短信，验证码{}",phone,randomCode);
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(ConstantSmsUtils.SECRET_ID, ConstantSmsUtils.SECRET_KEY);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
          /*  ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);*/
            // 实例化要请求产品的client对象,clientProfile是可选的   第二个参数是地域信息
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            //设置固定的参数
            req.setSmsSdkAppId(ConstantSmsUtils.SMSSDKAPP_ID);// 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId
            req.setSignName(ConstantSmsUtils.SIGN_NAME);//短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名
            req.setTemplateId(ConstantSmsUtils.TEMPLATE_ID);//模板 ID: 必须填写已审核通过的模板 ID
            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
//            String sessionContext = "xxx";
//            req.setSessionContext(sessionContext);
            //设置发送相关的参数
            String[] phoneNumberSet1 = {phone};
            req.setPhoneNumberSet(phoneNumberSet1);//发送的手机号
            //生成6位数随机验证码
            String[] templateParamSet1 = {randomCode};//模板的参数 第一个是验证码，第二个是过期时间
            req.setTemplateParamSet(templateParamSet1);//发送验证码
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            sendSmsTxLog.info("发送短信返回结果{}",JSONObject.toJSONString(resp));
            return resp;
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            sendSmsTxLog.error("发送短信异常信息{}",e.getMessage());
        }
        return null;
    }

    /**
     * 余票不足通知管理员
     * @param phone
     * @return
     */
    public SendSmsResponse sendMsgNoticeSystem(String phone,String msg){
        try{
            sendSmsTxLog.info("手机号{}发送短信，通知管理员消息{}",phone,msg);
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(ConstantSmsUtils.SECRET_ID, ConstantSmsUtils.SECRET_KEY);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
          /*  ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);*/
            // 实例化要请求产品的client对象,clientProfile是可选的   第二个参数是地域信息
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            //设置固定的参数
            req.setSmsSdkAppId(ConstantSmsUtils.SMSSDKAPP_ID);// 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId
            req.setSignName(ConstantSmsUtils.SIGN_NAME);//短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名
            req.setTemplateId(ConstantSmsUtils.NoticeSystem_TEMPLATE_ID);//模板 ID: 必须填写已审核通过的模板 ID
            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
//            String sessionContext = "xxx";
//            req.setSessionContext(sessionContext);
            //设置发送相关的参数
            String[] phoneNumberSet1 = {phone};
            req.setPhoneNumberSet(phoneNumberSet1);//发送的手机号
            //生成6位数随机验证码
            String[] templateParamSet1 = {msg};//模板的参数 第一个是验证码，第二个是过期时间
            req.setTemplateParamSet(templateParamSet1);//发送验证码
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            sendSmsTxLog.info("发送短信返回结果{}",JSONObject.toJSONString(resp));
            return resp;
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            sendSmsTxLog.error("发送短信异常信息{}",e.getMessage());
        }
        return null;
    }
    /**
     * 发车前多少小时通知用户
     *
     * {1},您预订的车票，最晚发车时间{2}，提前达到集合点，请携带好您的行李,做好扫码上车准备,此次车牌号是{3}，任意一部车都可以坐，祝您一路顺风!
     *
     * 这个短信能不让他们手动来发呢。
     * @return
     */
    public SendSmsResponse sendMsgCarTime(String phone ,String name,String time,String carNum){
        try{
            sendSmsTxLog.info("手机号{}发送短信，通知用户{}",phone,name);
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(ConstantSmsUtils.SECRET_ID, ConstantSmsUtils.SECRET_KEY);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
          /*  ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);*/
            // 实例化要请求产品的client对象,clientProfile是可选的   第二个参数是地域信息
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            //设置固定的参数
            req.setSmsSdkAppId(ConstantSmsUtils.SMSSDKAPP_ID);// 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId
            req.setSignName(ConstantSmsUtils.SIGN_NAME);//短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名
            req.setTemplateId(ConstantSmsUtils.CarTime_TEMPLATE_ID);//模板 ID: 必须填写已审核通过的模板 ID
            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
//            String sessionContext = "xxx";
//            req.setSessionContext(sessionContext);
            //设置发送相关的参数
            String[] phoneNumberSet1 = {phone};
            req.setPhoneNumberSet(phoneNumberSet1);//发送的手机号
            //生成6位数随机验证码
            String[] templateParamSet1 = {name,time,carNum};//模板的参数 第一个是验证码，第二个是过期时间
            req.setTemplateParamSet(templateParamSet1);//发送验证码
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            sendSmsTxLog.info("发送短信返回结果{}",JSONObject.toJSONString(resp));
            return resp;
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            sendSmsTxLog.error("发送短信异常信息{}",e.getMessage());
        }
        return null;
    }

}
