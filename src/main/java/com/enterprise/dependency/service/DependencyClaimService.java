package com.enterprise.dependency.service;

import com.enterprise.dependency.model.DependencyClaim;
import com.enterprise.dependency.repository.DependencyClaimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing DependencyClaims.
 */
@Service
public class DependencyClaimService {
    private static final Logger logger = LoggerFactory.getLogger(DependencyClaimService.class);
    private final DependencyClaimRepository repository;

    public DependencyClaimService(DependencyClaimRepository repository) {
        this.repository = repository;
    }

    /**
     * Save a DependencyClaim.
     * @param claim the claim to save
     * @return saved claim
     */
    @Transactional
    public DependencyClaim save(DependencyClaim claim) {
        logger.info("Saving DependencyClaim: {} -> {}", claim.getSourceApplication(), claim.getTargetApplication());
        // TODO: Add validation logic
        return repository.save(claim);
    }

    /**
     * Get all DependencyClaims.
     * @return list of claims
     */
    public List<DependencyClaim> findAll() {
        logger.info("Fetching all DependencyClaims");
        return repository.findAll();
    }

    // TODO: Add batch save, delete, and custom query methods
}
