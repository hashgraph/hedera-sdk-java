open module com.hedera.hashgraph.sdk.integration.test {
    requires com.hedera.hashgraph.sdk;
    requires headlong;
    requires org.assertj.core;
    requires org.bouncycastle.provider;
    requires org.junit.jupiter.api;

    requires static com.google.errorprone.annotations;
    requires static java.annotation;
}
