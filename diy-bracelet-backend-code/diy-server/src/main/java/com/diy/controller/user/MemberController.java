package com.diy.controller.user;

import com.diy.result.Result;
import com.diy.service.MemberService;
import com.diy.vo.MemberInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/member")
@Api(tags = "C端-会员中心")
@Slf4j
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/info")
    @ApiOperation("获取当前用户会员信息")
    public Result<MemberInfoVO> info() {
        return Result.success(memberService.getCurrentMemberInfo());
    }
}
