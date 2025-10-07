package devacc11011.spring.controller;

import devacc11011.spring.dto.UserResponse;
import devacc11011.spring.dto.UserTokenUsageResponse;
import devacc11011.spring.service.UserService;
import devacc11011.spring.service.UserTokenUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserTokenUsageService userTokenUsageService;

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userService.findAllUsers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/token-usage")
    public List<UserTokenUsageResponse> getAllTokenUsage() {
        return userTokenUsageService.findAllTokenUsages().stream()
                .map(UserTokenUsageResponse::from)
                .collect(Collectors.toList());
    }
}
