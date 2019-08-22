package com.tmall.web;

import com.tmall.pojo.Product;
import com.tmall.service.ProductImageService;
import com.tmall.service.ProductService;
import com.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    @Autowired ProductService productService;
    @Autowired ProductImageService productImageService;

    @GetMapping(value = "/categories/{cid}/products")
    public Page4Navigator<Product> list(@PathVariable(value = "cid") int cid, @RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "5") int size){
        start = start < 0 ? 0 : start;
        Page4Navigator<Product> page = productService.list(cid, start, size, 5);
        productImageService.setFirstProductImages(page.getContent());
        return page;
    }

    @GetMapping(value = "/products/{id}")
    public Product get(@PathVariable(value = "id") int id){
        Product bean = productService.get(id);
        return bean;
    }

    @PostMapping(value = "/products")
    public Object add(@RequestBody Product bean){
        productService.add(bean);
        return bean;
    }

    @DeleteMapping(value = "/products/{id}")
    public String delete(@PathVariable(value = "id") int id){
        productService.delete(id);
        return null;
    }

    @PutMapping(value = "/products")
    public Object update(@RequestBody Product bean){
        productService.update(bean);
        return bean;
    }
}
