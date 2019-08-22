package com.tmall.web;

import com.tmall.pojo.Property;
import com.tmall.service.PropertyService;
import com.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PropertyController {

    @Autowired
    PropertyService propertyService;

    @GetMapping(value = "/categories/{cid}/properties")
    public Page4Navigator<Property> list(@PathVariable(value = "cid") int cid, @RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "5") int size){
        start = start < 0 ? 0 : start;
        Page4Navigator<Property> page = propertyService.list(cid, start, size, 5);
        return page;
    }

    @GetMapping(value = "/properties/{id}")
    public Property get(@PathVariable(value = "id") int id){
        Property bean = propertyService.get(id);
        return bean;
    }

    @PostMapping(value = "/properties")
    public Object add(@RequestBody Property bean){
        propertyService.add(bean);
        return bean;
    }

    @DeleteMapping(value = "/properties/{id}")
    public String delete(@PathVariable(value = "id") int id){
        propertyService.delete(id);
        return null;
    }

    @PutMapping(value = "/properties")
    public Object update(@RequestBody Property bean){
        propertyService.update(bean);
        return bean;
    }
}
