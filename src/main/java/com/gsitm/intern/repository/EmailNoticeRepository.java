package com.gsitm.intern.repository;

import com.gsitm.intern.entity.EmailNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailNoticeRepository extends JpaRepository<EmailNotice, Long> {

    @Query(value = "select month('reg_time') as 'month', count(*) from email_notice group by 'month'", nativeQuery=true)
    List<EmailNotice> findEmailNoticeByRegTimeIsBefore(LocalDateTime regTime);


}
