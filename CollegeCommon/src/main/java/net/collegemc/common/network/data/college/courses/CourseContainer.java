package net.collegemc.common.network.data.college.courses;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class CourseContainer {

  private final Map<Course, Integer> attributeMap;

  public CourseContainer() {
    this.attributeMap = new HashMap<>();
  }

  public int getCourseLevel(Course course) {
    return this.attributeMap.getOrDefault(course, 0);
  }

  public void setCourseLevel(Course course, int level) {
    this.attributeMap.put(course, level);
  }

  public void applyToCourse(Course course, IntUnaryOperator operator) {
    this.setCourseLevel(course, operator.applyAsInt(this.getCourseLevel(course)));
  }

}
