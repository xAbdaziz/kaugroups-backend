package dev.abdaziz.kaugroups.controller;

import dev.abdaziz.kaugroups.dto.request.AddGroupRequest;
import dev.abdaziz.kaugroups.dto.response.GroupResponse;
import dev.abdaziz.kaugroups.model.Group;
import dev.abdaziz.kaugroups.model.User;
import dev.abdaziz.kaugroups.service.GroupService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/")
    public ResponseEntity<GroupResponse> addGroup(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody AddGroupRequest request
    ) {
        Group group = groupService.addGroup(user, request);
        GroupResponse response = GroupResponse.from(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
