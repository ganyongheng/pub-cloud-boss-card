package work.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


@RefreshScope
@Data
@Component
@ConfigurationProperties(prefix = "wx")
public class WxPayConfig {

	private String url;

	private String urlToken;

	private String payUrl;

	private String appid;

	private String mchid;

	private String notifyUrl;



	private String wxAppSecret;

	//商户证书序列号有误
	private String businessPayId;

	/**
	 * 支付描述
	 */
	private String description;
	/**
	 * 支付描述
	 */
	private String apiV3Key;

}
