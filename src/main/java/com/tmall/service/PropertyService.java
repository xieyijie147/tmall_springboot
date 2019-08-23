package com.tmall.service;

import com.tmall.dao.PropertyDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.Property;
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

import java.util.List;

/*
* 新建PropertyService，提供CRUD一套。
* 需要注意的是，因为在业务上需要查询某个分类下的属性，所以list方法会带上对应分类的id。
* */

@Service
@CacheConfig(cacheNames="properties")
public class PropertyService {

    @Autowired
    PropertyDAO propertyDAO;
    @Autowired
    CategoryService categoryService;

    @CacheEvict(allEntries=true)
    public void add(Property bean){
        propertyDAO.save(bean);
    }

    @CacheEvict(allEntries=true)
    public void delete(int id){
        propertyDAO.delete(id);
    }

    @Cacheable(key="'properties-one-'+ #p0")
    public Property get(int id){
        return propertyDAO.findOne(id);
    }

    @CacheEvict(allEntries=true)
    public void update(Property bean){
        propertyDAO.save(bean);
    }

    @Cacheable(key="'properties-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public Page4Navigator<Property> list(int cid, int start, int size, int navigatePages){
        Category category = categoryService.get(cid);

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);

        Page<Property> pageFromJPA = propertyDAO.findByCategory(category, pageable);

        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }
    @Cacheable(key="'properties-cid-'+ #p0.id")
    public List<Property> ListbyCatrgory(Category category){
        return propertyDAO.findByCategory(category);
    }
}
