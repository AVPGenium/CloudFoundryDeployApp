package ru.apolyakov;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.apolyakov.ApplicationDeployer;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRunServices {
    @Autowired
    private ApplicationDeployer applicationDeployer;
    @Autowired
    private ServicesDeployer servicesDeployer;

    @Test
    public void testDeploy()
    {
        File projectFolder = new File(new File("."), "../spring-configuration");
        File jar = new File(projectFolder, "target/test.jar");
        String applicationName = "test-app";
        String mysqlSvc = "test-mysql";
        String planName = "100mb";
        Map<String,String> env = new HashMap<>();
        env.put("SPRING_PROFILES_ACTIVE", "cloud");
        Duration timeout = Duration.ofMinutes(5);

        servicesDeployer.deployService(applicationName, mysqlSvc, "p-mysql", planName)
                .then(applicationDeployer.deployApplication(jar, applicationName, env, timeout, mysqlSvc))
                .block();
    }

    @Test
    public void contextLoads() {
    }
}
