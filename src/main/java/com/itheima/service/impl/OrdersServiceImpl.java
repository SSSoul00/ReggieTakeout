package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.BaseContext;
import com.itheima.entity.*;
import com.itheima.exception.CustomException;
import com.itheima.mapeer.OrdersMapper;
import com.itheima.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单
     *
     * @param orders
     */
    @Override
    @Transactional
    public void submitOrder(Orders orders) {
        //根据用户ID查询用户信息
        User user = userService.getById(BaseContext.get());
        //根据请求数据中的地址ID查询地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        //根据用户ID查询购物车数据
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId, user.getId());
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(qw);

        if (shoppingCartList == null) {
            throw new CustomException("购物车内没有商品，无法提交订单");
        }
        //通过MP的IdWorker获取订单ID
        long orderId = IdWorker.getId();
        //通过原子操作整数保证多线程高并发不出错
        AtomicInteger amount = new AtomicInteger(0);
        //将购物车数据集合封装成OrderDetail集合，并累加总金额
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(item,orderDetail);
            orderDetail.setOrderId(orderId);
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        //设置订单属性
        orders.setId(orderId);
        orders.setUserId(user.getId());
        orders.setUserName(user.getName());
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getDistrictName());
        orders.setPhone(addressBook.getPhone());
        orders.setNumber(String.valueOf(orderId));
        orders.setAmount(BigDecimal.valueOf(amount.get()));
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入一条数据
        this.save(orders);
        //向订单明细表插入N条数据
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车
        shoppingCartService.remove(qw);
    }

    /**查询订单
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<Orders> getPage(int page, int pageSize) {
        Page<Orders> pageInfo =new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId,BaseContext.get());
        this.page(pageInfo,qw);
        return pageInfo;
    }
}
