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

import javax.annotation.Resource;
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
    // 未关注-扫描作品二维码
    private final static String UNSUBSCRIBEGETWORKS = "qrscene_1";
    // 未关注-扫描登录
    private final static String UNSUBSCRIBELOGINTICKET = "qrscene_2";
    // 用户信息缓存前缀
    private final static String USERINFOPREFIX = "userinfo_";
    private final StringRedisTemplate stringRedisTemplate;
    private final WechatConfig wechatConfig;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public WxMpServiceImpl(StringRedisTemplate stringRedisTemplate, WechatConfig wechatConfig) {
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
        try (PrintWriter out = response.getWriter()) {
            if (WxUtil.checkSignature(signature, timestamp, nonce)) {
                out.write(echostr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String handler(Map<String, String> parseXml) throws Exception {

        // 获取消息类型
        String msgType = parseXml.get("MsgType");
        // 获取消息内容
//        String content = parseXml.get("Content");
        // 获取发送方
        String fromusername = parseXml.get("FromUserName");
        // 获取接收方
        String tousername = parseXml.get("ToUserName");
        // 获取二维码Ticket
        String ticket = parseXml.get("Ticket");
        // 获取推送事件
        String event = parseXml.get("Event");
        // EventKey
        String eventKey = parseXml.get("EventKey");

        // 到缓存里面去取出用户信息
        JSONObject userInfo = (JSONObject) redisTemplate.opsForValue().get(USERINFOPREFIX + fromusername);
        if (userInfo == null) {
            // 获取用户信息
            userInfo = getUserInfoByOpenId(fromusername);

            // 把用户信息放入缓存
            redisTemplate.opsForValue().set(USERINFOPREFIX + fromusername, userInfo, 60L, TimeUnit.MINUTES);
        }

        // 构造返回结果
        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("ToUserName", fromusername);
        resultMap.put("FromUserName", tousername);
        resultMap.put("CreateTime", LocalDate.now().toString());
        resultMap.put("MsgType", "text");

        // 判断是否为事件推送
        if (msgType.equalsIgnoreCase(MsgType.SHIJIAN.getMsgtype())) {
            // 判断是否为直接关注
            if ("subscribe".equals(event) && ticket == null) {
                resultMap.put("Content", userInfo.getString("nickname") + "感谢关注");
                // 返回
                return WxUtil.mapToXml(resultMap);
            }
            // 说明是扫描带参数二维码推送的消息
            if (ticket != null) {
                switch (eventKey) {

                    // 通过扫描作品链接二维码--已关注
                    case GETWORKS: {
                        HashMap<String, WorkInfo> infoHashMap = (HashMap<String, WorkInfo>) redisTemplate.opsForValue().get(ticket);
                        assert infoHashMap != null;
                        WorkInfo workInfo = infoHashMap.get(ticket);
                        resultMap.put("Content", "作品名字" + "\n" + "<a href=\"" + workInfo.getWorkUrl() + "\">" + workInfo.getWorkName() + "</a>" + "\n" + "[旺柴][捂脸][呲牙][难过][微笑][流泪]\uD83D\uDE02[委屈]");
                        return WxUtil.mapToXml(resultMap);
                    }
                    // 通过扫描二维码登录--已关注
                    case LOGINORREGISTER: {
                        resultMap.put("Content", "微信已经授权,请等待网页跳转");
                        stringRedisTemplate.opsForValue().set(LOGINTICKET + ticket, fromusername, 14400L, TimeUnit.SECONDS);
                        return WxUtil.mapToXml(resultMap);
                    }
                    // 通过扫描作品链接二维码--未关注
                    case UNSUBSCRIBEGETWORKS: {
                        HashMap<String, WorkInfo> infoHashMap = (HashMap<String, WorkInfo>) redisTemplate.opsForValue().get(ticket);
                        assert infoHashMap != null;
                        WorkInfo workInfo = infoHashMap.get(ticket);
                        resultMap.put("Content", "感谢关注" + "\n这是作品链接\n" + "<a href=\"" + workInfo.getWorkUrl() + "\">" + workInfo.getWorkName() + "</a>" + "\n" + "[旺柴][捂脸][呲牙][难过][微笑][流泪]\uD83D\uDE02[委屈]");
                        return WxUtil.mapToXml(resultMap);
                    }
                    // 通过扫描二维码登录--未关注
                    case UNSUBSCRIBELOGINTICKET: {
                        resultMap.put("Content", "感谢关注,微信已经授权,请等待网页跳转");
                        stringRedisTemplate.opsForValue().set(LOGINTICKET + ticket, fromusername, 14400L, TimeUnit.SECONDS);
                        return WxUtil.mapToXml(resultMap);
                    }
                    default:
                        break;
                }
            }
//            if (content != null) {
//                 这里是用户向公众号发送消息的业务
//            }
        }
        return WxUtil.mapToXml(resultMap);
    }

    @Override
    public AccessToken getToken(String appid, String appSecrect) {
        HashMap<String, String> parms = new HashMap<>();
        parms.put("grant_type", "client_credential");
        parms.put("appid", appid);
        parms.put("secret", appSecrect);
        String s = HttpUtil.doGet(GETACCESSTOKENURL, parms);
        return JsonUtil.fromJson(s, AccessToken.class);
    }

    @Override
    public JSONObject getUserInfoByOpenId(String openid) {
        JSONObject userInfo = (JSONObject) redisTemplate.opsForValue().get(USERINFOPREFIX + openid);
        if (userInfo == null) {
            String accesstoken = stringRedisTemplate.opsForValue().get(ACCESSTOKENKEY);
            HashMap<String, String> parms = new HashMap<>();
            parms.put("access_token", accesstoken);
            parms.put("openid", openid);
            parms.put("lang", "zh_CN");
            String doGet = HttpUtil.doGet(URLGETUSERINFO, parms);
            userInfo = JSON.parseObject(doGet);
            // 将查询到的用户信息放入缓存
            redisTemplate.opsForValue().set(USERINFOPREFIX + openid, userInfo, 60L, TimeUnit.MINUTES);
        }
        return userInfo;
    }

    @Override
    public RespDto checkLogin(String ticket) {
        String openId = stringRedisTemplate.opsForValue().get(LOGINTICKET + ticket);
        if (openId == null || "".equals(openId)) return new RespDto(200, "未登录", null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject userInfoByOpenId = getUserInfoByOpenId(openId);
        pushMessage(openId, userInfoByOpenId.getString("nickname"), sdf.format(new Date()));
        stringRedisTemplate.delete(LOGINTICKET + ticket);
        return new RespDto<>(200, "登陆成功", userInfoByOpenId);
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
                // TODO 到时候需要更换成自己的模板 ID
                .templateId("O3OiVR3tfPd-DMMqMKLPgS-l6ihtCq9BYIyimLEl6iQ")
                //点击模板消息要访问的网址
                //TODO 到时候换成拼接的 URL----最好是作为参数传进来
                .url("www.baidu.com")
                .build();
        //3,如果是正式版发送消息，，这里需要配置你的信息
        for (int i = 0; i < values.length; i++) {
            // 这里这个“name” 是我的模板消息配置了这个参数，真实的情况请切换成自己的模板消息参数，可变参数 Objects...也请切换
            templateMessage.addData(new WxMpTemplateData(String.valueOf(i + 1), values[i], "#173177"));
        }
        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

