package dev.abdaziz.kaugroups.repository;

import dev.abdaziz.kaugroups.model.Course;
import dev.abdaziz.kaugroups.model.Gender;
import dev.abdaziz.kaugroups.model.Group;
import dev.abdaziz.kaugroups.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByUser(User user);
    List<Group> findByCourseAndGender(Course course, Gender gender);
    boolean existsByLink(String link);
}


