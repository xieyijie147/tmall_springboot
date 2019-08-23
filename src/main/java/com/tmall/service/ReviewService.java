package com.tmall.service;

import com.tmall.dao.ReviewDAO;
import com.tmall.pojo.Product;
import com.tmall.pojo.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="reviews")
public class ReviewService {
    @Autowired ReviewDAO reviewDAO;

    @CacheEvict(allEntries=true)
    public void add(Review review){
        reviewDAO.save(review);
    }

    @Cacheable(key="'reviews-pid-'+ #p0.id")
    public List<Review> list(Product product){
        List<Review> result = reviewDAO.findByProductOrderByIdDesc(product);
        return result;
    }

    @Cacheable(key="'reviews-count-pid-'+ #p0.id")
    public int getCount(Product product){
        return reviewDAO.countByProduct(product);
    }
}
