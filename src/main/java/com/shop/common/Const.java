package com.shop.common;

/**
 * @author elian
 * @create 2019-03-10 0:01
 * @desc
 **/
public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";

    public static final String USERNAME = "username";

    public interface Role {
        //        普通用户
        int ROLE_CUSTOMER = 0;

        int ROLE_ADMIN = 1;
//        管理员
    }
}
