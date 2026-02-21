package br.com.carreselling.usecase.color.list.mapping;

import br.com.carreselling.application.service.model.ColorSummary;
import br.com.carreselling.usecase.color.list.contract.ColorItem;

public class ColorListMapper {

    private ColorListMapper() {
    }

    public static ColorItem toItem(ColorSummary color) {
        return new ColorItem(color.id(), color.name());
    }
}
