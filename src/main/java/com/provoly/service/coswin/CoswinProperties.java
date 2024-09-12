package com.provoly.service.coswin;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "coswin", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface CoswinProperties {

    @WithDefault("https")
    String scheme();

    @WithDefault("8080")
    int port();

    @WithDefault("coswin.host.fr")
    String host();

    @WithDefault("username")
    String username();

    @WithDefault("password")
    String password();

    @WithDefault("datasource")
    String dataSource();
}
