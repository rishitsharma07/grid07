package com.rishit.grid07.repository;

import com.rishit.grid07.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {
}
