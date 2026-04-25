package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Setting;
import com.ndash.identity_framework.repositories.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository repository;

    public Map<String, Object> getAllSettings() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(
                        Setting::getKey,
                        this::parseValue
                ));
    }

    public void saveAll(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            Setting setting = repository.findByKey(key)
                    .orElseGet(() -> {
                        Setting s = new Setting();
                        s.setKey(key);
                        return s;
                    });

            setting.setValue(value);
            setting.setUpdatedAt(LocalDateTime.now());

            repository.save(setting);
        });
    }

    private Object parseValue(Setting setting) {
        if ("BOOLEAN".equalsIgnoreCase(setting.getDataType())) {
            return Boolean.parseBoolean(setting.getValue());
        }
        return setting.getValue();
    }

    public boolean getBoolean(String key) {
        return repository.findByKey(key)
                .map(s -> Boolean.parseBoolean(s.getValue()))
                .orElse(false);
    }
}
