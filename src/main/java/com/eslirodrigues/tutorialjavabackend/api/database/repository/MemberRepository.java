package com.eslirodrigues.tutorialjavabackend.api.database.repository;

import com.eslirodrigues.tutorialjavabackend.api.database.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByIdAndOwner(Long id, String owner);

    boolean existsByIdAndOwner(Long id, String owner);

    Page<Member> findByOwner(String owner, Pageable pageable);

}
