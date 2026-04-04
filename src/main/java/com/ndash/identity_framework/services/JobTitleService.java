package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.JobTitleResponse;

import java.util.List;

public interface JobTitleService {

    List<JobTitleResponse> getAllJobTitles();

    JobTitleResponse getById(Long id);
}
