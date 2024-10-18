package com.schnarbiesnmeowers.nmsgateway.repositories;

import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Integer> {

	Mono<AppUser> findUserByUserName(String userName);
	Mono<AppUser> findUserByEmail(String emailAddr);
	Mono<AppUser> findUserByUserIdentifier(String userIdentifier);
	@Query(value = "Select u from user u where u.roles in (?1)",nativeQuery = true)
	Flux<AppUser> findByRoleTypes(String roles);
}
