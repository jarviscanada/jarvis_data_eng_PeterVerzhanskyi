package com.inn.cafe.dao;

import com.inn.cafe.wrapper.UserWrapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.inn.cafe.POJO.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDao extends JpaRepository<User, Integer> {

    // matches @NamedQuery(name = "User.findByEmailId")
    User findByEmailId(@Param("email") String email);

    // matches @NamedQuery(name = "User.getAllUser")
    List<UserWrapper> getAllUser();

    // matches @NamedQuery(name = "User.getAllAdminEmails")
    List<String> getAllAdminEmails();

    // matches @NamedQuery(name = "User.updateStatus")
    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);
}
