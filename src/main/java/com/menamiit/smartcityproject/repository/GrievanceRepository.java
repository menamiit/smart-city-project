package com.menamiit.smartcityproject.repository;

import com.menamiit.smartcityproject.model.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {
 

    List<Grievance> findByCitizenUsernameOrderBySubmittedAtDesc(String citizenUsername);
    List<Grievance> findAllByOrderBySubmittedAtDesc();
    List<Grievance> findByStatusOrderBySubmittedAtDesc(String status);
    List<Grievance> findByAssignedOfficerOrderBySubmittedAtDesc(String officer);
}
