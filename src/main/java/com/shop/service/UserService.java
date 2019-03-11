package com.shop.service;

import com.shop.common.ServerResponse;
import com.shop.pojo.User;
import org.springframework.stereotype.Service;

/**
 * @author elian
 * @create 2019-03-09 22:03
 * @desc 用户登陆接口
 **/

public interface UserService {

    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String string, String type);

    ServerResponse selectQuestion(String username);

    ServerResponse<String> selectAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);

    ServerResponse<User> updateUserInfo(User user);

    ServerResponse<User> getInformation(Integer userId);

    ServerResponse checkAdminRole(User user);
}
