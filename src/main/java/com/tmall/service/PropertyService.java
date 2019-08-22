package com.tmall.service;

import com.tmall.dao.PropertyDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.Property;
import com.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PropertyService {

    @Autowired
    PropertyDAO propertyDAO;
    @Autowired
    CategoryService categoryService;

    public void add(Property bean){
        propertyDAO.save(bean);
    }

    public void delete(int id){
        propertyDAO.delete(id);
    }

    public Property get(int id){
        return propertyDAO.findOne(id);
    }

    public void update(Property bean){
        propertyDAO.save(bean);
    }

    public Page4Navigator<Property> list(int cid, int start, int size, int navigatePages){
        Category category = categoryService.get(cid);

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);

        Page<Property> pageFromJPA = propertyDAO.findByCategory(category, pageable);

        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    public List<Property> ListbyCatrgory(Category category){
        return propertyDAO.findByCategory(category);
    }
}
