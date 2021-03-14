package com.bonelf.frame.base.property;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description 短信验证码
 * @author:qingcong
 * @date:2019/11/5
 * @ver:1.0
 **/
@Data
@Component
@ConfigurationProperties(prefix = "ali.sms")
public class AliSmsProperties {

    private String regionId = "cn-hangzhou";

    private String version = "2017-05-25";

    private String accessKeyId;

    private String accessSecret;

    private String signName;
}
