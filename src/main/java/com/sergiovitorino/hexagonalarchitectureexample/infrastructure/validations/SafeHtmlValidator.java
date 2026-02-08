package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.validations;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {

    @Override
    public void initialize(SafeHtml constraintAnnotation) {
    }

    @Override
    public boolean isValid(String html, ConstraintValidatorContext constraintValidatorContext) {
        if (html == null) return true;
        return Jsoup.isValid(html, Safelist.none())
                && Jsoup.parse(html).text().equals(html);
    }

}
