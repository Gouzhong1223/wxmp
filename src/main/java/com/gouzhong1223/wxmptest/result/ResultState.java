package com.gouzhong1223.wxmptest.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2020-10-25 16:47
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.result
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
public class ResultState implements Serializable {
    private static final long serialVersionUID = -6184155678037435926L;

    private int errcode;

    private String errmsg;
}
