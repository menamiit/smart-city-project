package com.menamiit.smartcityproject.repository;

import com.menamiit.smartcityproject.model.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    List<Grievance> findByCitizenUsernameOrderBySubmittedAtDesc(String citizenUsername);
    List<Grievance> findAllByOrderBySubmittedAtDesc();
    List<Grievance> findByStatusOrderBySubmittedAtDesc(String status);
    List<Grievance> findByAssignedOfficerOrderBySubmittedAtDesc(String officer);

    @Query("""
        select upper(g.category), count(g)
        from Grievance g
        group by upper(g.category)
    """)
    List<Object[]> countByCategory();

    @Query("""
        select upper(trim(g.location)), count(g)
        from Grievance g
        where g.location is not null and trim(g.location) <> ''
        group by upper(trim(g.location))
    """)
    List<Object[]> countByZone();

    @Query("""
        select g
        from Grievance g
        where g.status in ('RESOLVED', 'CLOSED')
          and g.submittedAt is not null
          and g.updatedAt is not null
    """)
    List<Grievance> findResolvedForSla();

    @Query(value = """
        select
            upper(trim(location)) as zone,
            count(*) as total,
            sum(case when coalesce(reopen_count, 0) > 0 then 1 else 0 end) as reopened
        from grievances
        where location is not null and trim(location) <> ''
        group by upper(trim(location))
        having count(*) >= :minComplaints
        order by reopened desc, total desc
    """, nativeQuery = true)
    List<Object[]> findRedZones(@Param("minComplaints") int minComplaints);
}
