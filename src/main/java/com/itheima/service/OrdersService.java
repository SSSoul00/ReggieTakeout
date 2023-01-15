package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.entity.Orders;

public interface OrdersService extends IService<Orders> {
    public void submitOrder(Orders orders);
    public Page<Orders> getPage(int page,int pageSize);
}
