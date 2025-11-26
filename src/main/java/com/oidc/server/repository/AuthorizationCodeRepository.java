package com.oidc.server.repository;

import com.oidc.server.entity.AuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, String> {
    Optional<AuthorizationCode> findByCode(String code);
}
