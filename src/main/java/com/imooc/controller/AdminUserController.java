package com.imooc.controller;

import com.imooc.config.ProjectUrlConfig;
import com.imooc.constant.CookieConstant;
import com.imooc.constant.RedisConstant;
import com.imooc.dataobject.SellerInfo;
import com.imooc.enums.ResultEnum;
import com.imooc.exception.SellException;
import com.imooc.service.SellerService;
import com.imooc.utils.CookieUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 卖家用户
 * Created by 廖师兄
 * 2017-07-30 15:28
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminUserController {

    @Autowired
    private SellerService sellerService;


    @Autowired
    private ProjectUrlConfig projectUrlConfig;


    @GetMapping("/loginAdmin")
    public String loginAdmin(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("openid") String openid,
                             HttpServletResponse response) {
        log.info("name={}", username);
        //1. openid去和数据库里的数据匹配
        SellerInfo sellerInfo = sellerService.findSellerInfoByOpenid(openid);
        if (sellerInfo == null) {
            throw new SellException(ResultEnum.LOGIN_FAIL);
        }

        //2. 设置token至redis
        String token = UUID.randomUUID().toString();
        log.info("登录成功的token={}", token);
        Integer expire = RedisConstant.EXPIRE;

        //        redisTemplate.opsForValue().set(String.format(RedisConstant.TOKEN_PREFIX, token), openid, expire, TimeUnit.SECONDS);

        //3. 设置token至cookie
        CookieUtil.set(response, CookieConstant.TOKEN, token, expire);
        return "登录成功";
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request,
                               HttpServletResponse response,
                               Map<String, Object> map) {
        //1. 从cookie里查询
        Cookie cookie = CookieUtil.get(request, CookieConstant.TOKEN);
        if (cookie != null) {
            //2. 清除cookie
            CookieUtil.set(response, CookieConstant.TOKEN, null, 0);
        }
        map.put("msg", ResultEnum.LOGOUT_SUCCESS.getMessage());
        map.put("url", "/sell/seller/order/list");
        return new ModelAndView("common/success", map);
    }
}
