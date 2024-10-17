package com.schnarbiesnmeowers.nmsgateway.repositories;

import com.schnarbiesnmeowers.nmsgateway.entities.AppUserTemp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Dylan I. Kessler
 *
 */
public interface AppUserTempRepository extends JpaRepository<AppUserTemp, Integer>{

	AppUserTemp findUserByUserName(String userName);
	AppUserTemp findUserByUniqueId(String uniqueId);
}
