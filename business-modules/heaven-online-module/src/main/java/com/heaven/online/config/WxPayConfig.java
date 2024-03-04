package com.heaven.online.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


@RefreshScope
@Data
@Component
@ConfigurationProperties(prefix = "wx")
public class WxPayConfig {


	private String appid;


	private String wxAppSecret;


}
