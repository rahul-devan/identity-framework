package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Blueprint;
import com.ndash.identity_framework.dto.BlueprintRequest;
import com.ndash.identity_framework.dto.BlueprintResponse;

import java.util.List;

public interface BluePrintService {

    List<BlueprintResponse> getAllBlueprints();
    BlueprintResponse getBlueprintById(Long id);
    BlueprintResponse createBlueprint(BlueprintRequest blueprint);
    BlueprintResponse updateBlueprint(Long id, BlueprintRequest blueprint);
    void deleteBlueprint(Long id);
}
