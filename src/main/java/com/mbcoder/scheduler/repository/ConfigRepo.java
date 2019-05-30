package com.mbcoder.scheduler.repository;

import com.mbcoder.scheduler.model.ConfigItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepo extends JpaRepository<ConfigItem, String> {
}
