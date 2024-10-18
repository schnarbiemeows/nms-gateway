package com.schnarbiesnmeowers.nmsgateway.repositories;

import com.schnarbiesnmeowers.nmsgateway.entities.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public interface PasswordResetRepository extends ReactiveCrudRepository<PasswordReset, Integer> {

	Mono<PasswordReset> findUserByEmailAddr(String emailAddr);
	Mono<PasswordReset> findUserByUniqueId(String uniqueId);
}
