package com.pfe.hotel.feignClient;

import com.pfe.hotel.DTO.ClientUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users", url = "${user.service.url}")
public interface UserServiceFeignClient {
    @GetMapping("/api/users/{userId}")
    ResponseEntity<ClientUserDTO> getUserById(@PathVariable("userId") String userId);
}