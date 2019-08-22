package com.tmall.web;

import com.tmall.pojo.Order;
import com.tmall.service.OrderItemService;
import com.tmall.service.OrderService;
import com.tmall.util.Page4Navigator;
import com.tmall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/*
* 提供分页查询和订单发货，
* 这里就调用了 removeOrderFromOrderItem 方法，
* 可以试试不调用这个方法会发生什么事情。
* */
@RestController
public class OrderController {
    @Autowired OrderService orderService;
    @Autowired OrderItemService orderItemService;

    @GetMapping("/orders")
    public Page4Navigator<Order> list(@RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "5") int size){
        start = start < 0 ? 0 : start;
        Page4Navigator<Order> page = orderService.list(start, size, 5);
        orderItemService.fill(page.getContent());
        orderService.removeOrderFromOrderItem(page.getContent());
        return page;
    }

    @PutMapping("deliveryOrder/{oid}")
    public Object deliveryOrder(@PathVariable("oid") int oid){
        Order order = orderService.get(oid);
        order.setDeliveryDate(new Date());
        order.setStatus(OrderService.waitConfirm);
        orderService.update(order);
        return Result.success();
    }
}
