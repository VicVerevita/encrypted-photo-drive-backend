// PhotoRepository.java
package com.example.demo.repository;

import com.example.demo.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO photos (data, subject, user_id, encrypted) VALUES (CAST(:data AS BYTEA), :subject, :userId, :encrypted)", nativeQuery = true)
    void savePhoto(@Param("data") byte[] data, @Param("subject") String subject, @Param("userId") Long userId, @Param("encrypted") boolean encrypted);

    @Modifying
    @Transactional
    @Query("UPDATE Photo p SET p.data = :data, p.subject = :subject, p.encrypted = :encrypted WHERE p.id = :id")
    void updatePhoto(@Param("data") byte[] data, @Param("subject") String subject, @Param("encrypted") boolean encrypted, @Param("id") Long id);
}