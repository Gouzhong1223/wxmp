package com.gouzhong1223.wxmptest.entity;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 前端请求创建二维码的接收参数实体
 * @Date : create by QingSong in 2020-10-25 17:46
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.entity
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
public class WxQrCodeForm {

    @NotEmpty(message = "场景值ID不得为空")
    @Size(min = 1, max = 64, message = "场景值长度限制为1到64")
    private String sceneStr;

    @Max(value = 2592000, message = "过期时间不得超过30天")
    private Integer expireSeconds;

    private Boolean isTemp = true;

    private String worksUrl;

    private String worksName;

    private String qRInfo;
}
