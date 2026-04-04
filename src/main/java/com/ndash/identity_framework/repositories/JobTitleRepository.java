package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {

    Optional<JobTitle> findByNameAndExternalSource(String name, String source);
}
