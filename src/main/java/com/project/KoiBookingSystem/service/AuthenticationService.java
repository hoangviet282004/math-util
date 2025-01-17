package com.project.KoiBookingSystem.service;

import com.project.KoiBookingSystem.entity.Account;
import com.project.KoiBookingSystem.enums.Role;
import com.project.KoiBookingSystem.exception.AuthenticationException;
import com.project.KoiBookingSystem.exception.DuplicatedEntity;
import com.project.KoiBookingSystem.exception.NotFoundException;
import com.project.KoiBookingSystem.model.request.LoginRequest;
import com.project.KoiBookingSystem.model.request.RegisterRequest;
import com.project.KoiBookingSystem.model.response.EmailDetail;
import com.project.KoiBookingSystem.model.response.LoginResponse;
import com.project.KoiBookingSystem.model.response.RegisterResponse;
import com.project.KoiBookingSystem.repository.AccountRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    TokenService tokenService;

    @Autowired
    EmailService emailService;

    public RegisterResponse registerAccount(RegisterRequest registerRequest) {
        // Sử dụng modelmapper để map RegisterRequest => Account
        Account account = modelMapper.map(registerRequest, Account.class);
        try {
            String passwordStr = account.getPassword(); // Password do người dùng nhập
            account.setPassword(passwordEncoder.encode(passwordStr)); // mã hóa Password do người dùng nhập trước khi lưu xuống database
//            account.setRole(Role.CUSTOMER);
            String userId = generateUserID(account.getRole());
            account.setUserID(userId);
            account.setCreatedDate(LocalDate.now());
            account.setStatus(true);
            Account newAccount = accountRepository.save(account);

            return modelMapper.map(newAccount, RegisterResponse.class);
        } catch (DataIntegrityViolationException e) {
            handleDuplicateData(e, account);
        }
        return null;
    }

    public RegisterResponse registerManagerAccount(RegisterRequest registerRequest) {
        // Sử dụng modelmapper để map RegisterRequest => Account
        Account account = modelMapper.map(registerRequest, Account.class);
        try {
            String passwordStr = account.getPassword(); // Password do người dùng nhập
            account.setPassword(passwordEncoder.encode(passwordStr)); // mã hóa Password do người dùng nhập trước khi lưu xuống database
            account.setRole(Role.MANAGER);
            String userId = generateUserID(account.getRole());
            account.setUserID(userId);
            account.setCreatedDate(LocalDate.now());
            account.setStatus(true);
            Account newAccount = accountRepository.save(account);

            return modelMapper.map(newAccount, RegisterResponse.class);
        } catch (DataIntegrityViolationException e) {
            handleDuplicateData(e, account);
        }
        return null;
    }

    public RegisterResponse registerAccountByManager(RegisterRequest registerRequest) {
        Account account = modelMapper.map(registerRequest, Account.class);

        try {
            String passwordStr = account.getPassword();
            account.setPassword(passwordEncoder.encode(passwordStr));

            Account currentAccount = getCurrentAccount();
            if (!currentAccount.getRole().equals(Role.MANAGER)) {
                throw new AuthenticationException("Only Manager can access this!");
            }

            if (registerRequest.getRole() != null) {
                account.setRole(registerRequest.getRole());
            }
//            } else {
//                account.setRole(Role.CUSTOMER);
//            }

            String userId = generateUserID(account.getRole());
            account.setUserID(userId);
            account.setCreatedDate(LocalDate.now());
            account.setStatus(true);

            Account newAccount = accountRepository.save(account);
            return modelMapper.map(newAccount, RegisterResponse.class);
        } catch (DataIntegrityViolationException e) {
            handleDuplicateData(e, account);
        }
        return null;
    }

    public LoginResponse loginAccount(LoginRequest loginRequest) {
        // cung cấp tài khoản và mật khẩu để đăng nhập
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            ));
            Account account = (Account) authentication.getPrincipal();
            LoginResponse loginResponse = modelMapper.map(account, LoginResponse.class); // trả về thông tin người dùng
            loginResponse.setToken(tokenService.generateToken(account)); // nhờ tokenService generate token cho account vừa mới đăng nhập thành công
            return loginResponse;
        } catch (Exception e) {
            throw new NotFoundException("Username or Password Invalid!");
        }
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return accountRepository.findAccountByUsername(username);
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("Account not found: " + username);
        }
        return account;  // Trả về đối tượng Account, không phải String
    }


    public String generateUserID(Role role) {
        String prefix = "";
        switch (role) {
            case CUSTOMER:
                prefix = "CU";
                break;
            case SALES:
                prefix = "SS";
                break;
            case CONSULTING:
                prefix = "CS";
                break;
            case DELIVERING:
                prefix = "DS";
                break;
            case MANAGER:
                prefix = "MG";
                break;
            case ADMIN:
                prefix = "AD";
                break;
            default:
                throw new IllegalArgumentException("Invalid role " + role);
        }
        long countRole = accountRepository.countByRole(role);
        return prefix + (countRole + 1);
    }



    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Account) authentication.getPrincipal();
    }




    private void handleDuplicateData(DataIntegrityViolationException e, Account account) {
        if (e.getMessage().contains(account.getUsername())) {
            throw new DuplicatedEntity("Duplicated Username!");
        } else if (e.getMessage().contains(account.getPhone())) {
            throw new DuplicatedEntity("Duplicated Phone number!");
        } else {
            throw new DuplicatedEntity("Duplicated Email!");
        }
    }
}
