package br.com.costumerental.nfe.xml;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Map;

/**
 * Resolve o código IBGE de município a partir do nome normalizado da cidade e UF.
 * Usado quando o frontend não envia o cityCode diretamente.
 */
@Component
public class IbgeCityCodeResolver {

    private static final Map<String, String> CITY_CODES = Map.ofEntries(
        Map.entry("SAO CARLOS-SP", "3548906"),
        Map.entry("SAO PAULO-SP", "3550308"),
        Map.entry("RIO DE JANEIRO-RJ", "3304557"),
        Map.entry("BELO HORIZONTE-MG", "3106200"),
        Map.entry("CURITIBA-PR", "4106902"),
        Map.entry("PORTO ALEGRE-RS", "4314902"),
        Map.entry("SALVADOR-BA", "2927408"),
        Map.entry("RECIFE-PE", "2611606"),
        Map.entry("FORTALEZA-CE", "2304400"),
        Map.entry("BRASILIA-DF", "5300108"),
        Map.entry("GOIANIA-GO", "5208707"),
        Map.entry("VITORIA-ES", "3205309"),
        Map.entry("FLORIANOPOLIS-SC", "4205407"),
        Map.entry("CAMPINAS-SP", "3509502"),
        Map.entry("RIBEIRAO PRETO-SP", "3543402"),
        Map.entry("SAO JOSE DOS CAMPOS-SP", "3549904"),
        Map.entry("SOROCABA-SP", "3552205"),
        Map.entry("SANTOS-SP", "3548500"),
        Map.entry("BAURU-SP", "3506003"),
        Map.entry("MARILIA-SP", "3529005")
    );

    public String resolve(String cityName, String state) {
        if (cityName == null || state == null) {
            return null;
        }
        String key = normalize(cityName) + "-" + normalize(state);
        String code = CITY_CODES.get(key);
        if (code != null) {
            return code;
        }
        return CITY_CODES.getOrDefault("SAO CARLOS-" + normalize(state), "3548906");
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value.toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.trim();
    }
}
