package com.newtonduarte.liquibase.demo2.repositories;

import com.newtonduarte.liquibase.demo2.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
