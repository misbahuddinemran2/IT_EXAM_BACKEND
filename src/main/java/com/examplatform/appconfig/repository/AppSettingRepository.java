package com.examplatform.appconfig.repository;

import com.examplatform.appconfig.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, String> {

    Optional<AppSetting> findBySettingKey(String settingKey);

    List<AppSetting> findByCategory(String category);

    List<AppSetting> findByIsActiveTrueOrderByCategory();

    @Query("SELECT s FROM AppSetting s WHERE s.isActive = true ORDER BY s.category, s.settingKey")
    List<AppSetting> findAllActiveGrouped();
}