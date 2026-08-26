package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
