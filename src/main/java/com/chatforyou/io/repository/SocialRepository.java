package com.chatforyou.io.repository;

import com.chatforyou.io.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialRepository extends JpaRepository<SocialUser, Long> {
    Optional<SocialUser> findSocialUserByProviderAccountIdAndAndProvider(String providerAccountId, String provider);
}