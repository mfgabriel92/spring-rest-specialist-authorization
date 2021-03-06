package br.gabriel.springrestspecialistauthentication.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private static final int TOKEN_VALIDITY = 60 * 60;

    private static final int REFRESH_TOKEN_VALIDITY = 60 * 60 * 24 * 7;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Qualifier("theUserDetails")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private KeystoreProperties keystoreProperties;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("web-client")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("password", "refresh_token")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .refreshTokenValiditySeconds(REFRESH_TOKEN_VALIDITY)
                    .scopes("READ", "WRITE", "DELETE").and()

                .withClient("background-app")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("client_credentials")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .scopes("READ", "WRITE", "DELETE").and()

                .withClient("analytics-app")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("authorization_code")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .redirectUris("http://another-uri.com")
                    .scopes("READ", "WRITE", "DELETE").and()

                .withClient("webadmin")
                    .authorizedGrantTypes("implicit")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .redirectUris("http://another-uri.com")
                    .scopes("READ", "WRITE", "DELETE").and()

                .withClient("introspect")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("password");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
            .checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
            .reuseRefreshTokens(false)
            .tokenGranter(tokenGranter(endpoints))
            .accessTokenConverter(jwtAccessTokenConverter())
            .approvalStore(approvalStore(endpoints.getTokenStore()))
            .tokenEnhancer(tokenEnhancer());
    }

    @Bean
    protected JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();

        ClassPathResource file = new ClassPathResource(keystoreProperties.getPath());
        String password = keystoreProperties.getPassword();
        String alias = keystoreProperties.getAlias();

        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(file, password.toCharArray());
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias);

        jwtAccessTokenConverter.setKeyPair(keyPair);

        return jwtAccessTokenConverter;
    }

    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        PkceAuthorizationCodeTokenGranter pkceAuthorizationCodeTokenGranter = new PkceAuthorizationCodeTokenGranter(
            endpoints.getTokenServices(),
            endpoints.getAuthorizationCodeServices(),
            endpoints.getClientDetailsService(),
            endpoints.getOAuth2RequestFactory()
        );
        List<TokenGranter> granters = Arrays.asList(pkceAuthorizationCodeTokenGranter, endpoints.getTokenGranter());

        return new CompositeTokenGranter(granters);
    }

    private ApprovalStore approvalStore(TokenStore tokenStore) {
        TokenApprovalStore approvalStore = new TokenApprovalStore();
        approvalStore.setTokenStore(tokenStore);

        return approvalStore;
    }

    private TokenEnhancerChain tokenEnhancer() {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();

        enhancerChain.setTokenEnhancers(Arrays.asList(
            new JwtTokenEnhancer(),
            jwtAccessTokenConverter()
        ));

        return enhancerChain;
    }
}
