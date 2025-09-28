package br.com.bot.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Classe utilitária para manipulação e sanitização de strings.
 * Fornece métodos estáticos para operações comuns de texto.
 */
public class NormalizadorDeTexto {

    /**
     * Remove todos os acentos e marcas diacríticas de uma string.
     * Este metodo utiliza o Normalizer do Java para decompor os caracteres
     * e depois remove as marcas de combinação com uma expressão regular.
     *
     * @param texto A string original que pode conter acentos (ex: "ação").
     * @return A string normalizada sem acentos (ex: "acao"). Retorna null se a entrada for null.
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