package com.schnarbiesnmeowers.nmsgateway.repositories;

import com.schnarbiesnmeowers.nmsgateway.entities.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer>{

	PasswordReset findUserByEmailAddr(String emailAddr);
	PasswordReset findUserByUniqueId(String uniqueId);
}
