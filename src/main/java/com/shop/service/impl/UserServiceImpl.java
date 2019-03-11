package com.shop.service.impl;

import com.shop.common.Const;
import com.shop.common.ServerResponse;
import com.shop.common.TokenCache;
import com.shop.dao.UserMapper;
import com.shop.pojo.User;
import com.shop.service.UserService;
import com.shop.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author elian
 * @create 2019-03-09 22:04
 * @desc
 **/
@Service("userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
//TODO        密码登陆MD5
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse checkValid = this.checkValid(user.getUsername(), Const.USERNAME);

        if (!checkValid.isSuccess()) {
            return checkValid;
        }

        checkValid = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!checkValid.isSuccess()) {
            return checkValid;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
//        MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int checkUsername = userMapper.insert(user);
        if (checkUsername == 0) {
            ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String string, String type) {
        if (StringUtils.isNotBlank(type)) {
//            开始校验
            if (Const.USERNAME.equals(type)) {
                int checkUsername = userMapper.checkUsername(string);
                if (checkUsername > 0) {
                    return ServerResponse.createByErrorMessage("用户已经存在");
                }
            }

        }
        if (Const.EMAIL.equals(type)) {
            int checkEmail = userMapper.checkEmail(type);
            if (checkEmail > 0) {
                return ServerResponse.createByErrorMessage("邮件已经存在");
            }
        } else {
            return ServerResponse.createByErrorMessage("参数校验错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse selectQuestion(String username) {
        ServerResponse valid = this.checkValid(username, Const.USERNAME);
        if (valid.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> selectAnswer(String username, String question, String answer) {
        int checkAnswer = userMapper.checkAnswer(username, question, answer);
        if (checkAnswer > 0) {
//            说明问题及问题答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
/*        防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户
                因为我们会指定一个count(1),如果不指定id，那么结果就是true啦 count>0*/
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int update = userMapper.updateByPrimaryKeySelective(user);
        if (update > 0) {
            ServerResponse.createBySuccessMessage("面更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user) {
//        username是不能被更新的
//        email也要进行检验，检验新的email是否已经存在。
//        并且存在的email如果相同，不能是我们当前的这个用户的
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已经存在，请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("个人信息更新成功");
        }
        return ServerResponse.createByErrorMessage("个人信息更新失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


//    backend

    /**
     * 检验是否是管理员
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
