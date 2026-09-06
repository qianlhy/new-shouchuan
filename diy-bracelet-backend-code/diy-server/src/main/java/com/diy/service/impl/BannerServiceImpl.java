package com.diy.service.impl;

import com.diy.entity.Banner;
import com.diy.mapper.BannerMapper;
import com.diy.service.BannerService;
import com.diy.utils.WxCloudStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;
    
    @Autowired
    private WxCloudStorageUtil wxCloudStorageUtil;

    /**
     * 查询轮播图列表
     */
    @Override
    public List<Banner> list() {
        log.info("查询轮播图列表");
        return bannerMapper.list();
    }

    @Override
    public void add(Banner banner) {
        log.info("新增轮播图: {}", banner);
        bannerMapper.insert(banner);
    }

    @Override
    public void update(Banner banner) {
        log.info("更新轮播图: {}", banner);
        // 如果更新时传入了新图片地址且与旧值不同，则删除旧图片
        if (banner.getId() != null) {
            Banner old = bannerMapper.getById(banner.getId());
            if (old != null) {
                String oldImage = old.getImageUrl();
                String newImage = banner.getImageUrl();
                if (oldImage != null && !oldImage.isEmpty()
                        && newImage != null && !newImage.isEmpty()
                        && !oldImage.equals(newImage)) {
                    log.info("删除旧轮播图图片: {}", oldImage);
                    wxCloudStorageUtil.delete(oldImage);
                }
            }
        }
        bannerMapper.update(banner);
    }

    @Override
    public void delete(Long id) {
        log.info("删除轮播图: id={}", id);
        // 删除数据库记录前先删除图片文件
        Banner old = bannerMapper.getById(id);
        if (old != null && old.getImageUrl() != null && !old.getImageUrl().isEmpty()) {
            log.info("删除轮播图图片文件: {}", old.getImageUrl());
            wxCloudStorageUtil.delete(old.getImageUrl());
        }
        bannerMapper.delete(id);
    }
}
