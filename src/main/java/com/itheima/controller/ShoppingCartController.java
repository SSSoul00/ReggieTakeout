package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.common.BaseContext;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.DishService;
import com.itheima.service.SetMealService;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart(){
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,BaseContext.get());
        shoppingCartService.remove(qw);
        return R.success("清空购物车成功");
    }

    /**
     * 根据菜品ID或套餐ID将购物车Number-1或删除
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> subShoppingCart(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        //判断是菜品还是套餐来添加查询条件
        if (shoppingCart.getDishId() != null) {
            //根据DishId查询
            qw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //根据SetmealId查询
            qw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //2、判断此菜品或套餐是否已存在购物车内
        ShoppingCart one = shoppingCartService.getOne(qw);
        Integer number = one.getNumber();
        if (number == 1){
            shoppingCartService.remove(qw);
        }else {
            one.setNumber(number-1);
            shoppingCartService.updateById(one);
        }
        return R.success("删除成功");
    }

    /**
     * 根据用户ID查询出对应购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getList(){
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,BaseContext.get());
        qw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(qw);
        return R.success(shoppingCarts);
    }

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        //1、设置用户ID
        shoppingCart.setUserId(BaseContext.get());
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        //设置查询条件，根据UserId查询
        qw.eq(ShoppingCart::getUserId, BaseContext.get());
        //判断是菜品还是套餐来添加查询条件
        if (shoppingCart.getDishId() != null) {
            //根据DishId查询
            qw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            //根据SetmealId查询
            qw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //2、判断此菜品或套餐是否已存在购物车内
        ShoppingCart one = shoppingCartService.getOne(qw);
        //不存在则添加至数据库
        if (one == null) {
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        } else {
            //已存在则将number+1
            int number = one.getNumber() + 1;
            one.setNumber(number);
            shoppingCartService.updateById(one);
        }

        return R.success(shoppingCart);
    }
}
