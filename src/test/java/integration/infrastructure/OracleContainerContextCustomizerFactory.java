package integration.infrastructure;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.AnnotationUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

public class OracleContainerContextCustomizerFactory implements ContextCustomizerFactory {
    @Override
    public @Nullable ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        if (!AnnotationUtils.isAnnotated(testClass, OracleDataJpaTest.class))
            return null;

        return new OracleContainerContextCustomizer();
    }
}
