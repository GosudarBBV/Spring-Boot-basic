package book.shop.spring.boot.intro.service;

import book.shop.spring.boot.intro.dto.UserRegistrationRequestDto;
import book.shop.spring.boot.intro.dto.UserResponseDto;
import book.shop.spring.boot.intro.exception.EntityNotFoundException;
import book.shop.spring.boot.intro.exception.RegistrationException;
import book.shop.spring.boot.intro.mapper.UserMapper;
import book.shop.spring.boot.intro.model.Role;
import book.shop.spring.boot.intro.model.RoleName;
import book.shop.spring.boot.intro.model.ShoppingCart;
import book.shop.spring.boot.intro.model.User;
import book.shop.spring.boot.intro.repository.RoleRepository;
import book.shop.spring.boot.intro.repository.ShoppingCartRepository;
import book.shop.spring.boot.intro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private ShoppingCartRepository shoppingCartRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        roleRepository = mock(RoleRepository.class);
        shoppingCartRepository = mock(ShoppingCartRepository.class);
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder,
                roleRepository, shoppingCartRepository);
    }

    @Test
    @DisplayName("Register user successfully")
    void register_Success() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setShippingAddress("Kyiv");

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(requestDto.getPassword());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setShippingAddress(requestDto.getShippingAddress());

        Role role = new Role();
        role.setName(RoleName.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(user.getEmail());
        savedUser.setFirstName(user.getFirstName());
        savedUser.setLastName(user.getLastName());
        savedUser.setShippingAddress(user.getShippingAddress());

        UserResponseDto expectedResponse = new UserResponseDto(
                1L,
                "test@example.com",
                "John",
                "Doe",
                "Kyiv"
        );

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(expectedResponse);

        UserResponseDto actualResponse = userService.register(requestDto);

        assertEquals(expectedResponse, actualResponse);
        verify(shoppingCartRepository).save(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("Register user fails if email exists")
    void register_EmailExists_ThrowsRegistrationException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("existing@example.com");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        assertThrows(RegistrationException.class, () -> userService.register(requestDto));
    }

    @Test
    @DisplayName("Register fails if role not found")
    void register_RoleNotFound_ThrowsException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("123");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("123");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded123");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.register(requestDto));
    }

    @Test
    @DisplayName("Delete user by ID calls repository")
    void deleteById_CallsRepository() {
        userService.deleteById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Get authenticated user ID returns correct ID")
    void getAuthenticatedUserId_ReturnsUserId() {
        User user = new User();
        user.setId(42L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            Long result = userService.getAuthenticatedUserId();
            assertEquals(42L, result);
        }
    }

    @Test
    @DisplayName("Create shopping cart for user if not exists")
    void createShoppingCart_Success() {
        User user = new User();
        user.setId(1L);

        when(shoppingCartRepository.existsByUserId(1L)).thenReturn(false);

        userService.createShoppingCart(user);

        verify(shoppingCartRepository).save(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("Create shopping cart throws if cart already exists")
    void createShoppingCart_AlreadyExists_ThrowsException() {
        User user = new User();
        user.setId(1L);

        when(shoppingCartRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.createShoppingCart(user));
    }
}
