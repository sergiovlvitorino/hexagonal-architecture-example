package com.sergiovitorino.hexagonalarchitectureexample.application.validation;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
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
        var cleaned = Jsoup.clean(html, Safelist.none());
        return html.equals(Parser.unescapeEntities(cleaned, false));
    }

}
