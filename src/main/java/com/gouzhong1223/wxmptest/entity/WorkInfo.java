package com.gouzhong1223.wxmptest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : TODO
 * @Date : create by QingSong in 2020-10-26 00:10
 * @Email : gouzhong1223@gmail.com
 * @Since : JDK 1.8
 * @PackageName : com.gouzhong1223.wxmptest.entity
 * @ProjectName : wxmptest
 * @Version : 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String workUrl;
    private String workName;
}
