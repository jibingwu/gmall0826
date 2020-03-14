package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.MemberExeption;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberDao memberDaoL;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> memberWrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                memberWrapper.eq("username", data);
                break;
            case 2:
                memberWrapper.eq("mobile", data);
                break;
            case 3:
                memberWrapper.eq("email", data);
                break;
            default:
                return null;
        }


        return this.memberDaoL.selectCount(memberWrapper) == 0;
    }


    @Override
    public void register(MemberEntity memberEntity, String code) {

        // - 1）校验短信验证码

        //      - 2）生成盐
        String salt = UUID.randomUUID().toString().substring(0, 5);
        memberEntity.setSalt(salt);
        //   - 3）对密码加密
        String saltAndPassword = DigestUtils.md5Hex( memberEntity.getPassword()+salt);
        //保存到对象里面
        memberEntity.setPassword(saltAndPassword);
        //设置注册时间
        memberEntity.setCreateTime(new Date());
        memberEntity.setStatus(1);
        memberEntity.setLevelId(0L);
        //  - 4）写入数据库
        boolean flag = this.save(memberEntity);

        // - 5）删除Redis中的验证码

    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        // 1.先根据用户名查询
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));

        // 2.判断，如果为空，则用户名输入有误
        if (memberEntity == null) {
            throw new MemberExeption("用户名输入有误！");
        }

        // 3.获取用户的盐，对用户输入的密码加盐加密
        password = DigestUtils.md5Hex(password + memberEntity.getSalt());
        String salt = memberEntity.getSalt();
        String password1 = memberEntity.getPassword();


        System.out.println(salt+"-------"+password1);
        System.out.println(" 密码加盐 " +password );


        // 4.比较，如果不一样说明密码有误
        if (!StringUtils.equals(password, memberEntity.getPassword())) {
            throw new MemberExeption("密码输入错误！");
        }
        return memberEntity;

    }
}