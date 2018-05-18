package courgette.api.testng;

import courgette.api.CourgetteOptions;
import courgette.api.CourgetteRunLevel;
import courgette.runtime.*;
import cucumber.runtime.model.CucumberFeature;
import gherkin.pickles.PickleLocation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public abstract class TestNGCourgette {
    private CourgetteProperties courgetteProperties;
    private List<CourgetteRunnerInfo> runnerInfoList;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        final CourgetteOptions courgetteOptions = new CourgetteRunOptions(this.getClass());
        courgetteProperties = new CourgetteProperties(courgetteOptions, createSessionId(), courgetteOptions.threads());

        CourgetteFeatureLoader courgetteFeatureLoader = new CourgetteFeatureLoader(courgetteProperties, this.getClass().getClassLoader());
        List<CucumberFeature> cucumberFeatures = courgetteFeatureLoader.getCucumberFeatures();

        runnerInfoList = new ArrayList<>();

        if (courgetteOptions.runLevel().equals(CourgetteRunLevel.FEATURE)) {
            cucumberFeatures.forEach(feature -> runnerInfoList.add(new CourgetteRunnerInfo(courgetteProperties, feature, null)));
        } else {
            final Map<PickleLocation, CucumberFeature> scenarios = courgetteFeatureLoader.getCucumberScenarios();
            scenarios
                    .keySet()
                    .forEach(location -> runnerInfoList.add(new CourgetteRunnerInfo(courgetteProperties, scenarios.get(location), location.getLine())));
        }
    }

    @Test
    public void parallelRun() {
        final CourgetteRunner courgetteRunner = new CourgetteRunner(runnerInfoList, courgetteProperties);

        if (courgetteRunner.canRunFeatures()) {
            courgetteRunner.run();
            courgetteRunner.createReport();
            courgetteRunner.createCourgetteReport();
        }

        if (courgetteRunner.allFeaturesPassed()) {
            assertTrue(true);
        } else {
            courgetteRunner.createRerunFile();
            fail("There were failures during the test run. Please refer to Courgette / Cucumber reports.");
        }
    }

    private String createSessionId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}