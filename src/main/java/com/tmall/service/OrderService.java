package com.tmall.service;

import com.tmall.dao.OrderDAO;
import com.tmall.pojo.Order;
import com.tmall.pojo.OrderItem;
import com.tmall.pojo.User;
import com.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
* OrderService,提供分页查询。
* 还提供了 订单状态的常量，Order.java 的 getStatusDesc 会用到。
* 另外还提供了一个奇怪的方法，removeOrderFromOrderItem，它的作用是把订单里的订单项的订单属性设置为空。。。
* 听上去绕口吧。 再用代码说一下，比如有个 order, 拿到它的 orderItems， 然后再把这些orderItems的order属性，设置为空。
* 为什么要做这个事情呢？ 因为SpringMVC ( springboot 里内置的mvc框架是 这个东西)的 RESTFUL 注解，在把一个Order转换为json的同时，会把其对应的 orderItems 转换为 json数组， 而 orderItem对象上有 order属性， 这个order 属性又会被转换为 json对象，然后这个 order 下又有 orderItems 。。。
* 就这样就会产生无穷递归，系统就会报错了。
* 所以这里采用 removeOrderFromOrderItem 把 OrderItem的order设置为空就可以了。
* 那么为什么不用 @JsonIgnoreProperties 来标记这个字段呢？
* 因为后续我们要整合Redis，如果标记成了 @JsonIgnoreProperties 会在和 Redis 整合的时候有 Bug, 所以还是采用这种方式比较好。
* */
@Service
@CacheConfig(cacheNames="orders")
public class OrderService {
    public static final String waitPay = "waitPay";
    public static final String waitDelivery = "waitDelivery";
    public static final String waitConfirm = "waitConfirm";
    public static final String waitReview = "waitReview";
    public static final String finish = "finish";
    public static final String delete = "delete";

    @Autowired OrderDAO orderDAO;
    @Autowired OrderItemService orderItemService;

    @Cacheable(key="'orders-page-'+#p0+ '-' + #p1")
    public Page4Navigator<Order> list(int start, int size, int navigatePages){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        Page pageFromJPA = orderDAO.findAll(pageable);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    public void removeOrderFromOrderItem(List<Order> orders){
        for(Order order : orders){
            removeOrderFromOrderItem(order);
        }
    }

    public void removeOrderFromOrderItem(Order order) {
        List<OrderItem> orderItems = order.getOrderItems();
        for(OrderItem orderItem : orderItems){
            orderItem.setOrder(null);
        }
    }

    @Cacheable(key="'orders-one-'+ #p0")
    public Order get(int oid){
        return orderDAO.findOne(oid);
    }

    @CacheEvict(allEntries=true)
    public void update(Order bean){
        orderDAO.save(bean);
    }

    /*
    * 增加 add(Order o, List<OrderItem> ois)方法，该方法通过注解进行事务管理
    *   @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    * 故意抛出异常代码用来模拟当增加订单后出现异常，观察事务管理是否预期发生。（需要把false修改为true才能观察到）
    *   if(false)
    *       throw new RuntimeException();
    * */
    /*
    *propagation = Propagation.REQUIRED
    *   当前有事务则加入，没有则新建一个事务
    *rollbackForClassName = "Exception"
    *   回滚的错误类型
    * */
    @CacheEvict(allEntries=true)
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
    public float add(Order order, List<OrderItem> orderItems){
        float total = 0;
        add(order);

        if(false)
            throw new RuntimeException();

        for(OrderItem orderItem : orderItems){
            orderItem.setOrder(order);
            orderItemService.update(orderItem);
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
        }
        return total;
    }

    @CacheEvict(allEntries=true)
    public void add(Order order) {
        orderDAO.save(order);
    }

    public List<Order> listByUserWithoutDelete(User user){
        List<Order> orders = listByUserAndNotDeleted(user);
        orderItemService.fill(orders);
        return orders;
    }

    @Cacheable(key="'orders-uid-'+ #p0.id")
    public List<Order> listByUserAndNotDeleted(User user){
        return orderDAO.findByUserAndStatusNotOrderByIdDesc(user, OrderService.delete);
    }

    //计算订单总金额
    public void calc(Order order){
        List<OrderItem> orderItems = order.getOrderItems();
        float total = 0;
        for(OrderItem orderItem : orderItems){
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
        }
        order.setTotal(total);
    }
}
