package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.exception.RegistrationException;
import book.shop.spring.boot.intro.mapper.UserMapper;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User already exists with email: "
                    + requestDto.getEmail());
        }

        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Role with name " + RoleName.USER + " not found"));

        user.setRoles(Set.of(userRole));

        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new EntityNotFoundException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            Long userId = ((User) principal).getId();
            return userRepository.findById(userId)
                    .orElseThrow(()
                            -> new EntityNotFoundException("User not found by id: "
                            + userId));
        } else {
            throw new EntityNotFoundException("Authentication principal is not of User type");
        }
    }
}
