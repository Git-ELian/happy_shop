package com.shop.controller.backed;

import com.shop.common.Const;
import com.shop.common.ServerResponse;
import com.shop.pojo.User;
import com.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author elian
 * @create 2019-03-10 22:14
 * @desc
 **/
@Controller
@RequestMapping("/manage/user/")
public class UserManageController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(HttpSession session, String username, String password) {
        ServerResponse<User> login = userService.login(username, password);
        if (login.isSuccess()) {
            User user = login.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
//                说明登陆的事管理员
                session.setAttribute(Const.CURRENT_USER, user);
                return login;
            } else {
                return ServerResponse.createByErrorMessage("不是系统管理员，无法登陆");
            }
        }

        return login;
    }

}
