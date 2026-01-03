package dev.abdaziz.kaugroups.dto.response;

import dev.abdaziz.kaugroups.model.Group;

import java.util.UUID;

public record GroupResponse(
    UUID id,
    String link
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(group.getId(), group.getLink());
    }
}


