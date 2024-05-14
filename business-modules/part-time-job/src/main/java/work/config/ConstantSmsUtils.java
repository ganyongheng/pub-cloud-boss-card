package work.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class ConstantSmsUtils implements InitializingBean {
    @Value("${tencent.sms.secretId}")
    private String secretID ;
    @Value("${tencent.sms.secretKey}")
    private String secretKey ;
    @Value("${tencent.sms.smsSdkAppId}")
    private String smsSdkAppID ;
    @Value("${tencent.sms.signName}")
    private String signName ;
    @Value("${tencent.sms.templateId}")
    private String templateID ;

    @Value("${tencent.sms.carTimeTemplateID}")
    private String carTimeTemplateID ;

    @Value("${tencent.sms.noticeSystemTemplateID}")
    private String noticeSystemTemplateID ;

    public static String SECRET_ID;
    public static String SECRET_KEY;
    public static String SMSSDKAPP_ID;
    public static String SIGN_NAME;
    public static String TEMPLATE_ID;
    public static String CarTime_TEMPLATE_ID;
    public static String NoticeSystem_TEMPLATE_ID;


    @Override
    public void afterPropertiesSet() throws Exception {
        SECRET_ID = secretID;
        SECRET_KEY = secretKey;
        SMSSDKAPP_ID = smsSdkAppID;
        SIGN_NAME = signName;
        TEMPLATE_ID = templateID;
        CarTime_TEMPLATE_ID = carTimeTemplateID;
        NoticeSystem_TEMPLATE_ID = noticeSystemTemplateID;
    }

}
