package com.schnarbiesnmeowers.nmsgateway.utilities;

import javax.swing.plaf.basic.BasicLabelUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * the class that defines our permissions:
 * data:select - user can view the questions, answers, question levels, and question categories
 * data:update - user can update any questions, answers, question levels, and question categories
 * data:create - user can create questions, answers, question levels, and question categories
 * data:delete - user can delete any questions, answers, question levels, and question categories
 * self:udate - user or admin can update their personal data
 * self:lvl1 - user can store basic aggregate data about their testing performance
 * self:lvl2 - user can store more option with their testing performance
 * admin:select - admin can view all admin data and user data
 * admin:update - admin can create users(non-admin)
 * admin:create - admin can create other admins
 * admin:delete - admin can delete admins
 * user:select - admin can view all user(non-admin) data
 * user:update - admin can update all user(non-admin) data
 * user:create - admin can create users(non-admin)
 * user:delete - admin can delete users(non-admin)
 * 
 * @author Dylan I. Kessler
 *
 */
public class Authorizations {

	
	public static final List<String> BASIC_USER_AUTH ;
	public static final List<String> ADV_USER_AUTH ;
	public static final List<String> PREMIUM_USER_AUTH;
	public static final List<String> ADMIN_AUTH ;
	public static final List<String> SUPER_ADMIN_AUTH ;

	static {
		String[] basic_auth = { "data:select", "self:update" };
		String[] adv_auth = { "data:select", "self:lvl1", "self:update" };
		String[] prem_auth = { "data:select", "self:lvl1", "self:lvl2", "self:update" };
		String[] admin_auth = { "data:select","data:update","data:create","data:delete",
				"self:lvl1", "self:lvl2", "admin:select","self:update","user:select","user:update",
				"user:create","user:delete" };
		String[] super_auth = { "data:select","data:update","data:create","data:delete",
				"self:lvl1", "self:lvl2", "admin:select","self:update", "admin:update","admin:create","admin:delete",
				"user:select","user:update","user:create","user:delete" };
		BASIC_USER_AUTH = Arrays.asList(basic_auth);
		ADV_USER_AUTH = Arrays.asList(adv_auth);
		PREMIUM_USER_AUTH = Arrays.asList(prem_auth);
		ADMIN_AUTH = Arrays.asList(admin_auth);
		SUPER_ADMIN_AUTH = Arrays.asList(super_auth);
	}
}
