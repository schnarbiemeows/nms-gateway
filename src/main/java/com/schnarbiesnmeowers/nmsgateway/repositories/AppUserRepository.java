package com.schnarbiesnmeowers.nmsgateway.repositories;

import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public interface AppUserRepository extends JpaRepository<AppUser, Integer>{

	AppUser findUserByUserName(String userName);
	AppUser findUserByEmail(String emailAddr);
	AppUser findUserByUserIdentifier(String userIdentifier);
	@Query(value = "Select u from user u where u.roles in (?1)",nativeQuery = true)
	List<AppUser> findByRoleTypes(String roles);
}
