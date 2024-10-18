package com.schnarbiesnmeowers.nmsgateway.repositories;

import com.schnarbiesnmeowers.nmsgateway.entities.AppUserTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public interface AppUserTempRepository extends ReactiveCrudRepository<AppUserTemp, Integer> {

	Mono<AppUserTemp> findUserByUserName(String userName);
	Mono<AppUserTemp> findUserByUniqueId(String uniqueId);
}
