package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.UserMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.user.User;
import com.qingcheng.service.order.OrderItemService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.user.UserService;
import com.qingcheng.util.BCrypt;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service(interfaceClass = UserService.class)
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    RedisTemplate redisTemplate;
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @Reference
    OrderItemService orderItemService;
    
    @Reference
    OrderService orderService;
    
    /**
     * 密码修改
     *
     * @param passwordMap 密码集合
     */
    @Override
    public void edit(Map<String, String> passwordMap) {
        //取出密码和数据库中的密码对比
        String oldPassword = passwordMap.get("oldPassword");
        String username = passwordMap.get("username");
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        List<User> users = userMapper.selectByExample(example);
        for (User user : users) {
            if (!user.getPassword().equals(oldPassword)) {
                throw new RuntimeException("与原密码不一致,请重新输入");
            }
            //密码正确，将新密码加盐
            String gensalt = BCrypt.gensalt();
            BCrypt.hashpw(passwordMap.get("newpass"), gensalt);
            user.setPassword(oldPassword);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }
    
    /**
     * 根据用户名查询个人信息
     *
     * @param username 前端传入的用户名
     * @return 用户信息
     */
    @Override
    public Map<String, String> getUser(String username) {
        List<Map<String, String>> mapList = userMapper.getUser(username);
        return mapList.get(0);
    }
    
    /**
     * 增加用户
     *
     * @param user    用户信息
     * @param smsCode 验证码
     */
    @Override
    public void add(User user, String smsCode) {
        String sysCode = (String) redisTemplate.boundValueOps("code_" + user.getPhone()).get();
        if (sysCode == null) {
            throw new RuntimeException("验证码未发送或已过期");
        }
        if (!smsCode.equals(sysCode)) {
            throw new RuntimeException("验证码不正确");
        }
        if (user.getUsername() == null) {
            user.setUsername(user.getPhone());
        }
        User searchUser = new User();
        searchUser.setUsername(user.getUsername());
        if (userMapper.selectCount(searchUser) > 0) {
            throw new RuntimeException("该手机号已注册");
        }
        //保存账户
        user.setCreated(new Date());
        user.setUpdated(new Date());
        user.setPoints(0);
        user.setStatus("1");
        user.setIsEmailCheck("0");
        user.setIsMobileCheck("1");
        
        userMapper.insert(user);
    }
    
    /**
     * 发送短信验证码
     *
     * @param phone 手机号码
     */
    @Override
    public void sendSms(String phone) {
        //产生6位随机验证码
        int max = 999999;
        int min = 100000;
        Random random = new Random();
        int code = random.nextInt(max);
        if (code < min) {
            code = min + code;
        }
        //保存到redis中
        redisTemplate.boundValueOps("code_" + phone).set(code + "");
        redisTemplate.boundValueOps("code_" + phone).expire(5, TimeUnit.MINUTES); //有限时间5分钟
        
        //发送给mq
        Map<String, String> map = new HashMap<>();
        map.put("code", code + "");
        map.put("phone", phone);
        rabbitTemplate.convertAndSend("", "queue.sms", JSON.toJSONString(map));
    }
    
    /**
     * 返回全部记录
     *
     * @return
     */
    @Override
    public List<User> findAll() {
        return userMapper.selectAll();
    }
    
    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<User> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<User> users = (Page<User>) userMapper.selectAll();
        return new PageResult<User>(users.getTotal(), users.getResult());
    }
    
    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<User> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return userMapper.selectByExample(example);
    }
    
    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<User> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<User> users = (Page<User>) userMapper.selectByExample(example);
        return new PageResult<User>(users.getTotal(), users.getResult());
    }
    
    /**
     * 新增
     *
     * @param user
     */
    @Override
    public void add(User user) {
        userMapper.insert(user);
    }
    
    /**
     * 修改用户中心
     * @param userMap
     */
    @Override
    @Transactional
    @SuppressWarnings("all")
    public void update(Map<String, String> userMap) {
        try {
            //取出用户信息
            String nickName = userMap.get("nickName");
            String username = userMap.get("username");
            String birthday = userMap.get("birthday");
            String province = userMap.get("province");
            String city = userMap.get("city");
            String area = userMap.get("area");
            String phone = userMap.get("phone");
            String email = userMap.get("email");
            //先根据用户输入的省信息找出相应的id信息
            Map<String, String> provincialMap = userMapper.findByProvincial(province, city, area).get(0);
            //修改个人信息
            Date time = new Date();
            userMapper.update(username, nickName, birthday, provincialMap.get("provinceid"), provincialMap.get("cityid"), provincialMap.get("areaid"), phone, email, time);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
    
    /**
     * 删除
     *
     * @param username
     */
    @Override
    public void delete(String username) {
        userMapper.deleteByPrimaryKey(username);
    }
    
    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 用户名
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
// 密码，加密存储
            if (searchMap.get("password") != null && !"".equals(searchMap.get("password"))) {
                criteria.andLike("password", "%" + searchMap.get("password") + "%");
            }
// 注册手机号
            if (searchMap.get("phone") != null && !"".equals(searchMap.get("phone"))) {
                criteria.andLike("phone", "%" + searchMap.get("phone") + "%");
            }
// 注册邮箱
            if (searchMap.get("email") != null && !"".equals(searchMap.get("email"))) {
                criteria.andLike("email", "%" + searchMap.get("email") + "%");
            }
// 会员来源：1:PC，2：H5，3：Android，4：IOS
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andLike("sourceType", "%" + searchMap.get("sourceType") + "%");
            }
// 昵称
            if (searchMap.get("nickName") != null && !"".equals(searchMap.get("nickName"))) {
                criteria.andLike("nickName", "%" + searchMap.get("nickName") + "%");
            }
// 真实姓名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
// 使用状态（1正常 0非正常）
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andLike("status", "%" + searchMap.get("status") + "%");
            }
// 头像地址
            if (searchMap.get("headPic") != null && !"".equals(searchMap.get("headPic"))) {
                criteria.andLike("headPic", "%" + searchMap.get("headPic") + "%");
            }
// QQ号码
            if (searchMap.get("qq") != null && !"".equals(searchMap.get("qq"))) {
                criteria.andLike("qq", "%" + searchMap.get("qq") + "%");
            }
// 手机是否验证 （0否  1是）
            if (searchMap.get("isMobileCheck") != null && !"".equals(searchMap.get("isMobileCheck"))) {
                criteria.andLike("isMobileCheck", "%" + searchMap.get("isMobileCheck") + "%");
            }
// 邮箱是否检测（0否  1是）
            if (searchMap.get("isEmailCheck") != null && !"".equals(searchMap.get("isEmailCheck"))) {
                criteria.andLike("isEmailCheck", "%" + searchMap.get("isEmailCheck") + "%");
            }
// 性别，1男，0女
            if (searchMap.get("sex") != null && !"".equals(searchMap.get("sex"))) {
                criteria.andLike("sex", "%" + searchMap.get("sex") + "%");
            }
// 会员等级
            if (searchMap.get("userLevel") != null) {
                criteria.andEqualTo("userLevel", searchMap.get("userLevel"));
            }
// 积分
            if (searchMap.get("points") != null) {
                criteria.andEqualTo("points", searchMap.get("points"));
            }
// 经验值
            if (searchMap.get("experienceValue") != null) {
                criteria.andEqualTo("experienceValue", searchMap.get("experienceValue"));
            }
            
        }
        return example;
    }
}
