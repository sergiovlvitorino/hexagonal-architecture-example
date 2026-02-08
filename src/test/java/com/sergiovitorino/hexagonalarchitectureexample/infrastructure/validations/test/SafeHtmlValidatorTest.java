package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.validations.test;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.validations.SafeHtmlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SafeHtmlValidatorTest {

    private SafeHtmlValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new SafeHtmlValidator();
        validator.initialize(null);
    }

    @Test
    public void testNullInputIsValid() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    public void testPlainTextIsValid() {
        assertTrue(validator.isValid("Sergio Vitorino", null));
    }

    @Test
    public void testScriptTagIsInvalid() {
        assertFalse(validator.isValid("<script>alert(1)</script>", null));
    }

    @Test
    public void testImgTagIsInvalid() {
        assertFalse(validator.isValid("<img src=x onerror=alert(1)>", null));
    }

    @Test
    public void testHtmlTagIsInvalid() {
        assertFalse(validator.isValid("<html>test</html>", null));
    }

    @Test
    public void testEncodedHtmlEntitiesAreInvalid() {
        assertFalse(validator.isValid("Tom &amp; Jerry", null));
    }

    @Test
    public void testPlainAmpersandIsValid() {
        assertTrue(validator.isValid("Tom & Jerry", null));
    }

    @Test
    public void testEmptyStringIsValid() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    public void testDivTagIsInvalid() {
        assertFalse(validator.isValid("<div>content</div>", null));
    }

}
