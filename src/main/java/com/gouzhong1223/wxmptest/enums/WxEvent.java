package com.gouzhong1223.wxmptest.enums;

import lombok.Getter;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description :
 * @Date : create by QingSong in 2020-10-25 15:54
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.enums
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Getter
public enum WxEvent {

    DINGYUE("subscribe"),
    SAOMIAO("SCAN");
    private String wxEvent;

    WxEvent(String wxEvent) {
        this.wxEvent = wxEvent;
    }
}
