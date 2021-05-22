package com.hardik.plutocracy.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hardik.plutocracy.dto.request.UserCreationRequestDto;
import com.hardik.plutocracy.dto.request.UserLoginRequestDto;
import com.hardik.plutocracy.dto.request.UserPasswordUpdationRequestDto;
import com.hardik.plutocracy.entity.TotalBalance;
import com.hardik.plutocracy.entity.User;
import com.hardik.plutocracy.repository.TotalBalanceRepository;
import com.hardik.plutocracy.repository.UserRepository;
import com.hardik.plutocracy.security.utility.JwtUtils;
import com.hardik.plutocracy.utils.ResponseUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	private final TotalBalanceRepository totalBalanceRepository;

	private final PasswordEncoder passwordEncoder;

	private final ResponseUtils responseUtils;

	private final JwtUtils jwtUtils;

	public boolean userExists(final String emailId) {
		return userRepository.findByEmailId(emailId).isPresent() ? true : false;
	}

	public ResponseEntity<?> createUser(final UserCreationRequestDto userCreationRequest) {

		if (userExists(userCreationRequest.getEmailId()))
			return responseUtils.duplicateEmailIdResponse();

		final var user = new User();
		user.setEmailId(userCreationRequest.getEmailId());
		user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
		user.setFirstName(userCreationRequest.getFirstName());
		user.setLastName(userCreationRequest.getLastName());
		user.setDateOfBirth(userCreationRequest.getDateOfBirth());

		final var savedUser = userRepository.save(user);

		final var totalBalance = new TotalBalance();
		totalBalance.setUserId(savedUser.getId());
		totalBalanceRepository.save(totalBalance);

		return responseUtils.userCreationSuccessResponse();
	}

	public ResponseEntity<?> login(final UserLoginRequestDto userLoginRequestDto) {
		final var user = userRepository.findByEmailId(userLoginRequestDto.getEmailId());
		if (user.isEmpty())
			return responseUtils.invalidEmailIdResponse();
		if (passwordEncoder.matches(userLoginRequestDto.getPassword(), user.get().getPassword())) {
			final var retreivedUser = user.get();
			final var jwtToken = jwtUtils.generateToken(retreivedUser,
					totalBalanceRepository.findByUserId(retreivedUser.getId()).get().getId());
			return responseUtils.loginSuccessResponse(jwtToken);
		}
		return responseUtils.invalidPasswordResponse();
	}

	public ResponseEntity<?> updatePassword(final UserPasswordUpdationRequestDto userPasswordUpdationRequestDto,
			final String token) {
		final var loggedInUserId = jwtUtils.extractUserId(token.replace("Bearer ", ""));
		final var user = userRepository.findById(loggedInUserId).get();

		if (!passwordEncoder.matches(userPasswordUpdationRequestDto.getOldPassword(), user.getPassword()))
			return responseUtils.invalidPasswordResponse();

		user.setPassword(passwordEncoder.encode(userPasswordUpdationRequestDto.getNewPassword()));
		userRepository.save(user);

		return responseUtils.passwordUpdationSuccessResponse();
	}

}
