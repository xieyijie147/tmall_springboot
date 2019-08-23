package com.tmall.service;

import com.tmall.dao.ProductImageDAO;
import com.tmall.pojo.OrderItem;
import com.tmall.pojo.Product;
import com.tmall.pojo.ProductImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageService {
    //提供两个常量，分别表示单个图片和详情图片
    public static final String type_single = "single";
    public static final String type_detail = "detail";

    @Autowired ProductImageDAO productImageDAO;
    @Autowired ProductService productService;

    public void add(ProductImage bean){
        productImageDAO.save(bean);
    }

    public void delete(int id){
        productImageDAO.delete(id);
    }

    public ProductImage get(int id){
        return productImageDAO.findOne(id);
    }

    //提供了根据产品id和图片类型查询的list方法
    public List<ProductImage> listSingleProductImages(Product product){
        return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_single);
    }
    public List<ProductImage> listDetailProductImages(Product product){
        return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_detail);
    }

    public void setFirstProductImage(Product product){
        List<ProductImage> singleImages = listSingleProductImages(product);
        if(!singleImages.isEmpty()){
            product.setFirstProductImage(singleImages.get(0));
        }
        else{
            product.setFirstProductImage(new ProductImage());
        }
        //这样做是考虑到产品还没有来得及设置图片，但是在订单后台管理里查看订单项的对应产品图片。
    }
    public void setFirstProductImages(List<Product> products){
        for(Product product : products){
            setFirstProductImage(product);
        }
    }

    public void setFirstProductImagesOnOrderItems(List<OrderItem> orderItems){
        for(OrderItem orderItem : orderItems){
            setFirstProductImage(orderItem.getProduct());
        }
    }
}
