package com.tmall.dao;

import com.tmall.pojo.Order;
import com.tmall.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDAO extends JpaRepository<Order, Integer>{
    //用来获取那些某个用户的订单，但是状态又不是 "delete" 的订单。 "delete" 是作为状态调用的时候传进来的
    public List<Order> findByUserAndStatusNotOrderByIdDesc(User user, String status);
}
