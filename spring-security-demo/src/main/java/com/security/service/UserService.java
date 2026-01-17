package com.security.service;

import com.security.entity.User;
import com.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 使用者服務
 *
 * 實作 UserDetailsService 以整合 Spring Security
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根據使用者名稱載入使用者詳情
     * Spring Security 會在認證時呼叫此方法
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("使用者不存在: " + username));
    }

    /**
     * 根據 ID 查詢使用者
     */
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("使用者不存在: ID=" + id));
    }

    /**
     * 儲存使用者
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * 檢查使用者名稱是否已存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 檢查電子郵件是否已存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
