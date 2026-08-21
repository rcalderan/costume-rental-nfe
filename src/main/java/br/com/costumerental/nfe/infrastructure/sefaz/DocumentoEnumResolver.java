package br.com.costumerental.nfe.infrastructure.sefaz;

import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;

/**
 * Resolve o {@link DocumentoEnum} (NFe ou NFCe) da biblioteca Java_NFe a
 * partir do modelo fiscal (55/65) ou da chave de acesso de 44 digitos, cujo
 * modelo ocupa as posicoes 21 e 22 (indices 20-21, base 0).
 */
final class DocumentoEnumResolver {

    private DocumentoEnumResolver() {
    }

    static DocumentoEnum fromModelo(String modelo) {
        return DocumentoEnum.getByModelo(modelo);
    }

    static DocumentoEnum fromAccessKey(String accessKey) {
        if (accessKey == null || accessKey.length() < 22) {
            throw new IllegalArgumentException(
                    "Chave de acesso invalida para identificar o modelo do documento: " + accessKey);
        }
        return fromModelo(accessKey.substring(20, 22));
    }
}
