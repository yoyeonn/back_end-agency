package com.travel.controller;

import com.travel.dto.AdminProfileResponse;
import com.travel.dto.UpdateAdminProfileRequest;
import com.travel.entity.User;
import com.travel.security.JwtUtil;
import com.travel.service.CloudinaryService;
import com.travel.service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/admin/profile")
public class AdminProfileController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CloudinaryService cloudinaryService;

    public AdminProfileController(UserService userService, JwtUtil jwtUtil, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cloudinaryService = cloudinaryService;
    }

    private User getUserFromAuth(String auth) {
        String token = auth.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(token);
        return userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @GetMapping
    public ResponseEntity<AdminProfileResponse> getMyProfile(
            @RequestHeader("Authorization") String auth
    ) {
        User user = getUserFromAuth(auth);

        return ResponseEntity.ok(new AdminProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCountry(),
                user.getCityState(),
                user.getPostalCode(),
                user.getImageUrl()
        ));
    }

    @PutMapping
    public ResponseEntity<AdminProfileResponse> updateMyProfile(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody UpdateAdminProfileRequest req
    ) {
        User user = getUserFromAuth(auth);

        user.setName(req.getName());
        user.setCountry(req.getCountry());
        user.setCityState(req.getCityState());
        user.setPostalCode(req.getPostalCode());

        userService.save(user);

        return ResponseEntity.ok(new AdminProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCountry(),
                user.getCityState(),
                user.getPostalCode(),
                user.getImageUrl()
        ));
    }

    // ✅ Upload image (multipart/form-data)
    @PostMapping("/image")
public ResponseEntity<AdminProfileResponse> uploadProfileImage(
        @RequestHeader("Authorization") String auth,
        @RequestParam("file") MultipartFile file
) {
    String token = auth.replace("Bearer ", "");
    String email = jwtUtil.getEmailFromToken(token);

    User user = userService.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    String imageUrl = cloudinaryService.uploadImage(file);
    user.setImageUrl(imageUrl);
    userService.save(user);

    return ResponseEntity.ok(new AdminProfileResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getCountry(),
            user.getCityState(),
            user.getPostalCode(),
            user.getImageUrl()
    ));
}

}
