package com.security.config;

import com.security.entity.Role;
import com.security.entity.User;
import com.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 資料初始化器
 *
 * 建立初始測試使用者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 建立管理員使用者
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("系統管理員")
                .build();
            admin.addRole(Role.ROLE_USER);
            admin.addRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            log.info("已建立管理員帳號: admin / admin123");
        }

        // 建立版主使用者
        if (!userRepository.existsByUsername("moderator")) {
            User moderator = User.builder()
                .username("moderator")
                .email("moderator@example.com")
                .password(passwordEncoder.encode("mod123"))
                .fullName("版主")
                .build();
            moderator.addRole(Role.ROLE_USER);
            moderator.addRole(Role.ROLE_MODERATOR);
            userRepository.save(moderator);
            log.info("已建立版主帳號: moderator / mod123");
        }

        // 建立一般使用者
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                .username("user")
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .fullName("一般使用者")
                .build();
            user.addRole(Role.ROLE_USER);
            userRepository.save(user);
            log.info("已建立一般使用者帳號: user / user123");
        }
    }
}
