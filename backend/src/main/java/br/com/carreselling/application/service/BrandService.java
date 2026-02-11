package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.BrandSummary;
import br.com.carreselling.domain.repository.BrandRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BrandService implements IBrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public List<BrandSummary> listBrands() {
        return brandRepository.findBrands()
            .stream()
            .map(brand -> new BrandSummary(brand.getId(), brand.getName()))
            .toList();
    }
}
