package com.rishit.grid07.repository;

import com.rishit.grid07.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
