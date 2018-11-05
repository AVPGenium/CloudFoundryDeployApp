package ru.apolyakov;//import org.cloudfoundry.cloudFoundryClient.CloudFoundryClient;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static ru.apolyakov.CloudFoundryService.CLOUD_FOUNDRY_SERVICE_NAME;

@Service(CLOUD_FOUNDRY_SERVICE_NAME)
public class CloudFoundryService {
    public static final String CLOUD_FOUNDRY_SERVICE_NAME = "CloudFoundryService";

    @Value("${cf.organization}")
    private String orgName;
    @Value("${cf.space}")
    private String spaceName;

    String appName = "testApp1";
    String pathToApp = "test-application.zip";
    boolean noStart = false;

    private final CloudFoundryClient cloudFoundryClient;
    private final DefaultCloudFoundryOperations cloudFoundryOperations;

    @Autowired
    public CloudFoundryService(CloudFoundryClient cloudFoundryClient, DefaultCloudFoundryOperations cloudFoundryOperations) {
        this.cloudFoundryClient = cloudFoundryClient;
        this.cloudFoundryOperations = cloudFoundryOperations;
    }

    @Deprecated
    private static URL getTargetURL(String target) {
        try {
            return URI.create(target).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("The target URL is not valid: " + e.getMessage());
        }
    }

//    public void runApp() throws IOException {
//        // login
//        CloudCredentials credentials = new CloudCredentials(user, password);
//        URL target = getTargetURL("https://api.ng.bluemix.net");
//        CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(credentials, target);
//        cloudFoundryClient.login();
//
//        String appName = "my-node-app";
//        String baseUrl = "http://" + appName + ".mybluemix.net/";
//        List<String> uris = Arrays.asList(baseUrl);
//        Integer memory = 128;
//        List<String> serviceNames = new ArrayList<String>();
//        serviceNames.add("my-service-test");
//        String command = "node app.js";
//        String buildpack = "sdk-for-nodejs";
//        // Создание нового приложения
//        cloudFoundryClient.createApplication(appName, new Staging(command, buildpack), memory, uris, serviceNames);
//        // Загрузка zip-файла
//        File appFile = new File("nodeApp.zip");
//        cloudFoundryClient.uploadApplication(appName, appFile);
//        // Запуск приложения
//        cloudFoundryClient.startApplication(appName);
//        cloudFoundryClient.logout();
//    }

    public Mono<ApplicationDetail> requestGetApplication(CloudFoundryOperations cloudFoundryOperations, String applicationtName) {
        return cloudFoundryOperations.applications()
                .get(GetApplicationRequest.builder()
                        .name(applicationtName)
                        .build());
    }

    public Mono<Void> createApplication(DefaultCloudFoundryOperations cloudFoundryOperations, Path application, String name, Boolean noStart) {
        return cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                        .buildpack("staticfile_buildpack")
                        .diskQuota(512)
                        .healthCheckType(ApplicationHealthCheck.NONE)
                        .memory(64)
                        .name(name)
                        //.noStart(noStart)
                        .routePath("abcdefg" + ".bluemix.net")
                        .path(application)
                        .build());
    }

    public Mono<Void> createService(DefaultCloudFoundryOperations cloudFoundryOperations, Path application, String name, Boolean noStart) {
        return cloudFoundryOperations.services()
                .createInstance(CreateServiceInstanceRequest.builder()
                        .serviceInstanceName("mysql")
                        .build());
    }

    public void printOrganizationsInfo()
    {
        CountDownLatch latch = new CountDownLatch(1);

        cloudFoundryOperations.organizations()
                .list()
                .map(OrganizationSummary::getName)
                .subscribe(System.out::println, t->{
                    t.printStackTrace();
                    latch.countDown();
                }, latch::countDown);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
//
    public void auth() throws IOException {
        List<OrganizationDetail> elements = new ArrayList<>();
        String organization = cloudFoundryOperations.getOrganization();
        CloudFoundryClient foundryClient = cloudFoundryOperations.getCloudFoundryClient();
        Mono<List<OrganizationSummary>> organizationDetailMono =
                cloudFoundryOperations.organizations()
                .list()
                .collectList();
        //.get(OrganizationInfoRequest.builder().name("ke1720pav31@susu.ru").build());

        createApplication(cloudFoundryOperations,
                new ClassPathResource("test-application.zip").getFile().toPath(),
                appName, false)
                .thenMany(cloudFoundryOperations.applications()
                        .list());

        cloudFoundryOperations.applications()
                .start((StartApplicationRequest.builder().name(appName).build()));

//        Mono.when(createApplication(cloudFoundryOperations, new ClassPathResource("test-application.zip").getFile().toPath(), appName, false))
//                .then(requestGetApplication(cloudFoundryOperations, appName))
//                .map(ApplicationDetail::getRequestedState)
//                .subscribe(System.out::println);


//        System.out.printf("%nSpaces:%n");
//        Flux<SpaceSummary> map = cloudFoundryClient.spacesV3()
//                .list(ListSpacesRequest.builder()
//                        .page(1)
//                        .build())
//                .flatMapIterable(ListSpacesResponse::getResources)
//                .map(resource -> SpaceSummary.builder()
//                        .id(resource.getId())
//                        .name(resource.getName())
//                        .build());
    }
}
