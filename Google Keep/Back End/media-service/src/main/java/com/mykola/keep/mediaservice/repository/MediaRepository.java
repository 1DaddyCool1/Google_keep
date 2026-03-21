package com.mykola.keep.mediaservice.repository;

import com.mykola.keep.mediaservice.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByIdAndUsername(Long id, String username);
    List<Media> findByIdInAndUsername(List<Long> ids, String username);
}
