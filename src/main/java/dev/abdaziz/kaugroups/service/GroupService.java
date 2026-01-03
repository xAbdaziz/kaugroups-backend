package dev.abdaziz.kaugroups.service;

import dev.abdaziz.kaugroups.dto.request.AddGroupRequest;
import dev.abdaziz.kaugroups.exception.BusinessRuleViolationException;
import dev.abdaziz.kaugroups.exception.ResourceNotFoundException;
import dev.abdaziz.kaugroups.model.Course;
import dev.abdaziz.kaugroups.model.Gender;
import dev.abdaziz.kaugroups.model.Group;
import dev.abdaziz.kaugroups.model.User;
import dev.abdaziz.kaugroups.repository.CourseRepository;
import dev.abdaziz.kaugroups.repository.GroupRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final CourseRepository courseRepository;

    public Group addGroup(User user, AddGroupRequest request) {
        
        request.validate();

        Course course = courseRepository.findByCodeAndNumber(
            request.getCourseCode().toUpperCase(), 
            request.getCourseNumber()
        ).orElseThrow(() -> new ResourceNotFoundException(
            "Course not found: " + request.getCourseCode() + request.getCourseNumber()
        ));

        Gender groupGender = determineGroupGender(user, request);
        String groupSection = determineGroupSection(request);

        if (groupRepository.existsByLink(request.getGroupLink())) {
            throw new BusinessRuleViolationException(
                "A group with this link already exists"
            );
        }

        Group group = Group.builder()
            .course(course)
            .user(user)
            .link(request.getGroupLink())
            .gender(groupGender)
            .section(groupSection)
            .generalGroup(request.getGeneralGroup())
            .generalGroupMaleAndFemale(request.getGeneralGroupMaleAndFemale())
            .build();

        return groupRepository.save(group);
    }

    private Gender determineGroupGender(User user, AddGroupRequest request) {
        // If it's a general group for both genders (entire course)
        if (request.getGeneralGroupMaleAndFemale()) {
            return Gender.UNKNOWN; // Represents "both genders"
        }
        
        // Otherwise (general for one gender or section-specific), use the user's gender
        return user.getGender();
    }

    private String determineGroupSection(AddGroupRequest request) {
        // If either general flag is true, section should be null
        if (request.getGeneralGroup() || request.getGeneralGroupMaleAndFemale()) {
            return null;
        }
        
        // Otherwise, use the provided section
        return request.getSection();
    }
}
