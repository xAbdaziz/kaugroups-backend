package dev.abdaziz.kaugroups.dto.request;

import dev.abdaziz.kaugroups.exception.BusinessRuleViolationException;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class UpdateGroupRequest {
    @Size(min = 1, max = 3)
    private String section;

    @Size(min = 48, max = 48)
    @URL
    @Pattern(
            regexp = "^https://chat\\.whatsapp\\.com/.*$",
            message = "Invalid group link"
    )
    private String link;

    private Boolean generalGroup;

    private Boolean generalGroupMaleAndFemale;

    public void setLink(String link) {
        if (link != null) {
            link = link.trim();
            int queryIndex = link.indexOf('?');
            if (queryIndex != -1) {
                link = link.substring(0, queryIndex);
            }
        }
        this.link = link;
    }

    public void validate(String currentSection, Boolean currentGeneralGroup, Boolean currentGeneralGroupMaleAndFemale) {
        Boolean effectiveGeneralGroup = generalGroup != null ? generalGroup : currentGeneralGroup;
        Boolean effectiveGeneralGroupMaleAndFemale = generalGroupMaleAndFemale != null ? generalGroupMaleAndFemale : currentGeneralGroupMaleAndFemale;
        String effectiveSection = section != null ? section : currentSection;

        if (effectiveGeneralGroupMaleAndFemale && effectiveGeneralGroup) {
            throw new BusinessRuleViolationException(
                "Cannot have both generalGroup and generalGroupMaleAndFemale set to true"
            );
        }

        if (!effectiveGeneralGroup && !effectiveGeneralGroupMaleAndFemale) {
            if (effectiveSection == null || effectiveSection.isBlank()) {
                throw new BusinessRuleViolationException(
                    "Section is required when the group is not general"
                );
            }
        }
    }

    public boolean hasUpdates() {
        return section != null || link != null || generalGroup != null || generalGroupMaleAndFemale != null;
    }
}


