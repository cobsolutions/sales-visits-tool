package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    @EntityGraph(attributePaths = {"teamLeader", "createdBy"})
    List<User> findAll();

    @Query("""
    SELECT u FROM User u
    WHERE u.role IN (
        com.sales.visits.app.sales.model.enums.UserRole.TEAM_LEADER,
        com.sales.visits.app.sales.model.enums.UserRole.SALES_REP
    )
      AND u.status = com.sales.visits.app.sales.model.enums.UserStatus.ACTIVE
      AND u.id <> :excludeUserId
    ORDER BY u.username
    """)
    List<User> findEligibleJoinedVisitors(Long excludeUserId);

    @Query("""
    SELECT u.id
    FROM User u
    WHERE u.teamLeader.id = :teamLeaderId
""")
    List<Long> findIdsByTeamLeaderId(@Param("teamLeaderId") Long teamLeaderId);
}
