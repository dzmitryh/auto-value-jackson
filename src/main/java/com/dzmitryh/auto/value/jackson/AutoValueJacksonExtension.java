package com.dzmitryh.auto.value.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.List;

import static com.dzmitryh.auto.value.jackson.AutoValueUtil.getSuperClass;
import static com.dzmitryh.auto.value.jackson.AutoValueUtil.getTypeVariables;
import static com.dzmitryh.auto.value.jackson.AutoValueUtil.newConstructor;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;

@AutoService(AutoValueExtension.class)
public class AutoValueJacksonExtension extends AutoValueExtension {
    @Override
    public boolean applicable(Context context) {
        return context.autoValueClass().getAnnotation(AutoJackson.class) != null;
    }

    @Override
    public String generateClass(Context context, String className,
                                String classToExtend, boolean isFinal) {
        TypeVariableName[] typeVariables = getTypeVariables(context.autoValueClass());
        TypeSpec subclass = TypeSpec.classBuilder(className)
                .addModifiers(isFinal ? FINAL : ABSTRACT)
                .addTypeVariables(Arrays.asList(typeVariables))
                .superclass(getSuperClass(context.packageName(), classToExtend, typeVariables))
                .addMethod(newConstructor(context.properties()))
                .addMethods(generateGetters(context))
                .build();
        return JavaFile.builder(context.packageName(), subclass).build().toString();
    }

    private static List<MethodSpec> generateGetters(Context context) {
        return context.abstractMethods().stream()
                .map(AutoValueJacksonExtension::generateGetter)
                .collect(toList());
    }

    private static MethodSpec generateGetter(ExecutableElement executableElement) {
        List<AnnotationSpec> annotations = executableElement.getAnnotationMirrors().stream()
                .map(AnnotationSpec::get)
                .collect(toList());

        List<Modifier> modifiers = executableElement.getModifiers().stream()
                .filter(m -> m == Modifier.PUBLIC || m == Modifier.PROTECTED)
                .collect(toList());

        return MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                .addAnnotation(JsonProperty.class)
                .addAnnotation(Override.class)
                .addAnnotations(annotations)
                .addModifiers(modifiers)
                .returns(ClassName.get(executableElement.getReturnType()))
                .addCode(String.format("return super.%s();\n", executableElement.getSimpleName()))
                .build();
    }
}
