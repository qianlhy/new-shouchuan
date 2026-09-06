package com.diy.service;

import com.diy.vo.MemberInfoVO;

public interface MemberService {

    /**
     * 获取当前登录用户的会员信息（由订单实付金额动态计算）
     */
    MemberInfoVO getCurrentMemberInfo();
}
