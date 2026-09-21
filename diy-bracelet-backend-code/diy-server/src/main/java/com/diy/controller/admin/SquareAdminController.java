package com.diy.controller.admin;

import com.diy.entity.SquareItem;
import com.diy.result.PageResult;
import com.diy.result.Result;
import com.diy.service.SquareItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("adminSquareController")
@RequestMapping("/admin/square")
@Api(tags = "管理端-灵感广场")
@Slf4j
public class SquareAdminController {

    @Autowired
    private SquareItemService squareItemService;

    @GetMapping("/page")
    @ApiOperation("分页查询广场作品")
    public Result<PageResult> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer showScope,
            @RequestParam(required = false) String title) {
        return Result.success(squareItemService.page(page, pageSize, status, showScope, title));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("作品详情")
    public Result<SquareItem> detail(@PathVariable Long id) {
        SquareItem item = squareItemService.getById(id);
        if (item == null) {
            return Result.error("作品不存在");
        }
        return Result.success(item);
    }

    @PutMapping("/status")
    @ApiOperation("设置展示/隐藏")
    public Result<String> updateStatus(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") != null ? Long.valueOf(body.get("id").toString()) : null;
        Integer status = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null;
        squareItemService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/update")
    @ApiOperation("更新标题/排序/状态/展示范围")
    public Result<String> update(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") != null ? Long.valueOf(body.get("id").toString()) : null;
        String title = body.get("title") != null ? body.get("title").toString() : null;
        Integer sort = body.get("sort") != null ? Integer.valueOf(body.get("sort").toString()) : null;
        Integer status = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null;
        Integer showScope = body.get("showScope") != null ? Integer.valueOf(body.get("showScope").toString()) : null;
        squareItemService.updateMeta(id, title, sort, status, showScope);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除广场作品")
    public Result<String> delete(@PathVariable Long id) {
        squareItemService.delete(id);
        return Result.success();
    }
}
