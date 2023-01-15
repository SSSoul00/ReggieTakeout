package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishDto;
import com.itheima.entity.DishFlavor;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据分类ID查找启用菜品及其口味
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDishes(Dish dish) {
        //1、根据分类ID查询启用的菜品
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .eq(Dish::getStatus, 1)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(qw);
        //2、查询每一个菜品的口味，将菜品及其口味封装到Dto中
        List<DishDto> dishDtoList = dishes.stream().map(item -> {
            LambdaQueryWrapper<DishFlavor> qw2 = new LambdaQueryWrapper<>();
            qw2.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(qw2);
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        //3、响应
        return R.success(dishDtoList);
    }

    /**
     * 删除菜品及口味
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.in(Dish::getId, ids);
        dishService.remove(qw);
        LambdaQueryWrapper<DishFlavor> qw2 = new LambdaQueryWrapper<>();
        qw2.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(qw2);
        return R.success("删除菜品成功");
    }

    /**
     * 更新菜品状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, Long[] ids) {
        Dish dish = new Dish();
        dish.setStatus(status);
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.in(Dish::getId, ids);
        dishService.update(dish, qw);
        return R.success("修改状态成功");
    }

    /**
     * 更新菜品和口味
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("更新菜品成功");
    }

    /**
     * 查询单个
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getDishDtoById(id);
        return R.success(dishDto);
    }

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }

    /**
     * 获取分页数据
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> getPage(int page, int pageSize, String name) {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.like(name != null, Dish::getName, name);
        qw.orderByDesc(Dish::getUpdateTime);

        dishService.page(dishPage, qw);

        //将dishPage除records的属性复制到dishDtoPage
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        //将dishList转化为dishDtoList
        List<Dish> dishes = dishPage.getRecords();
        List<DishDto> dishDtoList = dishes.stream().map(item -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dto.setCategoryName(categoryName);
            }
            return dto;
        }).collect(Collectors.toList());

        //将dishDtoList注入dishDtoPage后响应浏览器
        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }
}
