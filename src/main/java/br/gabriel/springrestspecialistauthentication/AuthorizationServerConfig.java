package br.gabriel.springrestspecialistauthentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

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

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${srs.jwt.signing-key}")
    private String signingKey;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("web-client")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("password", "refresh_token")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .refreshTokenValiditySeconds(REFRESH_TOKEN_VALIDITY)
                    .scopes("WRITE", "READ", "DELETE").and()

                .withClient("background-app")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("client_credentials")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .scopes("WRITE", "READ", "DELETE").and()

                .withClient("analytics-app")
                    .secret(passwordEncoder.encode("123"))
                    .authorizedGrantTypes("authorization_code")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .redirectUris("http://another-uri.com")
                    .scopes("WRITE", "READ", "DELETE").and()

                .withClient("webadmin")
                    .authorizedGrantTypes("implicit")
                    .accessTokenValiditySeconds(TOKEN_VALIDITY)
                    .redirectUris("http://another-uri.com")
                    .scopes("WRITE", "READ", "DELETE").and()

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
            .accessTokenConverter(jwtAccessTokenConverter());
    }

    @Bean
    protected JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(signingKey);

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
}
