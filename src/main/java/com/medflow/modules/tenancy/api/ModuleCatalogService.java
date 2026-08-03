package com.medflow.modules.tenancy.api;

import com.medflow.modules.tenancy.api.response.ModuleEntitlementResponse;
import java.util.List;

/**
 * Read model behind the UI's navigation: which product modules exist and which of them a
 * hospital is licensed for.
 */
public interface ModuleCatalogService {

  List<ModuleEntitlementResponse> forHospital(Long hospitalId);

  boolean isEntitled(Long hospitalId, String moduleCode);
}
