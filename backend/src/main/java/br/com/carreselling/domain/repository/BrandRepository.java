package br.com.carreselling.domain.repository;

import br.com.carreselling.domain.model.Brand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository {

    Brand saveBrand(Brand brand);

    List<Brand> findBrands();

    Optional<Brand> findBrandById(UUID id);

    Optional<Brand> findBrandByName(String name);
}
