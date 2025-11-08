package com.dhyey.chatapp_withjpa.repositories;

import com.dhyey.chatapp_withjpa.dto.GlobalUsersSearch;
import com.dhyey.chatapp_withjpa.dto.LoggedUserData;
import com.dhyey.chatapp_withjpa.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(Long userId);

    Optional<User> findByEmailAndPassword(String email, String password);

    Optional<User> findByUsernameAndPassword(String username, String password);

    @Query("""
        select new com.dhyey.chatapp_withjpa.dto.LoggedUserData(
            u.profileName,
            u.userId,
            u.email,
            u.username
        ) from User u where u.userId = :userId
    """)
    Optional<LoggedUserData> findUserByLoggedUserId(Long userId);

    @Query("""
    select new com.dhyey.chatapp_withjpa.dto.GlobalUsersSearch(
    u.userId,
    u.profileName,
    u.username,
    u.avatar
    )
    from User u where LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))
                             OR LOWER(u.profileName) LIKE LOWER(CONCAT('%', :profileName, '%'))
    """)
    List<GlobalUsersSearch> findByUsernameContainingIgnoreCaseOrProfileNameContainingIgnoreCase(
           @Param("username") String username, @Param("profileName") String profileName
    );

    @Query("""
    SELECT u 
    FROM User u
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%'))
       OR LOWER(u.profileName) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    List<GlobalUsersSearch> findGlobalUsersQueryMethod(@Param("term") String term);
}