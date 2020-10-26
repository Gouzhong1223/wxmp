package com.gouzhong1223.wxmptest.controller;

import com.gouzhong1223.wxmptest.config.WechatConfig;
import com.gouzhong1223.wxmptest.dto.RespDto;
import com.gouzhong1223.wxmptest.entity.AccessToken;
import com.gouzhong1223.wxmptest.entity.WorkInfo;
import com.gouzhong1223.wxmptest.entity.WxQrCodeForm;
import com.gouzhong1223.wxmptest.service.QrCodeService;
import com.gouzhong1223.wxmptest.service.WxMpService;
import com.gouzhong1223.wxmptest.util.WxUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 请求控制器
 * @Date : create by QingSong in 2020-10-25 15:29
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.controller
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@RestController
@RequestMapping("wx")
@Slf4j
public class WxController {

    private final static String ACCESSTOKENKEY = "accesstoken";

    private final WxMpService wxMpService;
    private final QrCodeService qrCodeService;
    private final WechatConfig wechatConfig;
    private final RedisTemplate<String, HashMap<String, WorkInfo>> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public WxController(WxMpService wxMpService, QrCodeService qrCodeService,
                        WechatConfig wechatConfig, RedisTemplate redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.wxMpService = wxMpService;
        this.qrCodeService = qrCodeService;
        this.wechatConfig = wechatConfig;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/mp")
    @ApiOperation(value = "微信进行 URL 认证的路径")
    @ApiParam(name = "request", value = "这是格式为 XML 的 String 消息，" +
            "请求内容格式格式参见 <a href=https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Access_Overview.html></a>")
    public void checkUrl(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        wxMpService.auth(request, response);
    }


    @PostMapping("/mp")
    @ApiOperation(value = "所有微信的消息请求都会发送到这里来")
    @ApiParam(name = "request", value = "这是格式为 XML 的 String 消息，" +
            "详细的内容格式参见 <a href=https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_standard_messages.html></a>")
    public String handler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        // 解析 XML 为 Map
        Map<String, String> parseXml = WxUtil.parseXml(request);
        return wxMpService.handler(parseXml);

    }

    @ApiOperation("获取二维码地址")
    @PostMapping("/createTicket")
    public RespDto createQrCodeTicket(@RequestBody WxQrCodeForm wxQrCodeForm) {
        // 先到缓存里面去取 accesstoken
        String accesstoken = stringRedisTemplate.opsForValue().get("accesstoken");
        if (accesstoken == null || accesstoken == "") {
            // 没有的话，就请求微信服务器获取accesstoken，并放入缓存
            AccessToken token = wxMpService.getToken(wechatConfig.getAppId(), wechatConfig.getAppsecret());
            accesstoken = token.getAccess_token();
            stringRedisTemplate.opsForValue().set(ACCESSTOKENKEY, token.getAccess_token(), 7200L, TimeUnit.SECONDS);
        }

        String tempTicket = qrCodeService.createTempTicket(accesstoken, wxQrCodeForm.getSceneStr(), wxQrCodeForm.getExpireSeconds(), wxQrCodeForm.getQRInfo());
        HashMap<String, WorkInfo> map = new HashMap<>();
        map.put(tempTicket, new WorkInfo(wxQrCodeForm.getWorksUrl(), wxQrCodeForm.getWorksName()));
        redisTemplate.opsForValue().set(tempTicket, map, 600L, TimeUnit.SECONDS);
        String qrCodeUrl = qrCodeService.showQrCode(accesstoken, tempTicket, false);
        HashMap<String, String> result = new HashMap<>();
        result.put("ticket", tempTicket);
        result.put("qrCodeUrl", qrCodeUrl);
        return new RespDto(200, "成功", result);
    }

    @ApiOperation("检查当前二维码是否有用户已经扫描并且登录")
    @PostMapping("/checkLogin")
    public RespDto checkLogin(@RequestBody String ticket) {
        return wxMpService.checkLogin(ticket);
    }


}
