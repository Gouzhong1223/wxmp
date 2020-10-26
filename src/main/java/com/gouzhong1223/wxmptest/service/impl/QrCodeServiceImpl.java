package com.gouzhong1223.wxmptest.service.impl;

import com.google.gson.JsonObject;
import com.gouzhong1223.wxmptest.config.WechatConfig;
import com.gouzhong1223.wxmptest.constant.QRCodeConstant;
import com.gouzhong1223.wxmptest.entity.WechatQRCode;
import com.gouzhong1223.wxmptest.entity.WechatQRCodeShortUrl;
import com.gouzhong1223.wxmptest.service.QrCodeService;
import com.gouzhong1223.wxmptest.util.EncodeUtils;
import com.gouzhong1223.wxmptest.util.HttpUtil;
import com.gouzhong1223.wxmptest.util.JsonUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2020-10-25 16:19
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.service.impl
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Service
public class QrCodeServiceImpl implements QrCodeService {

    private final WechatConfig wechatConfig;
    private final RedisTemplate redisTemplate;

    public QrCodeServiceImpl(WechatConfig wechatConfig, RedisTemplate redisTemplate) {
        this.wechatConfig = wechatConfig;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String createTempTicket(String accessToken, int sceneId, int expireSeconds, String qRInfo) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        // output data
        JsonObject data = new JsonObject();
        data.addProperty("action_name", QRCodeConstant.QR_SCENE);
        data.addProperty("expire_seconds", expireSeconds);
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_id", sceneId);
        scene.addProperty("action_info", qRInfo);
        JsonObject actionInfo = new JsonObject();
        actionInfo.add("scene", scene);
        data.add("action_info", actionInfo);
        String result = HttpUtil.doPost(wechatConfig.getCreateTicketUrl(), params, data.toString());
        WechatQRCode qrcode = JsonUtil.fromJson(result, WechatQRCode.class);
        return qrcode == null ? null : qrcode.getTicket();
    }

    @Override
    public String createTempTicket(String accessToken, String sceneStr, int expireSeconds, String qRInfo) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        // output data
        JsonObject data = new JsonObject();
        data.addProperty("action_name", QRCodeConstant.QR_STR_SCENE);
        data.addProperty("expire_seconds", expireSeconds);
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_str", sceneStr);
        scene.addProperty("action_info", qRInfo);
        JsonObject actionInfo = new JsonObject();
        actionInfo.add("scene", scene);
        data.add("action_info", actionInfo);
        String result = HttpUtil.doPost(wechatConfig.getCreateTicketUrl(), params, data.toString());
        WechatQRCode qrcode = JsonUtil.fromJson(result, WechatQRCode.class);
        return qrcode == null ? null : qrcode.getTicket();
    }

    @Override
    public String createForeverTicket(String accessToken, int sceneId) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        // output data
        JsonObject data = new JsonObject();
        data.addProperty("action_name", QRCodeConstant.QR_LIMIT_SCENE);
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_id", sceneId);
        JsonObject actionInfo = new JsonObject();
        actionInfo.add("scene", scene);
        data.add("action_info", actionInfo);
        String result = HttpUtil.doPost(wechatConfig.getCreateTicketUrl(), params, data.toString());
        WechatQRCode qrcode = JsonUtil.fromJson(result, WechatQRCode.class);
        return qrcode == null ? null : qrcode.getTicket();
    }

    @Override
    public String createForeverTicket(String accessToken, String sceneStr) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        // output data
        JsonObject data = new JsonObject();
        data.addProperty("action_name", "QR_LIMIT_STR_SCENE");
        JsonObject actionInfo = new JsonObject();
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_str", sceneStr);
        actionInfo.add("scene", scene);
        data.add("action_info", actionInfo);
        String result = HttpUtil.doPost(wechatConfig.getCreateTicketUrl(), params, data.toString());
        WechatQRCode qrcode = JsonUtil.fromJson(result, WechatQRCode.class);
        return qrcode == null ? null : qrcode.getTicket();
    }


    @Override
    public String showQrCode(String accessToken, String ticket, boolean isShortUrl) {
        String url = String.format(wechatConfig.getShowQrcodeUrl(), EncodeUtils.urlEncode(ticket));
        if (isShortUrl) {
            return toShortQRCodeurl(accessToken, url);
        }
        return url;
    }

    /**
     * 长链接转短链接
     *
     * @param accessToken
     * @param longUrl     长链接
     * @return
     */
    private String toShortQRCodeurl(String accessToken, String longUrl) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        JsonObject data = new JsonObject();
        data.addProperty("action", QRCodeConstant.LONG_TO_SHORT);
        data.addProperty("long_url", longUrl);
        String result = HttpUtil.doPost(wechatConfig.getShortQrcodeUrl(),
                params, data.toString());
        WechatQRCodeShortUrl wechatQRCodeShortUrl = JsonUtil.fromJson(result, WechatQRCodeShortUrl.class);
        return wechatQRCodeShortUrl == null ? null : wechatQRCodeShortUrl.getShortUrl();
    }


}
