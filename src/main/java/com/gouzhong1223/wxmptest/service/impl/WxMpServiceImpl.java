package com.gouzhong1223.wxmptest.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gouzhong1223.wxmptest.config.WechatConfig;
import com.gouzhong1223.wxmptest.dto.RespDto;
import com.gouzhong1223.wxmptest.entity.AccessToken;
import com.gouzhong1223.wxmptest.entity.WorkInfo;
import com.gouzhong1223.wxmptest.enums.MsgType;
import com.gouzhong1223.wxmptest.service.WxMpService;
import com.gouzhong1223.wxmptest.util.HttpUtil;
import com.gouzhong1223.wxmptest.util.JsonUtil;
import com.gouzhong1223.wxmptest.util.WxUtil;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 微信业务接口实现类
 * @Date : create by QingSong in 2020-10-25 15:34
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.service.impl
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Service
public class WxMpServiceImpl implements WxMpService {


    // 微信服务器-获取关注用户信息
    public static final String URLGETUSERINFO = "https://api.weixin.qq.com/cgi-bin/user/info";
    // 获取 accesstoken
    public static final String GETACCESSTOKENURL = "https://api.weixin.qq.com/cgi-bin/token";
    // 扫描二维码在公众号查看课程
    public static final String GETWORKS = "1";
    // 扫描二维码登录
    public static final String LOGINORREGISTER = "2";
    // redis 存登录二维码 ticket 前缀
    public static final String LOGINTICKET = "loginticket_";
    // redis 中的 accesstoken
    private final static String ACCESSTOKENKEY = "accesstoken";

    private final RedisTemplate<String, HashMap<String, WorkInfo>> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final WechatConfig wechatConfig;

    public WxMpServiceImpl(RedisTemplate redisTemplate, StringRedisTemplate stringRedisTemplate, WechatConfig wechatConfig) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.wechatConfig = wechatConfig;
    }

    @Override
    public void auth(HttpServletRequest request, HttpServletResponse response) {
        // 这个方法是通用的，不用管，只需要关注 token 就好了
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if (WxUtil.checkSignature(signature, timestamp, nonce)) {
                out.write(echostr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    @Override
    public String handler(Map<String, String> parseXml) throws Exception {

        // 获取消息类型
        String msgType = parseXml.get("MsgType");
        // 获取消息内容
        String content = parseXml.get("Content");
        // 获取发送方
        String fromusername = parseXml.get("FromUserName");
        // 获取接收方
        String tousername = parseXml.get("ToUserName");

        String ticket = parseXml.get("Ticket");

        System.out.println(parseXml);

        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("ToUserName", fromusername);
        resultMap.put("FromUserName", tousername);
        resultMap.put("CreateTime", LocalDate.now().toString());

        if (msgType.equalsIgnoreCase(MsgType.SHIJIAN.getMsgtype())) {

            if (ticket != null) {
                String eventKey = parseXml.get("EventKey");
                switch (eventKey) {
                    case GETWORKS: {
                        HashMap<String, WorkInfo> infoHashMap = redisTemplate.opsForValue().get(ticket);
                        WorkInfo workInfo = infoHashMap.get(ticket);
                        resultMap.put("MsgType", "text");
                        resultMap.put("Content", "这是标题" + "\n" + "<a href=\"" + workInfo.getWorkUrl() + "\">" + workInfo.getWorkName() + "</a>" + "\n" + "[旺柴][捂脸][呲牙][难过][微笑][流泪]\uD83D\uDE02[委屈]");
                        String mapToXml = WxUtil.mapToXml(resultMap);
                        return mapToXml;
                    }
                    case LOGINORREGISTER: {

                        resultMap.put("MsgType", "text");
                        resultMap.put("Content", "微信已经授权，请等待网页跳转");
                        stringRedisTemplate.opsForValue().set(LOGINTICKET + ticket, fromusername, 14400L, TimeUnit.SECONDS);
                        String mapToXml = WxUtil.mapToXml(resultMap);
                        return mapToXml;

                    }
                    default:
                        break;

                }
                HashMap<String, WorkInfo> infoHashMap = redisTemplate.opsForValue().get(ticket);
                WorkInfo workInfo = infoHashMap.get(ticket);
                resultMap.put("MsgType", "text");
                resultMap.put("Content", "这是标题" + "\n" + "<a href=\"" + workInfo.getWorkUrl() + "\">" + workInfo.getWorkName() + "</a>" + "\n" + "[旺柴][捂脸][呲牙][难过][微笑][流泪]\uD83D\uDE02[委屈]");
                String mapToXml = WxUtil.mapToXml(resultMap);
                return mapToXml;
            }
        }

        // 构造消息返回体
        System.out.println(parseXml);

        return null;
    }

    @Override
    public AccessToken getToken(String appid, String appSecrect) {

        HashMap<String, String> parms = new HashMap<>();
        parms.put("grant_type", "client_credential");
        parms.put("appid", appid);
        parms.put("secret", appSecrect);
        String s = HttpUtil.doGet(GETACCESSTOKENURL, parms);
        AccessToken accessToken = JsonUtil.fromJson(s, AccessToken.class);
        return accessToken;
    }

    @Override
    public JSONObject getUserInfoByOpenId(String openid) {
        String accesstoken = stringRedisTemplate.opsForValue().get(ACCESSTOKENKEY);
        HashMap<String, String> parms = new HashMap<>();
        parms.put("access_token", accesstoken);
        parms.put("openid", openid);
        parms.put("lang", "zh_CN");
        String doGet = HttpUtil.doGet(URLGETUSERINFO, parms);
        JSONObject jsonObject = JSON.parseObject(doGet);
        return jsonObject;
    }

    @Override
    public RespDto checkLogin(String ticket) {
        String openId = stringRedisTemplate.opsForValue().get(LOGINTICKET + ticket);
        if (openId == null || openId.equals("")) {
            return new RespDto(200, "未登录", null);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject userInfoByOpenId = getUserInfoByOpenId(openId);
        pushMessage(openId, userInfoByOpenId.getString("nickname"), sdf.format(new Date()));
        return new RespDto(200, "登陆成功", userInfoByOpenId);
    }

    @Override
    public void pushMessage(String openId, String... values) {

        //1,配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        //appid
        wxStorage.setAppId(wechatConfig.getAppId());
        //appsecret
        wxStorage.setSecret(wechatConfig.getAppsecret());
        me.chanjar.weixin.mp.api.WxMpService wxMpService = new me.chanjar.weixin.mp.api.impl.WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);

        //推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                //要推送的用户openid
                .toUser(openId)
                //模板id
                .templateId("O3OiVR3tfPd-DMMqMKLPgS-l6ihtCq9BYIyimLEl6iQ")
                //点击模板消息要访问的网址
                //TODO 到时候换成拼接的 URL
                .url("www.baidu.com")
                .build();
        //3,如果是正式版发送消息，，这里需要配置你的信息
        for (int i = 0; i < values.length; i++) {
            // 这里这个“name” 是我的模板消息配置了这个参数，真实的情况请切换成自己的模板消息参数，可变参数 Objects...也请切换
            templateMessage.addData(new WxMpTemplateData(String.valueOf(i + 1), values[i], "#173177"));
        }
        try {
            String msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

