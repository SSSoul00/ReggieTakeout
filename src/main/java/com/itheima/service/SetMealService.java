package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDto;

public interface SetMealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
}
