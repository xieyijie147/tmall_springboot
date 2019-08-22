package com.tmall.dao;

import com.tmall.pojo.Product;
import com.tmall.pojo.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewDAO extends JpaRepository<Review, Integer> {
    //查找某产品下的评论集合
    List<Review> findByProductOrderByIdDesc(Product product);
    //返回评论数量
    int countByProduct(Product product);
}
