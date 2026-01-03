package dev.abdaziz.kaugroups.service;

import dev.abdaziz.kaugroups.dto.request.AddGroupRequest;
import dev.abdaziz.kaugroups.dto.request.GetGroupsRequest;
import dev.abdaziz.kaugroups.dto.request.UpdateGroupRequest;
import dev.abdaziz.kaugroups.exception.BusinessRuleViolationException;
import dev.abdaziz.kaugroups.exception.ForbiddenException;
import dev.abdaziz.kaugroups.exception.ResourceNotFoundException;
import dev.abdaziz.kaugroups.model.Course;
import dev.abdaziz.kaugroups.model.Gender;
import dev.abdaziz.kaugroups.model.Group;
import dev.abdaziz.kaugroups.model.User;
import dev.abdaziz.kaugroups.repository.CourseRepository;
import dev.abdaziz.kaugroups.repository.GroupRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

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

        if (groupRepository.existsByLink(request.getGroupLink())) {
            throw new BusinessRuleViolationException(
                "A group with this link already exists"
            );
        }

        Gender groupGender = request.getGeneralGroupMaleAndFemale() ? Gender.UNKNOWN : user.getGender();
        boolean isGeneral = request.getGeneralGroup() || request.getGeneralGroupMaleAndFemale();
        String section = isGeneral ? null : request.getSection();

        checkDuplicateGroup(course, section, groupGender, request.getGeneralGroup(), request.getGeneralGroupMaleAndFemale(), null);

        Group group = Group.builder()
            .course(course)
            .user(user)
            .link(request.getGroupLink())
            .gender(groupGender)
            .section(section)
            .generalGroup(request.getGeneralGroup())
            .generalGroupMaleAndFemale(request.getGeneralGroupMaleAndFemale())
            .build();

        return groupRepository.save(group);
    }

    public List<Group> getGroups(User user, GetGroupsRequest request) {
        List<Group> groups = groupRepository.findByCourseAndGenderOrGeneralForBoth(
            request.getCourseCode().toUpperCase(), 
            request.getCourseNumber(), 
            user.getGender()
        );
        
        if (groups.isEmpty()) {
            throw new ResourceNotFoundException(
                "No groups found for course: " + request.getCourseCode() + request.getCourseNumber()
            );
        }
        
        return groups;
    }

    @Transactional
    public void deleteGroup(User user, UUID id) {
        Group group = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No group found with id: " + id));
        
        if (!group.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                "You are not authorized to delete this group. Only the group creator can delete it."
            );
        }
        
        groupRepository.delete(group);
    }

    @Transactional
    public Group updateGroup(User user, UUID id, UpdateGroupRequest request) {
        Group group = groupRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No group found with id: " + id));

        if (!group.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(
                "You are not authorized to update this group. Only the group creator can update it."
            );
        }

        if (!request.hasUpdates()) {
            throw new BusinessRuleViolationException("No fields to update");
        }

        request.validate(group.getSection(), group.getGeneralGroup(), group.getGeneralGroupMaleAndFemale());

        if (request.getLink() != null && !request.getLink().equals(group.getLink())) {
            if (groupRepository.existsByLink(request.getLink())) {
                throw new BusinessRuleViolationException("A group with this link already exists");
            }
            group.setLink(request.getLink());
        }

        if (request.getGeneralGroupMaleAndFemale() != null) {
            group.setGeneralGroupMaleAndFemale(request.getGeneralGroupMaleAndFemale());
            if (request.getGeneralGroupMaleAndFemale()) {
                group.setGender(Gender.UNKNOWN);
            } else {
                group.setGender(user.getGender());
            }
        }

        if (request.getGeneralGroup() != null) {
            group.setGeneralGroup(request.getGeneralGroup());
        }

        // Handle section based on general flags
        Boolean effectiveGeneralGroup = request.getGeneralGroup() != null ? request.getGeneralGroup() : group.getGeneralGroup();
        Boolean effectiveGeneralGroupMaleAndFemale = request.getGeneralGroupMaleAndFemale() != null ? request.getGeneralGroupMaleAndFemale() : group.getGeneralGroupMaleAndFemale();
        String effectiveSection;

        if (effectiveGeneralGroup || effectiveGeneralGroupMaleAndFemale) {
            effectiveSection = null;
        } else {
            effectiveSection = request.getSection() != null ? request.getSection() : group.getSection();
        }

        checkDuplicateGroup(group.getCourse(), effectiveSection, group.getGender(), effectiveGeneralGroup, effectiveGeneralGroupMaleAndFemale, group.getId());

        group.setSection(effectiveSection);

        return groupRepository.save(group);
    }

    private void checkDuplicateGroup(Course course, String section, Gender gender, Boolean generalGroup, Boolean generalGroupMaleAndFemale, UUID excludeId) {
        if (generalGroupMaleAndFemale) {
            if (groupRepository.existsDuplicateGeneralForBoth(course, excludeId)) {
                throw new BusinessRuleViolationException("A general group for both genders already exists for this course");
            }
        } else if (generalGroup) {
            if (groupRepository.existsDuplicateGeneralPerGender(course, gender, excludeId)) {
                throw new BusinessRuleViolationException("A general group for your gender already exists for this course");
            }
        } else if (section != null) {
            if (groupRepository.existsDuplicateSection(course, section, gender, excludeId)) {
                throw new BusinessRuleViolationException("A group for this section already exists");
            }
        }
    }
}
