package com.itheima.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.entity.SetmealDto;
import com.itheima.mapeer.SetMealMapper;
import com.itheima.service.SetMealService;
import com.itheima.service.SetmealDishiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {
    @Autowired
    private SetmealDishiService setmealDishiService;


    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //添加套餐
        this.save(setmealDto);
        //设置套餐ID
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long id = setmealDto.getId();
        setmealDishes.stream().map(setmealDish -> {
            setmealDish.setSetmealId(id);
            return setmealDish;
        }).collect(Collectors.toList());
        //添加套餐菜品关系
        setmealDishiService.saveBatch(setmealDishes);
    }
}
