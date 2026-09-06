package com.diy.controller.user;

import com.diy.entity.Product;
import com.diy.result.Result;
import com.diy.service.ProductService;
import com.diy.vo.ProductDetailVO;
import com.diy.vo.ProductListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController("userProductController")
@RequestMapping("/user/product")
@Api(tags = "C端-商品接口")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 根据分类ID查询商品列表
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询商品列表")
    public Result<ProductListVO> listByCategoryId(
            @ApiParam("分类ID") @RequestParam Long categoryId) {
        log.info("根据分类ID查询商品列表: categoryId={}", categoryId);
        
        List<Product> products = productService.listByCategoryId(categoryId);
        
        // 转换为VO
        List<ProductListVO.ProductItem> productItems = products.stream().map(product -> {
            ProductListVO.ProductItem item = new ProductListVO.ProductItem();
            BeanUtils.copyProperties(product, item);
            return item;
        }).collect(Collectors.toList());
        
        ProductListVO productListVO = ProductListVO.builder()
                .products(productItems)
                .build();
        
        return Result.success(productListVO);
    }
    
    /**
     * 根据ID查询商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail")
    @ApiOperation("根据ID查询商品详情")
    public Result<ProductDetailVO> getById(
            @ApiParam("商品ID") @RequestParam Long id) {
        log.info("根据ID查询商品详情: id={}", id);
        
        Product product = productService.getById(id);
        
        // 转换为VO
        ProductDetailVO.ProductItem item = new ProductDetailVO.ProductItem();
        BeanUtils.copyProperties(product, item);
        item.setDetailImages(product.getImages());
        
        ProductDetailVO productDetailVO = ProductDetailVO.builder()
                .product(item)
                .build();
        
        return Result.success(productDetailVO);
    }
}