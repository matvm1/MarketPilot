package architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@SuppressWarnings("unused")
@AnalyzeClasses(packages = "domain")
public class DependencyInversionTest {
    @ArchTest
    static final ArchRule domainEntitiesDoNotDependOnExternalLayers = noClasses()
            .that().resideInAPackage("..domain.entities..")
            .should().dependOnClassesThat().resideInAnyPackage("..domain.services..", "..application..");
}
