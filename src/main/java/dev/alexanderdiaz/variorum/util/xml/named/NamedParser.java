package dev.alexanderdiaz.variorum.util.xml.named;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that parse specific XML elements. The value defines the XML tag name
 * for the parser.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NamedParser {
  String[] value();
}
