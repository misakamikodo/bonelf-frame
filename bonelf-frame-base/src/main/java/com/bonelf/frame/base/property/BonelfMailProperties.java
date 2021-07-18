package com.bonelf.frame.base.property;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author bonelf
 */
@Data
@Component
@ConfigurationProperties(prefix = "bonelf.mail")
public class BonelfMailProperties {

    /**
     * 网易企业邮 smtp
     */
    private String smtp = "sftp.ym.163.com";
    /**
     * 网易企业邮 username（邮箱地址）
     */
    private String username;
    /**
     * 网易企业邮 密码
     */
    private String password;
}
