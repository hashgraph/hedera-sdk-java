package com.hedera.hashgraph.sdk;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;

@SuppressWarnings({"AndroidJdkLibsChecker", "Java7ApiChecker"})
@SupportedAnnotationTypes("com.hedera.hashgraph.sdk.FunctionalExecutable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FunctionalExecutableProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var packageName = "com.hedera.hashgraph.sdk";

        var clientClazz = ClassName.get(packageName, "Client");
        var preCheckStatusException = ClassName.get(packageName, "PrecheckStatusException");

        for (var element : roundEnv.getElementsAnnotatedWith(FunctionalExecutable.class)) {
            var annotation = element.getAnnotation(FunctionalExecutable.class);

            // expecting {name}Async
            var methodAsyncName = element.getSimpleName().toString();
            var methodName = methodAsyncName.replace("Async", "");

            // create With{Name} as the final output type name
            var interfaceName = "With" + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);

            // declare output type (and if its generic)
            var isGeneric = annotation.type().isEmpty();
            var outputType = isGeneric ? TypeVariableName.get("O") : ClassName.get(packageName, annotation.type());

            var parameterType = annotation.onClient() ? (annotation.inputType().isEmpty() ? null : ClassName.get(packageName, annotation.inputType())) : clientClazz;
            var parameterName = annotation.onClient() ? annotation.inputType().toLowerCase() : "client";
            var clientVariableName = annotation.onClient() ? "this" : "client";

            // are there any additional (checked) exceptions being through
            var moreExceptions = new TypeName[annotation.exceptionTypes().length];
            for (var i = 0; i < moreExceptions.length; ++i) {
                moreExceptions[i] = ClassName.get(packageName, annotation.exceptionTypes()[i]);
            }

            // common types
            var throwableTy = ClassName.get(Throwable.class);

            // callbacks
            var outCallbackTy = ParameterizedTypeName.get(ClassName.get(Consumer.class), outputType);
            var errCallbackTy = ParameterizedTypeName.get(ClassName.get(Consumer.class), throwableTy);
            var biCallbackTy = ParameterizedTypeName.get(ClassName.get(BiConsumer.class), outputType, throwableTy);

            // Return type for {name}Async()
            var futureReturnTy = ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), outputType);

            var methodGetRequestTimeout = methodBuilder("getRequestTimeout")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(Duration.class)
                .build();

            // --------------------------------------------------------------------------------------------------------

            var methodAsyncBuilder = methodBuilder(methodAsyncName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(futureReturnTy);

            if (parameterType != null) {
                methodAsyncBuilder.addParameter(parameterType, parameterName);
            }

            // --------------------------------------------------------------------------------------------------------

            var methodAsyncBiConsumerBuilder = methodBuilder(methodAsyncName)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .returns(void.class);

            if (parameterType != null) {
                methodAsyncBiConsumerBuilder.addParameter(parameterType, parameterName);
            }

            methodAsyncBiConsumerBuilder.addParameter(biCallbackTy, "callback");

            if (parameterType != null) {
                methodAsyncBiConsumerBuilder.addStatement("$L ($L, $L.getRequestTimeout(), callback)", methodAsyncName, parameterName, clientVariableName);
            } else {
                methodAsyncBiConsumerBuilder.addStatement("$L ($L.getRequestTimeout(), callback)", methodAsyncName, clientVariableName);
            }

            // --------------------------------------------------------------------------------------------------------

            var methodAsyncBiConsumerWithTimeoutBuilder = methodBuilder(methodAsyncName)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .returns(void.class);

            if (parameterType != null) {
                methodAsyncBiConsumerWithTimeoutBuilder
                    .addParameter(parameterType, parameterName)
                    .addStatement("$L ($L)" +
                            ".orTimeout(timeout.toMillis(), $T.MILLISECONDS)" +
                            ".whenComplete(callback)",
                        methodAsyncName, parameterName, TimeUnit.class);
            } else {
                methodAsyncBiConsumerWithTimeoutBuilder
                    .addStatement("$L ()" +
                            ".orTimeout(timeout.toMillis(), $T.MILLISECONDS)" +
                            ".whenComplete(callback)",
                        methodAsyncName, TimeUnit.class);
            }

            methodAsyncBiConsumerWithTimeoutBuilder
                .addParameter(Duration.class, "timeout")
                .addParameter(biCallbackTy, "callback");

            // --------------------------------------------------------------------------------------------------------

            var methodAsyncConsumerBuilder = methodBuilder(methodAsyncName)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .returns(void.class);

            if (parameterType != null) {
                methodAsyncConsumerBuilder
                    .addParameter(parameterType, parameterName)
                    .addStatement("$L ($L, $L.getRequestTimeout(), onSuccess, onFailure)", methodAsyncName, parameterName, clientVariableName);
            } else {
                methodAsyncConsumerBuilder
                    .addStatement("$L ($L.getRequestTimeout(), onSuccess, onFailure)", methodAsyncName, clientVariableName);
            }

            methodAsyncConsumerBuilder
                .addParameter(outCallbackTy, "onSuccess")
                .addParameter(errCallbackTy, "onFailure");

            // --------------------------------------------------------------------------------------------------------

            var methodAsyncConsumerWithTimeoutBuilder = methodBuilder(methodAsyncName)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .returns(void.class);

            if (parameterType != null) {
                methodAsyncConsumerWithTimeoutBuilder.addParameter(parameterType, parameterName)
                    .addStatement("$L ($L)" +
                            ".orTimeout(timeout.toMillis(), $T.MILLISECONDS)" +
                            ".whenComplete((output, error) -> {" +
                            "if (error != null) { onFailure.accept(error); }" +
                            "else { onSuccess.accept(output); }" +
                            "})",
                        methodAsyncName, parameterName, TimeUnit.class);
            } else {
                methodAsyncConsumerWithTimeoutBuilder.addStatement("$L ()" +
                        ".orTimeout(timeout.toMillis(), $T.MILLISECONDS)" +
                        ".whenComplete((output, error) -> {" +
                        "if (error != null) { onFailure.accept(error); }" +
                        "else { onSuccess.accept(output); }" +
                        "})",
                    methodAsyncName, TimeUnit.class);
            }

            methodAsyncConsumerWithTimeoutBuilder
                .addParameter(Duration.class, "timeout")
                .addParameter(outCallbackTy, "onSuccess")
                .addParameter(errCallbackTy, "onFailure");

            // --------------------------------------------------------------------------------------------------------

            var methodSyncBuilder = methodBuilder(methodName)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addException(TimeoutException.class)
                .addException(preCheckStatusException)
                .addExceptions(Arrays.asList(moreExceptions))
                .returns(outputType);

            if (parameterType != null) {
                methodSyncBuilder
                    .addParameter(parameterType, parameterName)
                    .addStatement("return $L ($L, $L.getRequestTimeout())", methodName, parameterName, clientVariableName);
            } else {
                methodSyncBuilder.addStatement("return $L ($L.getRequestTimeout())", methodName, clientVariableName);
            }

            // --------------------------------------------------------------------------------------------------------

            var methodSyncWithTimeoutBuilder = methodBuilder(methodName)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addException(TimeoutException.class)
                .addException(preCheckStatusException)
                .addExceptions(Arrays.asList(moreExceptions))
                .returns(outputType);

            if (parameterType != null) {
                methodSyncWithTimeoutBuilder.addParameter(parameterType, parameterName);
            }

            methodSyncWithTimeoutBuilder
                .addParameter(Duration.class, "timeout")
                .beginControlFlow("try");

            if (parameterType != null) {
                methodSyncWithTimeoutBuilder
                    .addStatement("return $L ($L).get(timeout.toMillis(), $T.MILLISECONDS)", methodAsyncName, parameterName, TimeUnit.class);
            } else {
                methodSyncWithTimeoutBuilder
                    .addStatement("return $L ().get(timeout.toMillis(), $T.MILLISECONDS)", methodAsyncName, TimeUnit.class);
            }

            methodSyncWithTimeoutBuilder
                .nextControlFlow("catch ($T e)", InterruptedException.class)
                .addStatement("throw new RuntimeException(e)")
                .nextControlFlow("catch ($T e)", ExecutionException.class)
                .addStatement("$T cause = e.getCause()", Throwable.class)
                // if there is no cause; re-throw
                .beginControlFlow("if (cause == null)")
                .addStatement("throw new RuntimeException(e)")
                .endControlFlow()
                // each library-thrown exception we should catch manually here and re-throw
                // this enables us to use checked exceptions in this sync wrapper
                .beginControlFlow("if (cause instanceof $T)", preCheckStatusException)
                .addStatement("throw (($T)cause)", preCheckStatusException)
                .endControlFlow();

            for (var e : moreExceptions) {
                methodSyncWithTimeoutBuilder
                    .beginControlFlow("if (cause instanceof $T)", e)
                    .addStatement("throw (($T)cause)", e)
                    .endControlFlow();
            }

            methodSyncWithTimeoutBuilder
                // any other exception must be thrown as an unchecked exception
                .addStatement("throw new RuntimeException(cause)")
                .endControlFlow();

            var tyBuilder = interfaceBuilder(interfaceName);

            if (isGeneric) {
                tyBuilder.addTypeVariable((TypeVariableName) outputType);
            }

            if (annotation.onClient()) {
                tyBuilder.addMethod(methodGetRequestTimeout);
            }

            var ty = tyBuilder
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodAsyncBuilder.build())
                .addMethod(methodAsyncBiConsumerBuilder.build())
                .addMethod(methodAsyncBiConsumerWithTimeoutBuilder.build())
                .addMethod(methodAsyncConsumerBuilder.build())
                .addMethod(methodAsyncConsumerWithTimeoutBuilder.build())
                .addMethod(methodSyncBuilder.build())
                .addMethod(methodSyncWithTimeoutBuilder.build())
                .build();

            var output = JavaFile.builder("com.hedera.hashgraph.sdk", ty).build();

            try {
                // Write to $OUT_DIR
                output.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}
