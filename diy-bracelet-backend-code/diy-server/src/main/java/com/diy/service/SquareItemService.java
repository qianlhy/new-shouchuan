package com.diy.service;

import com.diy.dto.SquareSubmitDTO;
import com.diy.entity.SquareItem;
import com.diy.result.PageResult;

import java.util.List;

public interface SquareItemService {

    Long submit(SquareSubmitDTO dto, Long userId);

    /** channel: square | recommend | mine */
    List<SquareItem> listVisible(String channel);

    SquareItem getById(Long id);

    PageResult page(Integer page, Integer pageSize, Integer status, Integer showScope, String title);

    void updateStatus(Long id, Integer status);

    void updateMeta(Long id, String title, Integer sort, Integer status, Integer showScope);

    void delete(Long id);
}
