package ru.apolyakov;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.PushApplicationManifestRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.Map;

@Component
public class ApplicationDeployer {
    private final Log log = LogFactory.getLog(getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    public ApplicationDeployer(CloudFoundryOperations cloudFoundryOperations) {
        this.cloudFoundryOperations = cloudFoundryOperations;
    }

    public Mono<Void> deployApplication(File jar, String applicationName,
                                        Map<String, String> envOg, Duration timeout, String... svcs)
    {
        return cloudFoundryOperations.applications().push(pushApp(jar, applicationName))
                .then(bindServices(applicationName, svcs))
                .then(setEnvironmentVariables(applicationName, envOg))
                .then(startApplication(applicationName, timeout));
    }

    private PushApplicationRequest pushApp(File jar, String applicationName)
    {
        return PushApplicationRequest.builder()
                .name(applicationName).noStart(true)
                .randomRoute(true)
                .buildpack("https://github.com/cloudfoundry/java-buildpack.git")
                .application(jar.toPath())
                .instances(1)
                .build();
    }

    private Mono<Void> bindServices(String applicationName, String[] svcs)
    {
        return Flux.just(svcs)
                .flatMap(svc -> {
                    BindServiceInstanceRequest request = BindServiceInstanceRequest.builder()
                            .applicationName(applicationName)
                            .serviceInstanceName(svc)
                            .build();
                    return cloudFoundryOperations.services().bind(request);
                })
                .then();
    }

    private Mono<Void> startApplication(String applicationName, Duration timeout)
    {
        return cloudFoundryOperations.applications()
                .start(StartApplicationRequest.builder()
                        .name(applicationName)
                        .stagingTimeout(timeout)
                        .startupTimeout(timeout)
                        .build());
    }

    private Mono<Void> setEnvironmentVariables(String applicationName, Map<String, String> env)
    {
        return Flux.fromIterable(env.entrySet())
                .flatMap(entry -> cloudFoundryOperations.applications().setEnvironmentVariable(
                        SetEnvironmentVariableApplicationRequest.builder()
                                .name(applicationName)
                                .variableName(entry.getKey())
                                .variableValue(entry.getValue())
                                .build()
                )).then();
    }
}
