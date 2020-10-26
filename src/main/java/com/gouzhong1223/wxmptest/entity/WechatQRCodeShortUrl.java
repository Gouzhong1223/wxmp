package com.gouzhong1223.wxmptest.entity;

import com.google.gson.annotations.SerializedName;
import com.gouzhong1223.wxmptest.result.ResultState;
import lombok.Data;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 二维码短连接实体
 * @Date : create by QingSong in 2020-10-25 16:48
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.entity
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
public class WechatQRCodeShortUrl extends ResultState {
    private static final long serialVersionUID = -2491236254633842526L;

    @SerializedName("short_url")
    private String shortUrl; //短链接
}
