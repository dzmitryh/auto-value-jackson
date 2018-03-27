package com.dzmitryh.auto.value.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import java.util.List;
import java.util.Map;

/**
 * Remove it once pr will be merged
 * Copy & pasted from https://github.com/gabrielittner/auto-value-extension-util
 */
final class AutoValueUtil {
    private AutoValueUtil() {
    }

    static TypeVariableName[] getTypeVariables(TypeElement autoValueClass) {
        List<? extends TypeParameterElement> parameters = autoValueClass.getTypeParameters();
        TypeVariableName[] typeVariables = new TypeVariableName[parameters.size()];
        for (int i = 0, length = typeVariables.length; i < length; i++) {
            typeVariables[i] = TypeVariableName.get(parameters.get(i));
        }
        return typeVariables;
    }

    static TypeName getSuperClass(
            String packageName, String classToExtend, TypeName[] typeVariables) {
        ClassName superClassWithoutParameters = ClassName.get(packageName, classToExtend);
        if (typeVariables.length > 0) {
            return ParameterizedTypeName.get(superClassWithoutParameters, typeVariables);
        } else {
            return superClassWithoutParameters;
        }
    }

    static MethodSpec newConstructor(Map<String, ExecutableElement> properties) {
        List<ParameterSpec> params = Lists.newArrayList();
        for (Map.Entry<String, ExecutableElement> entry : properties.entrySet()) {
            TypeName typeName = TypeName.get(entry.getValue().getReturnType());
            params.add(
                    ParameterSpec.builder(typeName, entry.getKey())
                            .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                                    .addMember("value", "$S", entry.getKey()).build()).build());
        }

        CodeBlock code = newConstructorCall(CodeBlock.of("super"), properties.keySet().toArray());

        return MethodSpec.constructorBuilder()
                .addParameters(params)
                .addCode(code)
                .addAnnotation(JsonCreator.class)
                .build();
    }

    private static CodeBlock newConstructorCall(CodeBlock constructorName, Object[] properties) {
        StringBuilder params = new StringBuilder("(");
        for (int i = properties.length; i > 0; i--) {
            params.append("$N");
            if (i > 1) params.append(", ");
        }
        params.append(")");
        return CodeBlock.builder()
                .add(constructorName)
                .addStatement(params.toString(), properties)
                .build();
    }
}
