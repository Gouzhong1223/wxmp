package com.gouzhong1223.wxmptest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : 返回给前端的实体类
 * @Date : create by QingSong in 2020-10-26 13:03
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.dto
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespDto<T> {
    private Integer code;
    private String message;
    private T data;
}
