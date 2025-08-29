package com.inn.cafe.serviceimpl;

import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final EmailUtils emailUtils;
    private final UserDao userDao;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(EmailUtils emailUtils,
                           UserDao userDao,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.emailUtils = emailUtils;
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ===================== SIGN UP =====================
    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signUp {}", requestMap);
        try {
            if (!validateSignUpMap(requestMap)) {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            User existing = userDao.findByEmailId(requestMap.get("email"));
            if (existing != null) {
                return CafeUtils.getResponseEntity("Email already exists", HttpStatus.BAD_REQUEST);
            }

            User toSave = getUserFromMap(requestMap);
            // BCrypt encode here (only on write)
            toSave.setPassword(passwordEncoder.encode(requestMap.get("password")));
            userDao.save(toSave);

            return CafeUtils.getResponseEntity("Successfully registered", HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error in signUp", ex);
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("name")
                && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email")
                && requestMap.containsKey("password");
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        // password set separately (encoded) in signUp()
        user.setStatus("false"); // pending admin approval, per your flow
        user.setRole("user");
        return user;
    }

    // ===================== LOGIN =====================
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            String email = requestMap.get("email");
            String password = requestMap.get("password");

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            if (!auth.isAuthenticated()) {
                return new ResponseEntity<>("{\"message\":\"Bad Credentials.\"}", HttpStatus.UNAUTHORIZED);
            }

            User dbUser = userDao.findByEmailId(email);
            if (dbUser == null) {
                return new ResponseEntity<>("{\"message\":\"User not found.\"}", HttpStatus.UNAUTHORIZED);
            }

            if (!"true".equalsIgnoreCase(dbUser.getStatus())) {
                return new ResponseEntity<>("{\"message\":\"Wait for admin approval.\"}", HttpStatus.FORBIDDEN);
            }

            String token = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole());
            return new ResponseEntity<>("{\"token\":\"" + token + "\"}", HttpStatus.OK);

        } catch (BadCredentialsException bce) {
            log.warn("Bad credentials for email {}", requestMap.get("email"));
            return new ResponseEntity<>("{\"message\":\"Bad Credentials.\"}", HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("Login error", ex);
            return new ResponseEntity<>("{\"message\":\"" + CafeConstants.SOMETHING_WENT_WRONG + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== ADMIN: GET ALL USERS =====================
    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if (!isCurrentUserAdmin()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("getAllUser error", ex);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== ADMIN: UPDATE USER STATUS =====================
    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (!isCurrentUserAdmin()) {
                return CafeUtils.getResponseEntity(CafeConstants.UNATHORIZED_ACCESS, HttpStatus.FORBIDDEN);
            }

            String idStr = requestMap.get("id");
            String status = requestMap.get("status");
            if (idStr == null || status == null) {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            int id = Integer.parseInt(idStr);
            Optional<User> optional = userDao.findById(id);
            if (optional.isEmpty()) {
                return CafeUtils.getResponseEntity("User id does not exist", HttpStatus.NOT_FOUND);
            }

            userDao.updateStatus(status, id);

            // Notify all admins except the current approver
            User targetUser = optional.get();
            sendMailToAllAdmin(status, targetUser.getEmail(), userDao.getAllAdminEmails());

            return CafeUtils.getResponseEntity("Successfully updated user status", HttpStatus.OK);

        } catch (NumberFormatException nfe) {
            return CafeUtils.getResponseEntity("Invalid user id", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("update error", ex);
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===================== EMAIL NOTIFY HELPERS =====================
    private void sendMailToAllAdmin(String status, String affectedUserEmail, List<String> allAdminEmails) {
        String current = getCurrentUsername();
        if (current != null) {
            // remove the approver from recipients
            allAdminEmails.removeIf(email -> email != null && email.equalsIgnoreCase(current));
        }

        String subject;
        String body;

        if ("true".equalsIgnoreCase(status)) {
            subject = "Account Approved";
            body = "USER: " + affectedUserEmail + "\n" +
                    "has been approved by ADMIN: " + (current != null ? current : "unknown");
        } else if ("false".equalsIgnoreCase(status)) {
            subject = "Account Disabled";
            body = "USER: " + affectedUserEmail + "\n" +
                    "has been disabled by ADMIN: " + (current != null ? current : "unknown");
        } else {
            subject = "Account Status Changed";
            body = "USER: " + affectedUserEmail + "\n" +
                    "status changed to: " + status + " by ADMIN: " + (current != null ? current : "unknown");
        }

        // Send to each admin (skip empty list safely)
        for (String adminEmail : allAdminEmails) {
            if (adminEmail == null || adminEmail.isBlank()) continue;
            try {
                emailUtils.sendSimpleMessage(adminEmail, subject, body, null);
            } catch (Exception ex) {
                log.warn("Failed to notify admin {} about status change of {}", adminEmail, affectedUserEmail, ex);
            }
        }
    }

    // ===================== SECURITY HELPERS =====================
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(a.getAuthority())) return true;
        }
        return false;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
}
