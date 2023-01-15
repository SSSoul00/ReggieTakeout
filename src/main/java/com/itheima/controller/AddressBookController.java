package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.common.BaseContext;
import com.itheima.entity.AddressBook;
import com.itheima.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/addressBook")
@Slf4j
@Transactional
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.get();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return R.success("添加地址成功");
    }

    /**
     * 获取当前用户所有地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> getAddress() {
        Long userId = BaseContext.get();
        LambdaQueryWrapper<AddressBook> qw = new LambdaQueryWrapper<>();
        qw.eq(userId != null, AddressBook::getUserId, userId);
        List<AddressBook> AddressBookList = addressBookService.list(qw);
        return R.success(AddressBookList);
    }

    /**
     * 根据地址ID查询地址，修改回显。
     * @param addressBookId
     * @return
     */
    @GetMapping("/{addressBookId}")
    public R<AddressBook> getById(@PathVariable Long addressBookId) {
        AddressBook addressBook = addressBookService.getById(addressBookId);
        return R.success(addressBook);
    }

    /**
     * 获取默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefaultAddress(){
        LambdaQueryWrapper<AddressBook> qw = new LambdaQueryWrapper<>();
        qw.eq(AddressBook::getUserId,BaseContext.get())
          .eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = addressBookService.getOne(qw);
        return R.success(addressBook);
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> updateStatus(@RequestBody AddressBook addressBook) {
        //1、将所有地址默认值改为0
        Long userId = BaseContext.get();
        LambdaUpdateWrapper<AddressBook> qw = new LambdaUpdateWrapper<>();
        qw.eq(userId != null, AddressBook::getUserId, userId);
        qw.set(AddressBook::getIsDefault, 0);
        addressBookService.update(qw);
        //2、将指定ID的地址默认值改为1
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("修改状态成功");
    }
}
