package com.mykola.keep.labelservice.repository;

import com.mykola.keep.labelservice.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByUsernameOrderByCreatedAtDesc(String username);
    Optional<Label> findByIdAndUsername(Long id, String username);
    boolean existsByUsernameAndNameIgnoreCase(String username, String name);
    List<Label> findByIdInAndUsername(Collection<Long> ids, String username);
}
