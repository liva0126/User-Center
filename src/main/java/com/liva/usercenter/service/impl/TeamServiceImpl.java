package com.liva.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liva.usercenter.comment.ErrorCode;
import com.liva.usercenter.enums.TeamStatusEnum;
import com.liva.usercenter.exception.BusinessException;
import com.liva.usercenter.model.domain.Team;
import com.liva.usercenter.model.domain.User;
import com.liva.usercenter.model.domain.UserTeam;
import com.liva.usercenter.service.TeamService;
import com.liva.usercenter.mapper.TeamMapper;
import com.liva.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
 * @author liva
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2022-10-15 16:33:18
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Override
    public long addTeam(Team team, User loginUser) {

        //请求参数判断
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断用户是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //校验信息
        //队伍人数在1-20间
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(1);
        if (maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数错误");
        }
        //队伍标题小于20字
        if (StringUtils.isEmpty(team.getName()) || team.getName().length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名不合法");
        }
        //描述小于512
        if (StringUtils.isNotEmpty(team.getDescription()) && team.getDescription().length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //status 状态是否公开 不传默认为0（不公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态错误");
        }
        //如果为加密状态，则密码必须有且合法
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum) && (StringUtils.isEmpty(password) || password.length() >32 )){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不正确");
        }
        //判断过期时间
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"过期");
        }

        //判断用户创建的队伍是否超过5个
        //todo 有bug，用户可能同时疯狂点 创建100个队伍
        LambdaQueryWrapper<Team> qw = new LambdaQueryWrapper<>();
        qw.eq(Team::getUserId,loginUser.getId());
        int count = count(qw);
        if (count >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户创建的队伍超过5个");
        }

        TeamService proxy = (TeamService)AopContext.currentProxy();
        return  proxy.saveTeam(team, loginUser);
    }

    @Transactional
    public long saveTeam(Team team, User loginUser) {
        //插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(loginUser.getId());
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userTeam.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return team.getId();
    }
}




