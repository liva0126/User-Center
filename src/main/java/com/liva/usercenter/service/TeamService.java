package com.liva.usercenter.service;

import com.liva.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liva.usercenter.model.domain.User;

/**
* @author liva
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2022-10-15 16:33:18
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    long saveTeam(Team team, User loginUser);
}
