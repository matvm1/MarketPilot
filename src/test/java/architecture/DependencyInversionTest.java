package architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.marketpilot")
public class DependencyInversionTest {

    @ArchTest
    void domainDoesNotDependOnExternalLayers(JavaClasses classes) {
        innerLayerDoesNotDependOnOuterLayer("domain", "application", "adapters")
                .check(classes);
    }

    @ArchTest
    void domainEntitiesDoNotDependOnDomainServices(JavaClasses classes) {
        innerLayerDoesNotDependOnOuterLayer("domain.entities", "domain.services")
                .check(classes);
    }

    @ArchTest
    void applicationDoesNotDependOnExternalLayers(JavaClasses classes) {
        innerLayerDoesNotDependOnOuterLayer("application", "adapters")
                .check(classes);
    }

    private ArchRule innerLayerDoesNotDependOnOuterLayer(String innerLayer, String... outerLayers) {
        innerLayer = ".." + innerLayer + "..";

        for (int i = 0; i < outerLayers.length; i++) {
            outerLayers[i] = ".." + outerLayers[i] + "..";
        }

        return noClasses()
                .that().resideInAPackage(innerLayer)
                .should().dependOnClassesThat().resideInAnyPackage(outerLayers);
    }
}