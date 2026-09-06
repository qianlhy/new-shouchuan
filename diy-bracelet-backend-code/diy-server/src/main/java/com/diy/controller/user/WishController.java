package com.diy.controller.user;

import com.diy.dto.WishDTO;
import com.diy.result.Result;
import com.diy.service.ProductWishService;
import com.diy.vo.WishStatusVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/wish")
@Api(tags = "C端-心愿众筹")
@Slf4j
public class WishController {

    @Autowired
    private ProductWishService productWishService;

    @PostMapping("/toggle")
    @ApiOperation("切换想要状态")
    public Result<WishStatusVO> toggle(@RequestBody WishDTO wishDTO) {
        log.info("切换想要状态: productId={}", wishDTO.getProductId());
        return Result.success(productWishService.toggle(wishDTO.getProductId()));
    }

    @GetMapping("/counts")
    @ApiOperation("批量查询众筹进度（公开）")
    public Result<List<WishStatusVO>> counts(@RequestParam("ids") String ids) {
        List<Long> idList = new ArrayList<>();
        if (ids != null && !ids.trim().isEmpty()) {
            for (String s : ids.split(",")) {
                if (!s.trim().isEmpty()) {
                    try {
                        idList.add(Long.valueOf(s.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return Result.success(productWishService.counts(idList));
    }

    @GetMapping("/mine")
    @ApiOperation("我想要的商品列表（我的收藏）")
    public Result<List<Map<String, Object>>> mine() {
        return Result.success(productWishService.myWishes());
    }
}
