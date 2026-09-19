package com.diy.service.impl;

import com.diy.entity.DiyMaterial;
import com.diy.mapper.DiyMaterialMapper;
import com.diy.result.PageResult;
import com.diy.service.DiyMaterialService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DiyMaterialServiceImpl implements DiyMaterialService {

    @Autowired
    private DiyMaterialMapper diyMaterialMapper;

    @Override
    public List<DiyMaterial> list(String categoryKey, String colorSeriesKey, String title, Integer limit) {
        List<String> categories = categoryKey != null && !categoryKey.isEmpty() ? Arrays.asList(categoryKey) : null;
        List<String> colorSeries = colorSeriesKey != null && !colorSeriesKey.isEmpty() ? Arrays.asList(colorSeriesKey) : null;
        String titleKey = title != null && !title.trim().isEmpty() ? title.trim() : null;
        return diyMaterialMapper.list(categories, colorSeries, titleKey, limit);
    }

    @Override
    public PageResult page(Integer page, Integer pageSize, String categoryKey, String colorSeriesKey, String title) {
        PageHelper.startPage(page, pageSize);
        List<String> categories = categoryKey != null && !categoryKey.isEmpty() ? Arrays.asList(categoryKey) : null;
        List<String> colorSeriesList = colorSeriesKey != null && !colorSeriesKey.isEmpty() ? Arrays.asList(colorSeriesKey) : null;
        String titleKey = title != null && !title.trim().isEmpty() ? title.trim() : null;
        // 分页时不要带 limit，交给 PageHelper
        Page<DiyMaterial> pageResult = (Page<DiyMaterial>) diyMaterialMapper.list(categories, colorSeriesList, titleKey, null);
        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    public DiyMaterial getById(Long id) {
        return diyMaterialMapper.getById(id);
    }

    @Override
    public void add(DiyMaterial material) {
        diyMaterialMapper.insert(material);
    }

    @Override
    public void update(DiyMaterial material) {
        diyMaterialMapper.update(material);
    }

    @Override
    public void delete(Long id) {
        diyMaterialMapper.delete(id);
    }

    @Override
    public void updateStatus(Integer status, List<Long> ids) {
        for (Long id : ids) {
            diyMaterialMapper.updateStatus(id, status);
        }
    }
}
