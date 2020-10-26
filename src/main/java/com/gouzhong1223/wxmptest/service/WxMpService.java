package com.gouzhong1223.wxmptest.service;

import com.alibaba.fastjson.JSONObject;
import com.gouzhong1223.wxmptest.dto.RespDto;
import com.gouzhong1223.wxmptest.entity.AccessToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2020-10-25 15:32
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.service
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
public interface WxMpService {

    /**
     * 进行微信 URL 认证
     *
     * @param request  请求体
     * @param response 返回体
     */
    void auth(HttpServletRequest request, HttpServletResponse response);

    /**
     * 接收所有微信服务器发送过来的消息
     *
     * @param parseXml 消息参数
     * @return 返回的 XML
     */
    String handler(Map<String, String> parseXml) throws Exception;

    /**
     * 获取 AccessToken
     *
     * @param appid
     * @param appSecrect
     * @return
     */
    AccessToken getToken(String appid, String appSecrect);

    /**
     * 通过openid，获取关注用户信息
     *
     * @param openid
     * @return
     * @throws Exception
     */
    JSONObject getUserInfoByOpenId(String openid) throws Exception;


    /**
     * 检查是否登录
     *
     * @param ticket 二维码 ticket
     * @return
     */
    RespDto checkLogin(String ticket);

    /**
     * 推送消息
     *
     * @param openId openId
     * @param values 参数
     */
    void pushMessage(String openId, String... values);
}
