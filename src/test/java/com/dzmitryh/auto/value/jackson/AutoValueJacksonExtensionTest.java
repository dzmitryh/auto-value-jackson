package com.dzmitryh.auto.value.jackson;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.Collections;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AutoValueJacksonExtensionTest {

    @Test
    public void addJsonPropertyToGeneratedMethod() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.dzmitryh.auto.value.jackson.AutoJackson;\n"
                + "@AutoJackson @AutoValue public abstract class Test {\n"
                + "  public abstract String a();\n"
                + "}\n");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "import com.fasterxml.jackson.annotation.JsonCreator;\n"
                + "import com.fasterxml.jackson.annotation.JsonProperty;\n"
                + "import java.lang.Override;\n"
                + "import java.lang.String;\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "  @JsonCreator\n"
                + "  AutoValue_Test(@JsonProperty(\"a\") String a) {\n"
                + "    super(a);\n"
                + "  }\n"
                + "  @JsonProperty\n"
                + "  @Override\n"
                + "  public String a() {\n"
                + "    return super.a();\n"
                + "  }\n"
                + "}\n");

        assertAbout(javaSources())
                .that(Collections.singletonList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

}