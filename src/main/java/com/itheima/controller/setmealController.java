package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.entity.SetmealDto;
import com.itheima.exception.CustomException;
import com.itheima.service.CategoryService;
import com.itheima.service.SetMealService;
import com.itheima.service.SetmealDishiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class setmealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishiService setmealDishiService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类ID查找启用套餐
     *
     * @param categoryId
     * @param status
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#categoryId +'_' +#status")
    public R<List<Setmeal>> getSetmeals(Long categoryId, Integer status) {
//        String key = "setmeal_" + categoryId + "_" + status;
        //从redis缓存中查找套餐数据，如果有直接return
//        List<Setmeal> setmealList = (List<Setmeal>) redisTemplate.opsForValue().get(key);
//        if (setmealList!=null){
//            return R.success(setmealList);
//        }
        //如果没有从数据库中查询，存入redis缓存。
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.eq(status != null, Setmeal::getStatus, status)
                .eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                .orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setMealService.list(qw);
//        redisTemplate.opsForValue().set(key,setmealList,60, TimeUnit.MINUTES);
        return R.success(setmealList);
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, Long[] ids) {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.in(Setmeal::getId, ids);
        setMealService.update(setmeal, qw);
        return R.success("修改套餐状态成功");
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    @Transactional
    public R<String> delete(long[] ids) {
        //定义数组用于存放无法删除的套餐名
        ArrayList<String> setmeals = new ArrayList<>();
        //删除停售状态的套餐
        for (long id : ids) {
            Setmeal setmeal = setMealService.getById(id);
            if (setmeal.getStatus() == 0) {
                setMealService.removeById(id);
                LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
                qw.eq(SetmealDish::getSetmealId, id);
                setmealDishiService.remove(qw);
            } else {
                setmeals.add(setMealService.getById(id).getName());
            }
        }
        //若有无法删除的套餐抛出业务异常
        if (setmeals.size() > 0) {
            String setmealInfo = "";
            for (int i = 0; i < setmeals.size(); i++) {
                if (i != setmeals.size() - 1) {
                    setmealInfo += setmeals.get(i) + ",";
                } else {
                    setmealInfo += setmeals.get(i);
                }
            }
            throw new CustomException(setmealInfo + "处于出售状态，无法删除");
        }
        //若全部删除响应删除成功
        return R.success("删除套餐成功");
//        //删除套餐
//        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
//        qw.in(ids!=null,Setmeal::getId,ids);
//        setMealService.remove(qw);
//        //删除套餐关联菜品
//        LambdaQueryWrapper<SetmealDish> qw2 = new LambdaQueryWrapper<>();
//        qw2.in(ids!=null,SetmealDish::getSetmealId,ids);
//        setmealDishiService.remove(qw2);

    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        //更新时清理redis缓存
//        String key = "setmeal_" + setmealDto.getCategoryId() + "_1";
//        redisTemplate.delete(key);
        //修改套餐信息
        setMealService.updateById(setmealDto);
        //修改套餐关联菜品信息
        //1、删除套餐内菜品
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishiService.remove(qw);
        //2、重新添加套餐内菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishiService.saveBatch(setmealDishes);
        return R.success("修改套餐成功");
    }

    /**
     * 查询单个
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto setmealDto = new SetmealDto();
        //获取套餐信息
        Setmeal setmeal = setMealService.getById(id);
        //查询套餐内菜品
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishiService.list(qw);
        //为SetmealDto赋值
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);
        return R.success(setmealDto);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> getPage(int page, int pageSize, String name) {
        //创建分页对象
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);
        //查询分页
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.like(name != null, Setmeal::getName, name).orderByDesc(Setmeal::getUpdateTime);
        setMealService.page(setmealPage, qw);
        //为setmealDtoPage赋值
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");
        List<Setmeal> setmeals = setmealPage.getRecords();
        List<SetmealDto> setmealDtos = setmeals.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            String categoryName = categoryService.getById(item.getCategoryId()).getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(setmealDtos);

        return R.success(setmealDtoPage);
    }

    /**
     * 添加套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        //更新时清理redis缓存
//        String key = "setmeal_" + setmealDto.getCategoryId() + "_1";
//        redisTemplate.delete(key);
        setMealService.saveWithDish(setmealDto);
        return R.success("添加套餐成功");
    }
}
