package com.schnarbiesnmeowers.nmsgateway.services;

import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTO;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserDTOWrapper;
import com.schnarbiesnmeowers.nmsgateway.dtos.AppUserTempDTO;
import com.schnarbiesnmeowers.nmsgateway.exceptions.ResourceNotFoundException;
import com.schnarbiesnmeowers.nmsgateway.entities.AppUser;
import com.schnarbiesnmeowers.nmsgateway.repositories.AppUserRepository;
import com.schnarbiesnmeowers.nmsgateway.utilities.Roles;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import com.schnarbiesnmeowers.nmsgateway.exceptions.user.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class retrieves data from the controller class
 * most business logic should be put in this class
 * @author Dylan I. Kessler
 *
 */
@Component
public class UserBusinessService {

	private static final Logger applicationLogger = LogManager.getLogger("FileAppender");
    public static final String ID_EQUALS = "id = ";
    public static final String NOT_FOUND = " not found";
    public static final String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
	/**
	 * JPA Repository handle
	 */
	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	UserService userBusinessService;
	/**
	 * get all AppUser records
	 * @return
	 * @throws Exception
	 */
	public List<AppUserDTO> getAllAppUser() throws Exception {
		Iterable<AppUser> interviewuser = appUserRepository.findAll();
		Iterator<AppUser> interviewusers = interviewuser.iterator();
		List<AppUserDTO> interviewuserdto = new ArrayList();
		while(interviewusers.hasNext()) {
			AppUser item = interviewusers.next();
			interviewuserdto.add(item.toDTO());
		}
		return interviewuserdto;
	}

	/**
	 * get AppUser by primary key
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public AppUserDTO findAppUserById(int id) throws Exception {
		Optional<AppUser> interviewuserOptional = appUserRepository.findById(id);
		if(interviewuserOptional.isPresent()) {
			AppUser results = interviewuserOptional.get();
			return results.toDTO();
		} else {
			throw new ResourceNotFoundException(ID_EQUALS + id + NOT_FOUND);
		}
	}

	/**
	 * create a new AppUser
	 * @param data
	 * @return
	 * @throws EmailExistsException 
	 * @throws UsernameExistsException 
	 * @throws UserNotFoundException 
	 */
	public AppUserDTO createAppUser(AppUserDTO data, String[] authorizations, String adminUser)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException {
		userBusinessService.validateNewUsernameAndEmail(StringUtils.EMPTY,data.getUserName(),data.getEmail());
		AppUser user = new AppUser();
		AppUserDTOWrapper userwrapper = new AppUserDTOWrapper(data);
		userwrapper.setNewUserName(userwrapper.getUserName());
		copyFormPropertiesToUserRecord(user,userwrapper,authorizations, adminUser, user.getUserName());
		String userId = userBusinessService.generateUserIdentifier();
		user.setUserIdentifier(userId);
		user.setJoinDate(LocalDate.now());
		user = appUserRepository.save(user);
		return user.toDTO();
	}

	/**
	 * update a AppUser
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public AppUserDTO updateAppUser(AppUserDTOWrapper data, String[] authorizations, String adminUser)
			throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, AccessDeniedException {
		AppUser user = userBusinessService.validateNewUsernameAndEmail(data.getUserName(),data.getNewUserName(),data.getEmail());
		copyFormPropertiesToUserRecord(user,data,authorizations, adminUser, user.getUserName());
		user = appUserRepository.save(user);
		return user.toDTO();
	}

	/**
	 * delete a AppUser by primary key
	 * we are either deleting an admin, or deleting a user
	 *
	 * @return
	 * @throws Exception
	 */
	public String deleteAppUser(String username, String[] authorizations, String adminUser) throws ResourceNotFoundException, AccessDeniedException {
		try {
			AppUser user = userBusinessService.findUserByUsername(username);
			if(!userEqualsUser(adminUser, username) && adminRoleHigherThanUserRole(user.getRoles(),authorizations)) {
				appUserRepository.deleteById(user.getUserId());
			} else {
				throw new AccessDeniedException("You do not have enough permissions to perform this action.");
			}
		}
		catch(ResourceNotFoundException e) {
			throw new ResourceNotFoundException(ID_EQUALS + username + NOT_FOUND);
		}
		return "Successfully Deleted";
	}

