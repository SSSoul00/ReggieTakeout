package com.itheima.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.entity.Orders;
import com.itheima.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    private R<Page> getPage(int page,int pageSize){
        Page<Orders> pageInfo = ordersService.getPage(page, pageSize);
        return R.success(pageInfo);
    }
    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public R<String> submitOrder(@RequestBody Orders orders){
        log.info(orders.toString());
        ordersService.submitOrder(orders);
        return R.success("添加订单成功");
    }
}
