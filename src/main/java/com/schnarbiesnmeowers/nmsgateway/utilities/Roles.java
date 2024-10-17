package com.schnarbiesnmeowers.nmsgateway.utilities;

import java.util.List;

import static com.schnarbiesnmeowers.nmsgateway.utilities.Authorizations.*;

/**
 * 
 * @author Dylan I. Kessler
 *
 */
public enum Roles {

	ROLE_BASIC_USER(BASIC_USER_AUTH),
	ROLE_ADV_USER(ADV_USER_AUTH),
	ROLE_PREMIUM_USER(PREMIUM_USER_AUTH),
	ROLE_ADMIN(ADMIN_AUTH),
	ROLE_SUPER(SUPER_ADMIN_AUTH);
	
	private List<String> authorizations;
	
	Roles(List<String> authorizations) {
		this.authorizations = authorizations;
	}
	
	public List<String> getAuthorizations() {
		return this.authorizations;
	}
}
