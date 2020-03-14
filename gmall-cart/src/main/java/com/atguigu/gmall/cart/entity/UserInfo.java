package com.atguigu.gmall.cart.entity;

import lombok.Data;

@Data
public class UserInfo {
     private Long userId; //用户id
     private String userKey; // 游客的TOKEN
}
