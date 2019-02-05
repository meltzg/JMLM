package org.meltzg.jmlm;

import org.meltzg.jmlm.ui.configuration.ScreensConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import(ScreensConfiguration.class)
@EnableAutoConfiguration
@EnableJpaRepositories("org.meltzg.jmlm.repositories")
public class JmlmApplicationConfiguration {
}
