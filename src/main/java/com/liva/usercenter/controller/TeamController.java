package com.liva.usercenter.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liva.usercenter.comment.BaseResponse;
import com.liva.usercenter.comment.ErrorCode;
import com.liva.usercenter.comment.R;
import com.liva.usercenter.exception.BusinessException;
import com.liva.usercenter.model.domain.Team;
import com.liva.usercenter.model.domain.User;
import com.liva.usercenter.model.request.TeamAddRequest;
import com.liva.usercenter.model.request.TeamQuery;
import com.liva.usercenter.model.request.UserLoginRequest;
import com.liva.usercenter.model.request.UserRegisterRequest;
import com.liva.usercenter.service.TeamService;
import com.liva.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍控制层接口
 *
 * @author liva
 */

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://127.0.0.1:5173/",allowCredentials="true",allowedHeaders = "*")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest , HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.currentUser(request);
        Team team = new Team();
        BeanUtil.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team, user);

        return R.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Long> deleteTeam(@RequestBody Team team){
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean success = teamService.removeById(team);
        if (!success){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return R.success(team.getId());
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team){
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean success = teamService.updateById(team);
        if (!success){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return R.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam("id") Long id){
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询失败");
        }
        return R.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<Team>> ListTeams(TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtil.copyProperties(teamQuery,team);
        //qw创建后直接按照我们team的特征包装
        LambdaQueryWrapper<Team> qw = new LambdaQueryWrapper<>(team);
        List<Team> list = teamService.list(qw);

        if (list == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询失败");
        }
        return R.success(list);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> ListTeamsByPages(TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtil.copyProperties(teamQuery,team);
        //qw创建后直接按照我们team的特征包装
        LambdaQueryWrapper<Team> qw = new LambdaQueryWrapper<>(team);

        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Page<Team> teamPage = teamService.page(page, qw);

        if (teamPage == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询失败");
        }
        return R.success(teamPage);
    }

}
