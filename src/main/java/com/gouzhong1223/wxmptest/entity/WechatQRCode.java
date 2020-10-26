package com.gouzhong1223.wxmptest.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 二维码实体
 * @Date : create by QingSong in 2020-10-25 16:24
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.entity
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
public class WechatQRCode {

    // 获取的二维码
    private String ticket;

    // 二维码的有效时间,单位为秒,最大不超过2592000（即30天）
    @SerializedName("expire_seconds")
    private int expireSeconds;

    // 二维码图片解析后的地址
    //由于测试无法访问这个，故直接通过ticket 显示并转换成短链接
    private String url;
}
