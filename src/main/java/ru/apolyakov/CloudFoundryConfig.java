package ru.apolyakov;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class CloudFoundryConfig {
    /**
     * Контекст для подключения к сервисам Cloud Foundry
     * (описывает экземпляр Cloud Foundry на который нужно нацелиться)
     * @param apiHost api endpoint
     * @return контекст по умолчанию
     */
    @Bean
    public DefaultConnectionContext connectionContext(@Value("${cf.api}") String apiHost)
    {
        if (apiHost.contains("://"))
        {
            apiHost = apiHost.split("://")[1];
        }
        return DefaultConnectionContext.builder().apiHost(apiHost).build();
    }

    /**
     * Провайдер токена для доступа к сервисам Cloud Foundry на основе логина/пароля
     * @param username логин
     * @param password пароль
     * @return токен доступа, использующий логин/пароль пользователя
     */
    @Bean
    public PasswordGrantTokenProvider tokenProvider(@Value("${cf.username}") String username,
                                                    @Value("${cf.password}") String password)
    {
        return PasswordGrantTokenProvider.builder().username(username).password(password).build();
    }

    /**
     * Клиент для Cloud Foundry REST API
     * @param connectionContext контекст подключения к сервисам
     * @param tokenProvider провайдер токена
     * @return клиент для подключения к Cloud Foundry
     */
    @Bean
    public ReactorCloudFoundryClient cloudFoundryClient (
            ConnectionContext connectionContext, TokenProvider tokenProvider)
    {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    /**
     * Клиент для Doppler (подсистемы, основанной на веб-сокетах)
     * @param connectionContext контекст подключения к сервисам
     * @param tokenProvider провайдер токена
     * @return клиент для подключения к Cloud Foundry
     */
    @Bean
    public ReactorDopplerClient reactorDopplerClient(
            ConnectionContext connectionContext, TokenProvider tokenProvider)
    {
        return ReactorDopplerClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    /**
     * Клиент для UAA
     * @param connectionContext контекст подключения к сервисам
     * @param tokenProvider провайдер токена
     * @return клиент для подключения к Cloud Foundry
     */
    @Bean
    public ReactorUaaClient reactorUaaClient (
            ConnectionContext connectionContext, TokenProvider tokenProvider)
    {
        return ReactorUaaClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    @Bean
    public DefaultCloudFoundryOperations cloudFoundryOperations(
            CloudFoundryClient cloudFoundryClient, ReactorDopplerClient reactorDopplerClient,
            ReactorUaaClient reactorUaaClient, @Value("${cf.org}") String organization,
            @Value("${cf.space}") String space)
    {
        return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .dopplerClient(reactorDopplerClient)
                .uaaClient(reactorUaaClient)
                .organization(organization)
                .space(space)
                .build();
    }
}
