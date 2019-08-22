package com.tmall.web;

import com.tmall.comparator.*;
import com.tmall.pojo.*;
import com.tmall.service.*;
import com.tmall.util.Result;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ForeRESTController {
    @Autowired CategoryService categoryService;
    @Autowired ProductService productService;
    @Autowired UserService userService;
    @Autowired ProductImageService productImageService;
    @Autowired PropertyValueService propertyValueService;
    @Autowired ReviewService reviewService;
    @Autowired OrderItemService orderItemService;
    @Autowired OrderService orderService;

    @GetMapping("/forehome")
    public Object home(){
        List<Category> categories = categoryService.list();
        productService.fill(categories);
        productService.fillByRow(categories);
        categoryService.removeCategoryFromProduct(categories);
        return categories;
    }

    /*
    * registerPage.html 的 axios.js 提交数据到路径 foreregister,导致ForeRESTController.register()方法被调用
    * 1. 通过参数User获取浏览器提交的账号密码
    * 2. 通过HtmlUtils.htmlEscape(name);把账号里的特殊符号进行转义
    * 3. 判断用户名是否存在
    *   3.1 如果已经存在，就返回Result.fail,并带上 错误信息
    *   3.2 如果不存在，则加入到数据库中，并返回 Result.success()
    *
    *   注： 为什么要用 HtmlUtils.htmlEscape？ 因为有些同学在恶意注册的时候，会使用诸如 <script>alert('papapa')</script> 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了。
    *   注： 密码为什么没有加密？ User表还有个 salt字段，为什么没有使用。 咳咳。。。 是这样的，目前这里仅仅实现简单的用户注册功能，后续还在这个基础上改造成用 Shiro 来实现用户验证，加密等等。
    *   注： Result 这个类，第一次使用，是在订单管理 发货功能讲解里用到的，当发货成功后，会返回 Result对象。 Result 对象是一种常见的 RESTFUL 风格返回的 json 格式，里面可以有错误代码，错误信息和数据。 这样就比起以前那样，仅仅返回数据附加了更多的信息，方便前端人员识别和显示给用户可识别信息。
    * */
    @PostMapping("/foreregister")
    public Object register(@RequestBody User user){
        String name = user.getName();
        String password = user.getPassword();
        //转义,防止恶意注册
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);
        if(exist){
            String message = "用户名已经被使用，不能使用";
            return Result.fail(message);
        }
        //user.setPassword(password);
        userService.add(user);
        return Result.success();
    }

    /*
    *loginPage.html的 axios.js 提交数据到路径 forelogin,导致ForeRESTController.login()方法被调用
    * 1. 账号密码注入到 userParam 对象上
    * 2. 把账号通过HtmlUtils.htmlEscape进行转义
    * 3. 根据账号和密码获取User对象
    *   3.1 如果对象为空，则返回错误信息
    *   3.2 如果对象存在，则把用户对象放在 session里，并且返回成功信息
    * 注 为什么要用 HtmlUtils.htmlEscape？
    * 因为注册的时候，ForeRESTController.register()，就进行了转义，所以这里也需要转义。有些同学在恶意注册的时候，会使用诸如 <script>alert('papapa')</script> 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了。
    * */
    @PostMapping("forelogin")
    public Object login(@RequestBody User userParam, HttpSession session){
        String name = userParam.getName();
        name = HtmlUtils.htmlEscape(name);

        User user = userService.get(name, userParam.getPassword());
        if(null == user){
            String message = "账号或密码错误";
            return Result.fail(message);
        }
        else{
            session.setAttribute("user", user);
            return Result.success();
        }
    }

    /*
    *通过访问地址
    *   http://127.0.0.1:8080/tmall_springboot/product?pid=844
    *
    * 导致ForeRESTController.product() 方法被调用
    * 1. 获取参数pid
    * 2. 根据pid获取Product 对象product
    * 3. 根据对象product，获取这个产品对应的单个图片集合
    * 4. 根据对象product，获取这个产品对应的详情图片集合
    * 5. 获取产品的所有属性值
    * 6. 获取产品对应的所有的评价
    * 7. 设置产品的销量和评价数量
    * 8. 把上述取值放在 map 中
    * 9. 通过 Result 把这个 map 返回到浏览器去
    *
    * 为什么要用Map呢？
    * 因为返回出去的数据是多个集合，而非一个集合，所以通过 map返回给浏览器，浏览器更容易识别
    * */
    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable("pid") int pid){
        Product product = productService.get(pid);
        List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);

        List<PropertyValue> propertyValues = propertyValueService.list(product);
        List<Review> reviews = reviewService.list(product);
        productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProductImage(product);

        Map<String, Object> map = new HashMap<>();
        map.put("product", product);
        map.put("pvs", propertyValues);
        map.put("reviews", reviews);

        return Result.success(map);
    }

    //登录状态检查
    @GetMapping("/forecheckLogin")
    public Object checkLogin(HttpSession session){
        User user = (User) session.getAttribute("user");
        if(null != user){
            return Result.success();
        }
        return Result.fail("未登录");
    }

    //根据不同sort获取排序好产品的category
    @GetMapping("/forecategory/{cid}")
    public Object category(@PathVariable("cid") int cid, @RequestParam("sort") String sort){
        Category category = categoryService.get(cid);
        productService.fill(category);
        productService.setSaleAndReviewNumber(category.getProducts());
        categoryService.removeCategoryFromProduct(category);

        if(null != sort){
            switch (sort){
                case "review":
                    Collections.sort(category.getProducts(), new ProductReviewComparator());
                    break;
                case "date":
                    Collections.sort(category.getProducts(), new ProductDateComparator());
                    break;
                case "saleCount":
                    Collections.sort(category.getProducts(), new ProductSaleCountComparator());
                    break;
                case "price":
                    Collections.sort(category.getProducts(), new ProductPriceComparator());
                    break;
                case "price2":
                    Collections.sort(category.getProducts(), new ProductPrice2Comparator());
                    break;
                case "all":
                    Collections.sort(category.getProducts(), new ProductAllComparator());
                    break;
            }
        }
        return category;
    }

    //搜索
    @PostMapping("foresearch")
    public Object search(@RequestParam("keyword") String keyword){
        if(null==keyword)
            keyword = "";
        List<Product> products = productService.search(keyword, 0, 20);
        productImageService.setFirstProductImages(products);
        productService.setSaleAndReviewNumber(products);
        return products;
    }

    @GetMapping("forebuyone")
    public Object buyone(int pid, int num, HttpSession session){
        return buyoneAndAddCart(pid, num, session);
    }

    /*
    * 1. 获取参数pid
    * 2. 获取参数num
    * 3. 根据pid获取产品对象p
    * 4. 从session中获取用户对象user
    *
    * 接下来就是新增订单项OrderItem， 新增订单项要考虑两个情况
    * a. 如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量
    *   a.1 基于用户对象user，查询没有生成订单的订单项集合
    *   a.2 遍历这个集合
    *   a.3 如果产品是一样的话，就进行数量追加
    *   a.4 获取这个订单项的 id
    *
    * b. 如果不存在对应的OrderItem,那么就新增一个订单项OrderItem
    *   b.1 生成新的订单项
    *   b.2 设置数量，用户和产品
    *   b.3 插入到数据库
    *   b.4 获取这个订单项的 id
    *
    *   5.返回当前订单项id
    *   6. 在页面上，拿到这个订单项id，就跳转到 location.href="buy?oiid="+oiid;
    *   buy 是结算页面，现在还没有做，在下一个知识点就会做了。
    *   因为增加到购物车的逻辑和这个是一样的，所以都重构到 buyoneAndAddCart 方法里了，方便后续增加购物车行为。
    * */
    private int buyoneAndAddCart(int pid, int num, HttpSession session) {
        Product product = productService.get(pid);
        User user = (User) session.getAttribute("user");

        List<OrderItem> orderItems = orderItemService.listByUser(user);
        int oiid = 0;
        boolean found = false;
        //如果找到了，在原有订单项上进行修改
        for(OrderItem orderItem : orderItems){
            if(orderItem.getProduct().getId() == product.getId()){
                orderItem.setNumber(orderItem.getNumber() + num);
                orderItemService.update(orderItem);
                found = true;
                oiid = orderItem.getId();
                break;
            }
        }
        //如果没找到则新建订单项
        if(!found){
            OrderItem orderItem = new OrderItem();
            orderItem.setUser(user);
            orderItem.setProduct(product);
            orderItem.setNumber(num);
            orderItemService.add(orderItem);
            oiid = orderItem.getId();
        }
        return oiid;
    }

    /*
    *在 buyPage.html中，访问路径： "forebuy?oiid="+oiid;
    *   http://127.0.0.1:8080/tmall_springboot/forebuy?oiid=1
    *
    * 导致ForeRESTController.buy()方法被调用
    * 1. 通过字符串数组获取参数oiid
    *   为什么这里要用字符串数组试图获取多个oiid，而不是int类型仅仅获取一个oiid? 因为根据购物流程环节与表关系，结算页面还需要显示在购物车中选中的多条OrderItem数据，所以为了兼容从购物车页面跳转过来的需求，要用字符串数组获取多个oiid
    * 2. 准备一个泛型是OrderItem的集合ois
    * 3. 根据前面步骤获取的oiids，从数据库中取出OrderItem对象，并放入ois集合中
    * 4. 累计这些ois的价格总数，赋值在total上
    * 5. 把订单项集合放在session的属性 "ois" 上
    * 6. 把订单集合和total 放在map里
    * 7. 通过 Result.success 返回
    * */
    @GetMapping("forebuy")
    public  Object buy(@RequestParam("oiid") String[] oiid, HttpSession session){
        List<OrderItem> orderItems = new ArrayList<>();
        float total = 0;
        for(String strid : oiid){
            int id = Integer.parseInt(strid);
            OrderItem orderItem = orderItemService.get(id);
            total += orderItem.getProduct().getPromotePrice() * orderItem.getNumber();
            orderItems.add(orderItem);
        }

        productImageService.setFirstProductImagesOnOrderItems(orderItems);

        session.setAttribute("ois", orderItems);

        Map<String, Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total);
        return Result.success(map);
    }

    //加入购物车
    @GetMapping("foreaddCart")
    public Object addCart(int pid, int num, HttpSession session){
        buyoneAndAddCart(pid, num, session);
        return Result.success();
    }

    /*
    * 访问地址/forecart导致ForeRESTController.cart()方法被调用
    * 1. 通过session获取当前用户
    *   所以一定要登录才访问，否则拿不到用户对象,会报错
    * 2. 获取为这个用户关联的订单项集合 ois
    * 3. 设置图片
    * 4. 返回这个订单项集合
    * */
    @GetMapping("forecart")
    public Object cart(HttpSession session){
        User user = (User) session.getAttribute("user");
        List<OrderItem> orderItems = orderItemService.listByUser(user);
        productImageService.setFirstProductImagesOnOrderItems(orderItems);
        return orderItems;
    }

    @GetMapping("forechangeOrderItem")
    public Object changeOrderItem(HttpSession session, int pid, int num){
        User user = (User) session.getAttribute("user");
        if(null == user)
            return Result.fail("未登录");
        List<OrderItem> orderItems = orderItemService.listByUser(user);
        for(OrderItem orderItem : orderItems){
            if(orderItem.getProduct().getId() == pid){
                orderItem.setNumber(num);
                orderItemService.update(orderItem);
                break;
            }
        }
        return Result.success();
    }

    @GetMapping("foredeleteOrderItem")
    public Object deleteOrderItem(HttpSession session, int oiid){
        User user = (User) session.getAttribute("user");
        if(null == user)
            return Result.fail("未登录");
        orderItemService.delete(oiid);
        return Result.success();
    }

    /*
    * 提交订单访问路径 /forecreateOrder, 导致ForeRESTController.createOrder 方法被调用
    * 1. 从session中获取user对象
    * 2. 根据当前时间加上一个4位随机数生成订单号
    * 3. 根据上述参数，创建订单对象
    * 4. 把订单状态设置为等待支付
    * 5. 从session中获取订单项集合 ( 在结算功能的ForeRESTController.buy() ，订单项集合被放到了session中 )
    * 7. 把订单加入到数据库，并且遍历订单项集合，设置每个订单项的order，更新到数据库
    * 8. 统计本次订单的总金额
    * 9. 返回总金额
    * */
    @PostMapping("/forecreateOrder")
    public Object createOrder(@RequestBody Order order, HttpSession session){
        User user = (User) session.getAttribute("user");
        if(null == user)
            return Result.fail("未登录");
        //根据当前时间加上一个4位随机数生成订单号
        Date date = new Date();
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSS").format(date) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(date);
        order.setUser(user);
        order.setStatus(OrderService.waitPay);
        List<OrderItem> ois = (List<OrderItem>) session.getAttribute("ois");

        float total = orderService.add(order, ois);

        Map<String,Object> map = new HashMap<>();
        map.put("oid", order.getId());
        map.put("total", total);

        return Result.success(map);
    }

    @GetMapping("forepayed")
    public Object payed(int oid){
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }

    /*
    * /forebought导致ForeRESTController.bought()方法被调用
    * 1. 通过session获取用户user
    * 2. 查询user所有的状态不是"delete" 的订单集合os
    * 3. 为这些订单填充订单项
    * 4. 返回 订单集合
    * */
    @GetMapping("forebought")
    public Object bought(HttpSession session){
        //System.out.println("aaa");
        User user = (User) session.getAttribute("user");
        if(null == user)
            return Result.fail("未登录");
        //System.out.println("sss");
        List<Order> orders = orderService.listByUserWithoutDelete(user);
        orderService.removeOrderFromOrderItem(orders);
        return orders;
    }

    //确认收货
    @GetMapping("foreconfirmPay")
    public Object confirmPay(int oid){
        Order order = orderService.get(oid);
        orderItemService.fill(order);
        orderService.calc(order);
        orderService.removeOrderFromOrderItem(order);
        System.out.println(order.getAddress());
        return order;
    }

    //确认收货成功
    @GetMapping("foreorderConfirmed")
    public Object orderConfirmed(int oid){
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitReview);
        order.setCreateDate(new Date());
        orderService.update(order);
        return Result.success();
    }

    //删除订单
    @PutMapping("foredeleteOrder")
    public Object deleteOrder(int oid){
        Order order = orderService.get(oid);
        order.setStatus(OrderService.delete);
        orderService.update(order);
        return Result.success();
    }

    /*
    * 3. ForeRESTController.review() 被调用
    *   3.1 获取参数oid
    *   3.2 根据oid获取订单对象o
    *   3.3 为订单对象填充订单项
    *   3.4 获取第一个订单项对应的产品,因为在评价页面需要显示一个产品图片，那么就使用这第一个产品的图片了。（这里没有对订单里的每种产品都评价，因为复杂度就比较高了，初学者学起来太吃力，有可能就放弃学习了，所以考虑到学习的平滑性，就仅仅提供对第一个产品的评价）
    *   3.5 获取这个产品的评价集合
    *   3.6 为产品设置评价数量和销量
    *   3.7 把产品，订单和评价集合放在map上
    *   3.8 通过 Result 返回这个map
    * */
    @GetMapping("forereview")
    public Object review(int oid){
        System.out.println("sss");
        Order order = orderService.get(oid);
        orderItemService.fill(order);
        orderService.removeOrderFromOrderItem(order);
        Product product = order.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(product);
        productService.setSaleAndReviewNumber(product);
        boolean showReviews = (order.getStatus() == OrderService.finish);
        Map<String, Object> map = new HashMap<>();
        map.put("p", product);
        map.put("o", order);
        map.put("reviews", reviews);
        map.put("showReviews", showReviews);
        return  Result.success(map);
    }

    @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    @PostMapping("foredoreview")
    public Object doreview(HttpSession session, int oid, int pid, String content){
        Order order = orderService.get(oid);
        order.setStatus(OrderService.finish);
        orderService.update(order);

        Product product = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);

        User user = (User) session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setProduct(product);
        review.setCreateDate(new Date());
        review.setUser(user);
        reviewService.add(review);
        return Result.success();
    }
}
