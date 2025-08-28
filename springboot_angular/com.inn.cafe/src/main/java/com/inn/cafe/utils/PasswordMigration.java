//package com.inn.cafe.utils;
//
//import com.inn.cafe.dao.UserDao;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class PasswordMigration {
//
//    @Bean
//    CommandLineRunner migratePasswords(UserDao userDao, PasswordEncoder encoder) {
//        return args -> {
//            var users = userDao.findAll();
//            for (var u : users) {
//                String p = u.getPassword();
//                if (p == null) continue;
//                // crude check: BCrypt hashes start with $2a/$2b/$2y
//                boolean looksBCrypt = p.startsWith("$2a$") || p.startsWith("$2b$") || p.startsWith("$2y$");
//                if (!looksBCrypt) {
//                    u.setPassword(encoder.encode(p));
//                    userDao.save(u);
//                }
//            }
//        };
//    }
//}
