package com.gsitm.intern.repository;

import com.gsitm.intern.entity.SmsNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsNoticeRepository extends JpaRepository<SmsNotice, Long> {
}
