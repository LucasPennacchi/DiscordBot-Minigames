package br.com.bot.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class NormalizadorDeTexto {

    /**
     * Remove todos os acentos e marcas diacríticas de uma string.
     * Converte 'ç' para 'c'.
     *
     * @param texto A string original com acentos.
     * @return A string normalizada sem acentos.
     */
    public static String removerAcentos(String texto) {
        if (texto == null) {
            return null;
        }

        // 1. Decompõe a string, separando letras de seus acentos
        String textoDecomposto = Normalizer.normalize(texto, Normalizer.Form.NFD);

        // 2. Cria um padrão de regex para encontrar todas as marcas diacríticas (acentos)
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        // 3. Remove as marcas encontradas, deixando apenas os caracteres base
        return pattern.matcher(textoDecomposto).replaceAll("");
    }
}