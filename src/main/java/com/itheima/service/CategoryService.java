package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.controller.R;
import com.itheima.entity.Category;

public interface CategoryService extends IService<Category> {
    void removeById(Long id);
}
