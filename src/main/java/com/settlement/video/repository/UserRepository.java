package com.settlement.video.repository;


import com.settlement.video.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.invoke.VarHandle;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByKakaoId(Long kakaoId);

}
