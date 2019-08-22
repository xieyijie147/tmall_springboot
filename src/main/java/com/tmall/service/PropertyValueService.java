package com.tmall.service;

import com.tmall.dao.ProductDAO;
import com.tmall.dao.PropertyDAO;
import com.tmall.dao.PropertyValueDAO;
import com.tmall.pojo.Product;
import com.tmall.pojo.Property;
import com.tmall.pojo.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
*PropertyValueService，提供修改，查询和 初始化。
* 初始化的意思是：
* 1 这个方法的作用是初始化PropertyValue。
*   为什么要初始化呢？ 因为对于PropertyValue的管理，没有增加，只有修改。 所以需要通过初始化来进行自动地增加，以便于后面的修改。
* 2 首先根据产品获取分类，然后获取这个分类下的所有属性集合
* 3 然后用属性id和产品id去查询，看看这个属性和这个产品，是否已经存在属性值了。
* 4 如果不存在，那么就创建一个属性值，并设置其属性和产品，接着插入到数据库中。
* 这样就完成了属性值的初始化。
* */
@Service
public class PropertyValueService {
    @Autowired PropertyValueDAO propertyValueDAO;
    @Autowired PropertyService propertyService;

    public void update(PropertyValue bean){
        propertyValueDAO.save(bean);
    }

    public void init(Product product){
        List<Property> properties = propertyService.ListbyCatrgory(product.getCategory());
        for(Property property : properties){
            PropertyValue propertyValue = getByPropertyAndProduct(product, property);
            if(null==propertyValue){
                propertyValue = new PropertyValue();
                propertyValue.setProduct(product);
                propertyValue.setProperty(property);
                propertyValueDAO.save(propertyValue);
            }
        }
    }

    public PropertyValue getByPropertyAndProduct(Product product, Property property) {
        return propertyValueDAO.findByPropertyAndProduct(property, product);
    }

    public List<PropertyValue> list(Product product){
        return propertyValueDAO.findByProductOrderByIdDesc(product);
    }
}
