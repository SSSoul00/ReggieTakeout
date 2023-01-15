package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.SMSUtils;
import com.itheima.common.ValidateCodeUtils;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送验证码接口
     * @param user
     * @param session
     * @return
     * @throws Exception
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws Exception {
        String phone = user.getPhone();
        if (phone!=null){
            //获取验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info(code);
            //发送验证码
//            SMSUtils.sendMessage(phone,code);
            //将验证码保存
            session.setAttribute(phone,code);
            return R.success("短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 登录接口
     * @param map
     * @param session
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) throws Exception {
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //获取Session中验证码
        String code2 = session.getAttribute(phone).toString();
        //比对验证码
        if (code2!=null&&code.equals(code2)){
            //如果通过
            //判断是否是新用户,如果是则进行注册
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper();
            qw.eq(User::getPhone,phone);
            User user = userService.getOne(qw);
            if (user==null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("用户不能存在");
    }
}
