package net.javaguides.todo.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.todo.dto.JwtAuthResponse;
import net.javaguides.todo.dto.LoginDto;
import net.javaguides.todo.dto.RegisterDto;
import net.javaguides.todo.entity.Role;
import net.javaguides.todo.entity.User;
import net.javaguides.todo.exception.TodoAPIException;
import net.javaguides.todo.repository.RoleRepository;
import net.javaguides.todo.repository.UserRepository;
import net.javaguides.todo.security.JwtTokenProvider;
import net.javaguides.todo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public String register(RegisterDto registerDto) {

        //check username is already exists in database
        if(userRepository.existsByUsername(registerDto.getUsername())){
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,"Username is already exists!");
        }

        //check email is already exists in database
        if(userRepository.existsByEmail((registerDto.getEmail()))){
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,"Email is already exists");
        }


        // Validate email format
        if (!registerDto.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        // Validate password length
        if (registerDto.getPassword().length() < 8) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles=new HashSet<>();
        Role userRole=roleRepository.findByName("ROLE_USER");
        roles.add(userRole);

        user.setRoles(roles);

        userRepository.save(user);

        return "User Registered Successfully!";
    }

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {

        // 查帳號是否存在
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
                loginDto.getUsernameOrEmail(),
                loginDto.getUsernameOrEmail()
        );

        // 不管帳號有沒有存在，統一只提示「帳號或密碼錯誤」
        if (userOptional.isEmpty()) {
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsernameOrEmail(),
                            loginDto.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        // ✅ 完整保留你原本的使用者資料設定
        if (userOptional.isPresent()) {
            User loggedInUser = userOptional.get();

            jwtAuthResponse.setFirstName(loggedInUser.getFirstName());
            jwtAuthResponse.setLastName(loggedInUser.getLastName());

            Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();
            optionalRole.ifPresent(role -> jwtAuthResponse.setRole(role.getName()));
        }

        return jwtAuthResponse;
    }



//    @Override
//    public JwtAuthResponse login(LoginDto loginDto) {
//
//        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                loginDto.getUsernameOrEmail(),
//                loginDto.getPassword()
//        ));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String token=jwtTokenProvider.generateToken(authentication);
//
//        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
//        jwtAuthResponse.setAccessToken(token);
//
//        Optional<User> userOptional=userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail());
//
//        if (userOptional.isPresent()) {
//            User loggedInUser = userOptional.get();
//
//            // 設定名字
//            jwtAuthResponse.setFirstName(loggedInUser.getFirstName());
//            jwtAuthResponse.setLastName(loggedInUser.getLastName());
//
//            // 設定角色
//            Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();
//            optionalRole.ifPresent(role -> jwtAuthResponse.setRole(role.getName()));
//        }
//
//        return jwtAuthResponse;
//    }

//    @Override
//    public JwtAuthResponse login(LoginDto loginDto) {
//
//        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                loginDto.getUsernameOrEmail(),
//                loginDto.getPassword()
//        ));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String token=jwtTokenProvider.generateToken(authentication);
//
//        Optional<User> userOptional=userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail());
//
//        String role=null;
//        String firstName = null;
//        String lastName = null;
//
//        if(userOptional.isPresent()){
//            User loggedInUser=userOptional.get();
//
//            firstName = loggedInUser.getFirstName();
//            lastName = loggedInUser.getLastName();
//            Optional<Role> optionalRole=loggedInUser.getRoles().stream().findFirst();
//
//            if(optionalRole.isPresent()){
//                Role userRole=optionalRole.get();
//                role=userRole.getName();
//            }
//        }
//
//        JwtAuthResponse jwtAuthResponse=new JwtAuthResponse();
//        jwtAuthResponse.setRole(role);
//        jwtAuthResponse.setFirstName(firstName);
//        jwtAuthResponse.setLastName(lastName);
//        jwtAuthResponse.setAccessToken(token);
//        return jwtAuthResponse;
//    }
}
