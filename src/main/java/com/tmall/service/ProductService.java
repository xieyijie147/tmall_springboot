package com.tmall.service;

import com.tmall.dao.ProductDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.Product;
import com.tmall.util.Page4Navigator;
import com.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames = "products")
public class ProductService {
    @Autowired ProductDAO productDAO;
    @Autowired CategoryService categoryService;
    @Autowired ProductImageService productImageService;
    @Autowired OrderItemService orderItemService;
    @Autowired ReviewService reviewService;

    @CacheEvict(allEntries=true)
    public void add(Product bean){
        productDAO.save(bean);
    }

    @CacheEvict(allEntries=true)
    public void delete(int id){
        productDAO.delete(id);
    }

    @Cacheable(key="'products-one-'+ #p0")
    public Product get(int id){
        return productDAO.findOne(id);
    }

    @CacheEvict(allEntries=true)
    public void update(Product bean){
        productDAO.save(bean);
    }

    @Cacheable(key="'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public Page4Navigator<Product> list(int cid, int start, int size, int navigetePages){
        Category category = categoryService.get(cid);

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);

        Page<Product> pageFromJPA = productDAO.findByCategory(category, pageable);

        return new Page4Navigator<>(pageFromJPA, navigetePages);
    }

    //1. 为分类填充产品集合
    public void fill(Category category){
        ProductService productService = SpringContextUtil.getBean(ProductService.class);
        List<Product> products = productService.listByCategory(category);
        productImageService.setFirstProductImages(products);
        category.setProducts(products);
    }

    //2. 为多个分类填充产品集合
    public void fill(List<Category> categories){
        for(Category category : categories){
            fill(category);
        }
    }

    //3.为多个分类填充推荐产品集合，即把分类下的产品集合，按照8个为一行，拆成多行，以利于后续页面上进行显示
    public void fillByRow(List<Category> categories){
        int productNumberEachRow = 8;
        for(Category category : categories){
            List<Product> products = category.getProducts();
            List<List<Product>> productsByRow = new ArrayList<>();
            for(int i = 0; i < products.size(); i+=productNumberEachRow){
                int size = i + productNumberEachRow;
                size = size > products.size() ? products.size() : size;
                List<Product> productsOfEachRow = products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }

    //4. 查询某个分类下的所有产品
    @Cacheable(key="'products-cid-'+ #p0.id")
    public List<Product> listByCategory(Category category) {
        return productDAO.findByCategoryOrderById(category);
    }

    //设置产品的销售额和评论数
    public void setSaleAndReviewNumber(Product product){
        int saleCount = orderItemService.getSaleCount(product);
        product.setSaleCount(saleCount);

        int reviewCount = reviewService.getCount(product);
        product.setReviewCount(reviewCount);
    }

    public void setSaleAndReviewNumber(List<Product> products){
        for(Product product : products){
            setSaleAndReviewNumber(product);
        }
    }

    public List<Product> search(String keyword, int start, int size) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        List<Product> products = productDAO.findByNameLike("%"+keyword+"%", pageable);
        return products;
    }
}
