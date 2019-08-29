package ru.apolyakov;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class ServicesDeployer {
    private final Log log = LogFactory.getLog(getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    public ServicesDeployer(CloudFoundryOperations cloudFoundryOperations) {
        this.cloudFoundryOperations = cloudFoundryOperations;
    }

    public Mono<Void> deployService(String applicationName, String svcInstanceName,
                             String svcTypeName, String planName)
    {
        // получение всех экземпляров приложения и их кэширование
        // чтобы другие подписчики не выполняли повторно REST-вызовы
        return cloudFoundryOperations.services().listInstances().cache()
                .filter(serviceInstance -> serviceInstance.getName().equalsIgnoreCase(svcInstanceName))
                .transform(unbindAndDelete(applicationName, svcInstanceName))
                .thenEmpty(createService(svcInstanceName, svcTypeName, planName));
    }


    private Function<Flux<ServiceInstanceSummary>, Publisher<Void>> unbindAndDelete(
            String applicationName, String svcInstanceName)
    {
        return siFlux -> Flux.concat(
                unbind(applicationName, svcInstanceName, siFlux),
                delete(svcInstanceName, siFlux)
        );
    }

    private Mono<Void> createService(String svcInstanceName,
                                     String svcTypeName, String planName)
    {
        return cloudFoundryOperations.services().createInstance(
                CreateServiceInstanceRequest.builder()
                        .serviceName(svcTypeName)
                        .serviceInstanceName(svcInstanceName)
                        .planName(planName)
                        .build()
        );
    }

    private Flux<Void> unbind ( String applicationName, String svcInstanceName,
                                Flux<ServiceInstanceSummary> serviceInstanceSummaryFlux)
    {
        return serviceInstanceSummaryFlux.filter(serviceInstance -> serviceInstance.getApplications().contains(applicationName))
                .flatMap(si -> cloudFoundryOperations.services().unbind(
                   UnbindServiceInstanceRequest.builder()
                           .applicationName(applicationName)
                           .serviceInstanceName(svcInstanceName)
                           .build())
                );
    }

    private Flux<Void> delete (String svcInstanceName,
                               Flux<ServiceInstanceSummary> serviceInstanceSummaryFlux)
    {
        return  serviceInstanceSummaryFlux.flatMap(serviceInstance ->
                cloudFoundryOperations.services().deleteInstance(
                        DeleteServiceInstanceRequest.builder().name(svcInstanceName).build()
                ));
    }
}
