package com.examplatform.modules.evaluation.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.entity.EvaluationProfile;
import com.examplatform.modules.evaluation.mapper.EvaluationProfileMapper;
import com.examplatform.modules.evaluation.repository.EvaluationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationProfileService {

    private final EvaluationProfileRepository profileRepository;
    private final EvaluationProfileMapper profileMapper;

    @Transactional
    public ProfileResponse create(ProfileCreateRequest req) {
        return profileMapper.toResponse(profileRepository.save(profileMapper.toEntity(req)));
    }

    @Transactional
    public ProfileResponse update(String id, ProfileUpdateRequest req) {
        EvaluationProfile profile = getOrThrow(id);
        profileMapper.applyUpdate(profile, req);
        return profileMapper.toResponse(profileRepository.save(profile));
    }

    public ProfileResponse getById(String id) {
        return profileMapper.toResponse(getOrThrow(id));
    }

    public List<ProfileResponse> listAll() {
        return profileRepository.findAll().stream().map(profileMapper::toResponse).toList();
    }

    @Transactional
    public void delete(String id) {
        profileRepository.delete(getOrThrow(id));
    }

    private EvaluationProfile getOrThrow(String id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationProfile", id));
    }
}
