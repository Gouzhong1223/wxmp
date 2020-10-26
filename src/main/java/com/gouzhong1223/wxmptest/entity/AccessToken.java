package com.gouzhong1223.wxmptest.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : AccessToken封装
 * @Date : create by QingSong in 2020-10-25 18:41
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.entity
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
public class AccessToken implements Serializable {

    private static final long serialVersionUID = 41546543215642313L;
    private String access_token;

    private Long expires_in;
}
