package br.com.carreselling.application.service;

import br.com.carreselling.application.service.model.PartnerSummary;
import java.util.List;
import java.util.UUID;

public interface IPartnerService {

    UUID createPartner(String name, String city);

    List<PartnerSummary> listPartners();
}
