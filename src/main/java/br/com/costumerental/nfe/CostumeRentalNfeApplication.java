package br.com.costumerental.nfe;

import br.com.costumerental.nfe.config.NfeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(NfeProperties.class)
@EnableCaching
@EnableAsync
public class CostumeRentalNfeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CostumeRentalNfeApplication.class, args);
    }
}
