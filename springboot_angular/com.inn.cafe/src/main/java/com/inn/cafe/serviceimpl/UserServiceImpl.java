package com.inn.cafe.serviceimpl;

import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserDao userDao,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

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

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            String email = requestMap.get("email");
            String password = requestMap.get("password");

            // Authenticate against UserDetailsService + BCrypt (no manual encoding here)
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            if (!auth.isAuthenticated()) {
                return new ResponseEntity<>("{\"message\":\"Bad Credentials.\"}", HttpStatus.UNAUTHORIZED);
            }

            // Load the user directly from DB (don’t rely on a side-effect in your UserDetailsService)
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
}
