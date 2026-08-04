package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.service.EvaluationDatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/research/datasets")
@RequiredArgsConstructor
public class AdminEvaluationDatasetController {

    private final EvaluationDatasetService datasetService;
    private final AdminUserRepository adminUserRepository;

    @PostMapping
    public DatasetResponse create(@RequestBody DatasetCreateRequest request, Authentication auth) {
        return datasetService.create(request, resolveAdminId(auth));
    }

    @PutMapping("/{id}")
    public DatasetResponse update(@PathVariable String id, @RequestBody DatasetUpdateRequest request) {
        return datasetService.update(id, request);
    }

    @GetMapping
    public List<DatasetSummaryResponse> getAll() {
        return datasetService.listAll();
    }

    @GetMapping("/{id}")
    public DatasetResponse getById(@PathVariable String id) {
        return datasetService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        datasetService.delete(id);
    }

    private String resolveAdminId(Authentication auth) {
        AdminUser adminUser = adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()));
        return adminUser.getId();
    }
}
