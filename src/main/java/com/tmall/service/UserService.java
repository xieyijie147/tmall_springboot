package com.tmall.service;

import com.tmall.dao.UserDAO;
import com.tmall.pojo.User;
import com.tmall.util.Page4Navigator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired UserDAO userDAO;

    //判断某个名称是否已经被使用过了
    public boolean isExist(String name){
        User user = getByName(name);
        return null != user;
    }

    private User getByName(String name) {
        return userDAO.findByName(name);
    }

    public Page4Navigator<User> list(int start, int size, int navigatePages){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        Page pageFromJPA = userDAO.findAll(pageable);
        return new Page4Navigator<>(pageFromJPA, navigatePages);
    }

    public void add(User user){
        userDAO.save(user);
    }

    public User get(String name, String password){
        return userDAO.getByNameAndAndPassword(name, password);
    }
}
