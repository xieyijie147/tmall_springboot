package com.tmall.service;


import com.tmall.dao.OrderItemDAO;
import com.tmall.pojo.Order;
import com.tmall.pojo.OrderItem;
import com.tmall.pojo.Product;
import com.tmall.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
*OrderItemService，提供对OrderItem的业务操作，其中主要是 fill 方法。
* 从数据库中取出来的 Order 是没有 OrderItem集合的，这里通过 OrderItemDAO 取出来再放在 Order的 orderItems属性上。
* 除此之外，还计算订单总数，总金额等等信息。
* */
@Service
public class OrderItemService {
    @Autowired OrderItemDAO orderItemDAO;
    @Autowired ProductImageService productImageService;

    public void fill(List<Order> orders){
        for(Order order : orders)
            fill(order);
    }

    /*计算订单的总金额，总数量，订单项*/
    public void fill(Order order){
        List<OrderItem> orderItems = listByOrder(order);
        float total = 0;
        int totalNumber = 0;
        for(OrderItem orderItem : orderItems){
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
            totalNumber += orderItem.getNumber();
            productImageService.setFirstProductImage(orderItem.getProduct());
        }
        order.setTotal(total);
        order.setTotalNumber(totalNumber);
        order.setOrderItems(orderItems);
    }

    public void update(OrderItem orderItem){
        orderItemDAO.save(orderItem);
    }

    public void add(OrderItem orderItem){
        orderItemDAO.save(orderItem);
    }

    public OrderItem get(int id){
        return orderItemDAO.findOne(id);
    }

    public void delete(int id){
        orderItemDAO.delete(id);
    }

    public int getSaleCount(Product product){
        List<OrderItem> orderItems = listByProduct(product);
        int result = 0;
        for(OrderItem orderItem : orderItems){
            if(null != orderItem.getOrder()) {
                if (null != orderItem.getOrder() && null != orderItem.getOrder().getPayDate()) {
                    result += orderItem.getNumber();
                }
            }
        }
        return result;
    }

    public List<OrderItem> listByProduct(Product product){
        return orderItemDAO.findByProduct(product);
    }

    public List<OrderItem> listByOrder(Order order) {
        return orderItemDAO.findByOrderOrderByIdDesc(order);
    }

    public List<OrderItem> listByUser(User user){
        return orderItemDAO.findByUserAndOrderIsNull(user);
    }
}
