package com.atguigu.gmall.auth.service;


import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    String accreditAction(String username, String password);
}
