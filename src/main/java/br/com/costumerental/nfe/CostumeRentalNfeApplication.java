package br.com.costumerental.nfe;

import br.com.costumerental.nfe.config.NfeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NfeProperties.class)
public class CostumeRentalNfeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CostumeRentalNfeApplication.class, args);
    }
}
