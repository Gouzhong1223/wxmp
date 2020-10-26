package com.gouzhong1223.wxmptest.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 关于微信的各种配置
 * @Date : create by QingSong in 2020-10-25 16:40
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.config
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Component
@Getter
@Setter
@ToString
public class WechatConfig {

    @Value("${wx.createTicketUrl}")
    private String createTicketUrl;

    @Value("${wx.showQrcodeUrl}")
    private String showQrcodeUrl;

    @Value("${wx.shortQrcodeUrl}")
    private String shortQrcodeUrl;

    @Value("${wx.appId}")
    private String appId;

    @Value("${wx.appsecret}")
    private String appsecret;
}
