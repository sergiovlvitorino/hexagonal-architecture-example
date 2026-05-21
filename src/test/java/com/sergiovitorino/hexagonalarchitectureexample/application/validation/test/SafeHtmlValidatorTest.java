package com.sergiovitorino.hexagonalarchitectureexample.application.validation.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.validation.SafeHtmlValidator;
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

    @Test
    public void testWhitespaceOnlyIsInvalid() {
        // Jsoup.clean() strip espaços — resultado "" != "   ", portanto o validador rejeita
        assertFalse(validator.isValid("   ", null));
    }

    @Test
    public void testUnicodeEscapedScriptTagIsInvalid() {
        // "<script>" em entidades HTML — o unescapeEntities irá expandir para tag real
        assertFalse(validator.isValid("&lt;script&gt;alert(1)&lt;/script&gt;", null));
    }

    @Test
    public void testNestedEncodedEntitiesAreInvalid() {
        // Entidades HTML aninhadas/misturadas com texto
        assertFalse(validator.isValid("Hello &lt;b&gt;World&lt;/b&gt;", null));
    }

}
