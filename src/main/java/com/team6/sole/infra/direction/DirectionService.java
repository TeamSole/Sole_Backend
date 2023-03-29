package com.team6.sole.infra.direction;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.home.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectionService {
    private final CourseRepository courseRepository;
    private static final int MAX_SEARCH_COUNT = 7; // 최대 검색 갯수
    private static final double RADIUS_KM = 20.0; // 반경 10 km

    // 추천 코스(현재 위치 기준으로 가장 가깝고, 스크랩수가 가장 많으며, 7개)
    @Transactional(readOnly = true)
    public List<Course> buildCourses(Gps gps) {
        return courseRepository.findAll().stream()
                .filter(course ->
                        calculateDistance(gps.getLatitude(),
                        gps.getLongitude(),
                                course.getPlaces().get(0).getGps().getLatitude(),
                                course.getPlaces().get(0).getGps().getLongitude()) <= RADIUS_KM)
                .sorted(Comparator.comparing(Course::getScrapCount).reversed())
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    // Haversine formula
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371; //Kilometers
        return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }
}
