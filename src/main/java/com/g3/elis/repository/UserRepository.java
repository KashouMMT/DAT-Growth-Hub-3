package com.g3.elis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g3.elis.model.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

	User findByEmail(String email);
	
	public Optional<User> findByStaffId(String search);
	
//	@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_STUDENT'")
//	public List<User> findByNameContainingIgnoreCase(String name);

	@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role AND LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
	Page<User> findByNameContainingIgnoreCaseAndRole(@Param("name") String name, @Param("role") String role, Pageable pageable);

	@Query("SELECT u.email FROM User u JOIN u.roles r WHERE r.name = :role")
	List<String> findEmailsByRole(String role);
	
	@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
	Page<User> findByRole(String role, Pageable pageable);

	@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_INSTRUCTOR' AND " +
	           "(:name IS NULL OR u.name LIKE %:name%) AND " +
	           "(:staffId IS NULL OR u.staffId LIKE %:staffId%) AND " +
	           "(:dept IS NULL OR u.dept LIKE %:dept%) AND " +
	           "(:division IS NULL OR u.division LIKE %:division%)")
	    Page<User> searchInstructors(@Param("name") String name, 
	                                 @Param("staffId") String staffId, 
	                                 @Param("dept") String dept, 
	                                 @Param("division") String division, Pageable pageable);

	

	
    
}
