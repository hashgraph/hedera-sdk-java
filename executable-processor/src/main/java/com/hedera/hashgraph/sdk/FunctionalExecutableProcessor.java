package com.hedera.hashgraph.sdk;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.threeten.bp.Duration;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;

@SuppressWarnings({"AndroidJdkLibsChecker", "Java7ApiChecker"})
@SupportedAnnotationTypes("com.hedera.hashgraph.sdk.FunctionalExecutable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FunctionalExecutableProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var clientClazz = ClassName.get("com.hedera.hashgraph.sdk", "Client");
        var preCheckStatusException = ClassName.get("com.hedera.hashgraph.sdk", "HederaPreCheckStatusException");

        for (var element : roundEnv.getElementsAnnotatedWith(FunctionalExecutable.class)) {
            // expecting {name}Async
            var methodAsyncName = element.getSimpleName().toString();
            var methodName = methodAsyncName.replace("Async", "");

            // create With{Name} as the final output type name
            var interfaceName = "With" + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);

            // TODO: Support a non-generic returns designation
            var outputType = TypeVariableName.get("O");

            // Common types
            var throwableTy = ClassName.get(Throwable.class);

            // Callbacks
            var outCallbackTy = ParameterizedTypeName.get(ClassName.get(Consumer.class), outputType);
            var errCallbackTy = ParameterizedTypeName.get(ClassName.get(Consumer.class), throwableTy);
            var biCallbackTy = ParameterizedTypeName.get(ClassName.get(BiConsumer.class), outputType, throwableTy);

            // Return type for {name}Async()
            var futureReturnTy = ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), outputType);

            /*
CompletableFuture<O> executeAsync(Client client);
             */

            // TODO: {name}Async(Client)
            var methodAsync = methodBuilder(methodAsyncName)
                .addParameter(clientClazz, "client")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(futureReturnTy)
                .build();

            /*
void executeAsync(Client client, java8.util.function.BiConsumer<O, Throwable> callback) {
    executeAsync(client, DEFAULT_TIMEOUT, callback);
}
             */

            // TODO: {name}Async(Client, BiConsumer)
            var methodAsyncBiConsumer = methodBuilder(methodAsyncName)
                .addParameter(clientClazz, "client")
                .addParameter(biCallbackTy, "callback")
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addStatement("$L (client, $T.ofSeconds(30), callback)", methodAsyncName, Duration.class)
                .returns(void.class)
                .build();

            /*
void executeAsync(Client client, Duration timeout, BiConsumer<O, Throwable> callback) {
    executeAsync(client)
        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .whenComplete(callback);
}
             */

            // TODO: {name}Async(Client, Duration, BiConsumer)
            var methodAsyncBiConsumerWithTimeout = methodBuilder(methodAsyncName)
                .addParameter(clientClazz, "client")
                .addParameter(Duration.class, "timeout")
                .addParameter(biCallbackTy, "callback")
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addStatement("$L (client)" +
                        ".orTimeout(timeout.toMillis(), $T.MILLISECONDS)" +
                        ".whenComplete(callback)",
                    methodAsyncName, TimeUnit.class)
                .returns(void.class)
                .build();

            /*
void executeAsync(Client client, java8.util.function.Consumer<O> onSuccess, java8.util.function.Consumer<Throwable> onFailure) {
    executeAsync(client, DEFAULT_TIMEOUT, onSuccess, onFailure);
}
             */

            // TODO: {name}Async(Client, Consumer, Consumer)
            var methodAsyncConsumer = methodBuilder(methodAsyncName)
                .addParameter(clientClazz, "client")
                .addParameter(outCallbackTy, "onSuccess")
                .addParameter(errCallbackTy, "onFailure")
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addStatement("$L (client, $T.ofSeconds(30), onSuccess, onFailure)", methodAsyncName, Duration.class)
                .returns(void.class)
                .build();

            /*
void executeAsync(Client client, Duration timeout, Consumer<O> onSuccess, Consumer<Throwable> onFailure) {
    executeAsync(client)
        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
        .whenComplete((response, error) -> {
            if (error != null) onFailure.accept(error);
            else onSuccess.accept(response);
        });
}
             */

            // TODO: {name}Async(Client, Duration, Consumer, Consumer)
            var methodAsyncConsumerWithTimeout = methodBuilder(methodAsyncName)
                .addParameter(clientClazz, "client")
                .addParameter(Duration.class, "timeout")
                .addParameter(outCallbackTy, "onSuccess")
                .addParameter(errCallbackTy, "onFailure")
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addStatement("$L (client)" +
                        ".orTimeout(timeout.toMillis(), $T.MILLISECONDS)" +
                        ".whenComplete((output, error) -> {" +
                        "if (error != null) { onFailure.accept(error); }" +
                        "else { onSuccess.accept(output); }" +
                        "})",
                    methodAsyncName, TimeUnit.class)
                .returns(void.class)
                .build();

            /*
O execute(Client client) throws TimeoutException, HederaPreCheckStatusException {
    return execute(client, DEFAULT_TIMEOUT);
}
             */

            // TODO: {name}(Client)
            var methodSync = methodBuilder(methodName)
                .addParameter(clientClazz, "client")
                .addException(TimeoutException.class)
                .addException(preCheckStatusException)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addStatement("return $L (client, $T.ofSeconds(30))", methodName, Duration.class)
                .returns(outputType)
                .build();

            /*
O execute(Client client, Duration timeout) throws TimeoutException {
    try {
        return executeAsync(client).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } catch (ExecutionException e) {
        var cause = e.getCause();

        // If there is no cause, just re-throw
        if (cause == null) throw new RuntimeException(e);

        // TODO: For explicit errors we want to have as checked, we need to
        //       do instanceof checks and bridge that here

        // Unwrap and re-wrap as a RuntimeException
        throw new RuntimeException(cause);
    }
}
             */

            // TODO: {name}(Client)
            var methodSyncWithTimeout = methodBuilder(methodName)
                .addParameter(clientClazz, "client")
                .addParameter(Duration.class, "timeout")
                .addException(TimeoutException.class)
                .addException(preCheckStatusException)
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .beginControlFlow("try")
                .addStatement("return $L (client).get(timeout.toMillis(), $T.MILLISECONDS)", methodAsyncName, TimeUnit.class)
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
                .endControlFlow()
                // any other exception must be thrown as an unchecked exception
                .addStatement("throw new RuntimeException(cause)")
                .endControlFlow()
                .returns(outputType)
                .build();

            var ty = interfaceBuilder(interfaceName)
                // TODO: type variable should be optional
                .addTypeVariable(outputType)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodAsync)
                .addMethod(methodAsyncBiConsumer)
                .addMethod(methodAsyncBiConsumerWithTimeout)
                .addMethod(methodAsyncConsumer)
                .addMethod(methodAsyncConsumerWithTimeout)
                .addMethod(methodSync)
                .addMethod(methodSyncWithTimeout)
                .build();

            var output = JavaFile.builder("com.hedera.hashgraph.sdk", ty).build();

            try {
                // Write to $OUT_DIR
                output.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}