	/**
	 * this method will copy all of the new properties to the AppUser record
	 * for this method, the only one of the "new" fields that I am going to use is the
	 * newUserName, because I need to retain the old one to make sure I know which record to look for
	 * in the database, since the UserId is never passed out to the UI
	 * @param user
	 * @param data
	 */
	private void copyFormPropertiesToUserRecord(AppUser user, AppUserDTOWrapper data, String[] authorizations, String adminUser, String userName) throws AccessDeniedException {
		if(userEqualsUser(adminUser, userName) || adminRoleHigherThanUserRole(data.getRoles(),authorizations)) {
			user.setUserIdentifier(data.getUserIdentifier());
			if(data.getPassword()!=null&&!data.getPassword().isEmpty()) {
				String encodedPassword = userBusinessService.encodePassword(data.getPassword());
				user.setPassword(encodedPassword);
			}
			user.setFirstName(data.getFirstName());
			user.setLastName(data.getLastName());
			user.setUserName(data.getNewUserName());
			user.setEmail(data.getEmail());
			user.setActv(data.isActv());
			user.setUserNotLocked(data.isUserNotLocked());
			setRolesAndAuthorization(user, data);
			user.setUserIdentifier(data.getUserIdentifier());
			user.setProfileImage(userBusinessService.getTemporaryImageUrl(data.getProfileImage()));
		} else {
			throw new AccessDeniedException("You do not have enough permissions to perform this action.");
		}
	}

	/**
	 * this method sets the user's roles and authorizations
	 * @param user
	 * @param data
	 * @throws AccessDeniedException
	 */
	private void setRolesAndAuthorization(AppUser user, AppUserDTOWrapper data) throws AccessDeniedException {
		String role = data.getRoles();
		switch(role) {
			case "ROLE_BASIC_USER" : 
				user.setRoles(Roles.ROLE_BASIC_USER.name());
				user.setAuthorizations(Roles.ROLE_BASIC_USER.getAuthorizations());
				break;
			case "ROLE_ADV_USER" :
				user.setRoles(Roles.ROLE_ADV_USER.name());
				user.setAuthorizations(Roles.ROLE_ADV_USER.getAuthorizations());
				break;
			case "ROLE_PREMIUM_USER" :
				user.setRoles(Roles.ROLE_PREMIUM_USER.name());
				user.setAuthorizations(Roles.ROLE_PREMIUM_USER.getAuthorizations());
				break;
			case "ROLE_ADMIN" :
				user.setRoles(Roles.ROLE_ADMIN.name());
				user.setAuthorizations(Roles.ROLE_ADMIN.getAuthorizations());
				break;
			case "ROLE_SUPER" :
				user.setRoles(Roles.ROLE_SUPER.name());
				user.setAuthorizations(Roles.ROLE_SUPER.getAuthorizations());
				break;
			default :
				throw new AccessDeniedException("You do not have enough permissions to perform this action.");
		}
				
	}

	/**
	 * this method determines if the person changing/deleting a record is the same as the record
	 * - if the person is changing their own record, they can change it, but not delete it
	 * @param adminUser
	 * @param userName
	 * @return
	 */
	private boolean userEqualsUser(String adminUser, String userName) {
		if(adminUser.equals(userName)) return true;
		return false;
	}
	/**
	 * this method determines if the person changing the record has the right permissions to change it
	 * we need this method because this could be an admin changing a user's record, a super admin
	 * changing another admin's record, or even an admin changing their OWN record
	 * @param roles = the role level of the record to be changed
	 * @param authorizations = the authorizations of the person making the change
	 *
	 * @return
	 */
	private boolean adminRoleHigherThanUserRole(String roles, String[] authorizations) {
		// super admins can change any record
		String authJoin = String.join(",", authorizations);
		if(authJoin.contains("admin:create") || authJoin.contains("admin:update") || authJoin.contains("admin:delete")) return true;
		// finally, if it is an admin, changing a user record, they can
		boolean hasAuthority = false;
		switch(roles) {
			case "ROLE_BASIC_USER" : 
				hasAuthority = true;
				break;
			case "ROLE_ADV_USER" :
				hasAuthority = true;
				break;
			case "ROLE_PREMIUM_USER" :
				hasAuthority = true;
				break;
			default :
				hasAuthority = false;
				break;
		}
		return hasAuthority;
	}

	/**
	 * this method checks to make sure the registration data that a new user has entered is valid
	 * @param user
	 * @throws UserFieldsNotValidException
	 */
	public void validateFields(AppUserTempDTO user) throws UserFieldsNotValidException {
		logAction("validating the registration fields");
		if(user.getUserName()==null||user.getUserName().isEmpty()) {
			logAction("username is empty");
			throw new UserFieldsNotValidException("username must have a value");
		}
		if(user.getEmail()==null||user.getEmail().isEmpty()) {
			logAction("email address is empty");
			throw new UserFieldsNotValidException("email must have a value");
		}
		if(user.getFirstName()==null||user.getFirstName().isEmpty()) {
			logAction("first name is empty");
			throw new UserFieldsNotValidException("first name must have a value");
		}
		if(user.getLastName()==null||user.getLastName().isEmpty()) {
			logAction("last name is empty");
			throw new UserFieldsNotValidException("last name must have a value");
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(user.getEmail());
		if(!matcher.matches()) {
			logAction("email address is not valid");
			throw new UserFieldsNotValidException("this email address is not valid");
		}
	}
	
	/**
	 * logging method
	 * 
	 * @param message
	 */
	private static void logAction(String message) {
		System.out.println("AppUserBusiness: " + message);
		applicationLogger.debug("AppUserBusiness: " + message);
	}

}
