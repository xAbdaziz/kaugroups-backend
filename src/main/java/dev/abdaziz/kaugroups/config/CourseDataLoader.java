package dev.abdaziz.kaugroups.config;

import dev.abdaziz.kaugroups.model.Course;
import dev.abdaziz.kaugroups.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseDataLoader implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final RestTemplate restTemplate;

    @Value("${app.data.load-courses:false}")
    private boolean loadCourses;

    @Value("${app.data.load-courses.url}")
    private String coursesUrl;

    @Override
    public void run(String... args) throws Exception {
        if (!loadCourses) {
            log.debug("Course loading disabled. Set app.data.load-courses=true to enable.");
            return;
        }

        try {
            CoursesApiResponse response = restTemplate.getForObject(coursesUrl, CoursesApiResponse.class);

            if (response == null || response.getData() == null) {
                log.warn("No course data received from API.");
                return;
            }

            Set<String> existingKeys = courseRepository.findAll().stream()
                    .map(c -> c.getCode() + "-" + c.getNumber())
                    .collect(Collectors.toSet());

            List<Course> newCourses = response.getData().stream()
                    .filter(courseData -> !existingKeys.contains(courseData.getSubject() + "-" + courseData.getCode()))
                    .map(courseData -> Course.builder()
                            .code(courseData.getSubject())
                            .number(Integer.parseInt(courseData.getCode()))
                            .name(courseData.getName())
                            .build())
                    .toList();

            if (!newCourses.isEmpty()) {
                courseRepository.saveAll(newCourses);
                log.info("Loaded {} new courses.", newCourses.size());
            }

        } catch (Exception e) {
            log.error("Failed to load courses: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Data
    private static class CoursesApiResponse {
        private String status;
        private String termName;
        private List<CourseData> data;
    }

    @Data
    private static class CourseData {
        private String subject;
        private String code;
        private String name;
    }
}

