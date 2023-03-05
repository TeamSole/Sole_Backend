package com.team6.sole.domain.home.repository;

import com.team6.sole.domain.home.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
