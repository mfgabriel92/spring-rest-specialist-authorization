package br.gabriel.springrestspecialistauthentication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties("srs.jwt.keystore")
public class KeystoreProperties {
    private String path;

    private String password;

    private String alias;
}
