package com.itheima.entity;


import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装Dish、口味、分类名称等，用于前后端数据传输；
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
