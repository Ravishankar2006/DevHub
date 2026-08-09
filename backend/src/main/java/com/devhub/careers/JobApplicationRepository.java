package com.devhub.careers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.company LEFT JOIN FETCH ja.resume "
            + "WHERE ja.user.id = :userId ORDER BY ja.updatedAt DESC")
    List<JobApplication> findByUserIdOrderByUpdatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.company LEFT JOIN FETCH ja.resume "
            + "WHERE ja.user.id = :userId AND ja.status = :status ORDER BY ja.updatedAt DESC")
    List<JobApplication> findByUserIdAndStatusOrderByUpdatedAtDesc(
            @Param("userId") UUID userId, @Param("status") JobApplicationStatus status);

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.company LEFT JOIN FETCH ja.resume "
            + "WHERE ja.user.id = :userId AND ja.company.id = :companyId ORDER BY ja.updatedAt DESC")
    List<JobApplication> findByUserIdAndCompanyIdOrderByUpdatedAtDesc(
            @Param("userId") UUID userId, @Param("companyId") UUID companyId);

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.company LEFT JOIN FETCH ja.resume "
            + "WHERE ja.user.id = :userId AND ja.status = :status AND ja.company.id = :companyId ORDER BY ja.updatedAt DESC")
    List<JobApplication> findByUserIdAndStatusAndCompanyIdOrderByUpdatedAtDesc(
            @Param("userId") UUID userId, @Param("status") JobApplicationStatus status, @Param("companyId") UUID companyId);

    List<JobApplication> findByResumeId(UUID resumeId);
}
