package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.entity.Dish;
import com.itheima.entity.DishDto;
import com.itheima.entity.DishFlavor;
import com.itheima.mapeer.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    public DishDto getDishDtoById(Long id) {
        //1、根据id获取菜品
        Dish dish = this.getById(id);
        //2、将菜品属性复制给DTO
        DishDto dto = new DishDto();
        BeanUtils.copyProperties(dish, dto);
        //3、获取菜品口味赋值给DTO
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavorList = dishFlavorService.list(qw);
        dto.setFlavors(flavorList);
        return dto;
    }

    /**
     * 更新菜品和口味
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品信息
        this.updateById(dishDto);
        //为口味中DishId赋值
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map(dishFlavor -> {
            dishFlavor.setDishId(dishId);
            return dishFlavor;
        }).collect(Collectors.toList());
        //先根据DishID删除口味
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(qw);
        //重新添加口味
        dishFlavorService.saveBatch(flavors);
    }



    /**
     * 添加菜品和口味
     * @param dishDto
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //添加菜品
        this.save(dishDto);

        //设置菜品对应的口味
        Long id = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map(dishFlavor -> {
            dishFlavor.setDishId(id);
            return dishFlavor;
        }).collect(Collectors.toList());

        //添加口味
        dishFlavorService.saveBatch(flavors);
    }

}
