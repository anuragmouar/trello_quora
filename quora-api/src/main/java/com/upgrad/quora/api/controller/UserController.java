package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;

import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.business.UserService;

import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

/**
 * This controller class will serve http requests related to User.
 * @author anuragmouar
 */
@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * This endpoint is used to register a new user in the Quora Application.
     *
     * @param signupUserRequest
     * @return
     * @throws SignUpRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        final UserEntity userEntity = new UserEntity();

        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutme(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());
        userEntity.setSalt("1234abc");

        final UserEntity createdUserEntity = userService.createUser(userEntity);
        SignupUserResponse userResponse = new SignupUserResponse();
        userResponse.setId(createdUserEntity.getUuid());
        userResponse.setStatus("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }

    /**
     * This endpoint is used for user authentication. The user authenticates in the application and after successful authentication, JWT token is given to a user.
     *
     * @param authorization
     * @return
     * @throws AuthenticationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        UserAuthEntity userAuthEntity = userService.signIn(decodedArray[0], decodedArray[1]);
        UserEntity user = userAuthEntity.getUser();

        SigninResponse signinResponse = new SigninResponse();
        signinResponse.setId(user.getUuid());
        signinResponse.setMessage("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthEntity.getAccessToken());
        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    /**
     * This endpoint is used to sign out from the Quora Application. The user cannot access any other endpoint once he is signed out of the application.
     *
     * @param authorization
     * @return
     * @throws SignOutRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST, path = "user/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(@RequestHeader("authorization") final String authorization) throws SignOutRestrictedException {

        String accessToken = authorization.split("Bearer ")[0];

        UserAuthEntity userAuthEntity = userService.signOut(accessToken);
        UserEntity user = userAuthEntity.getUser();

        SignoutResponse signoutResponse = new SignoutResponse();
        signoutResponse.setId(user.getUuid());
        signoutResponse.setMessage("SIGNED OUT SUCCESSFULLY");

        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }
}
