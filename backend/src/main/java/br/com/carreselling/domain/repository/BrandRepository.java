package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Brand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository {

    Brand saveBrand(int companyId, Brand brand);

    List<Brand> findBrands(int companyId);

    Optional<Brand> findBrandById(int companyId, UUID id);

    Optional<Brand> findBrandByName(int companyId, String name);
}
