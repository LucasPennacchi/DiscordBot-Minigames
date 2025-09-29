package br.com.bot.games.reflexo;

import br.com.bot.utils.NormalizadorDeTexto;

/**
 * Uma classe de utilidade para calcular uma pontuação complexa para o Jogo de Reflexo.
 * <p>
 * O cálculo é feito comparando uma frase correta (gabarito) com a entrada de um usuário,
 * seguindo um conjunto de regras que pontuam acertos de caracteres e posição, e penalizam
 * erros e caracteres extras.
 *
 * @author Lucas
 */
public class VerificadorDePontos {

    /**
     * Calcula a pontuação do input do usuário comparado à frase correta.
     * <p>
     * O método primeiro normaliza ambas as strings (para minúsculas e sem acentos) e depois
     * executa um algoritmo de três passadas para calcular a pontuação final.
     * As regras de pontuação são as seguintes:
     * <ul>
     * <li><b>+2 pontos:</b> Para cada caractere correto na posição correta.</li>
     * <li><b>+1 ponto:</b> Para cada caractere correto, mas na posição incorreta.</li>
     * <li><b>-1 ponto:</b> Para cada caractere incorreto (que não existe no gabarito).</li>
     * <li><b>-2 pontos:</b> Para cada caractere extra (além do tamanho do gabarito).</li>
     * </ul>
     * A pontuação final nunca será menor que zero.
     *
     * @param fraseCorreta   A string que o usuário deveria ter digitado.
     * @param inputDoUsuario A string que o usuário de fato digitou.
     * @return A pontuação final calculada, com um mínimo de 0.
     */
    public int calcularPontuacao(String fraseCorreta, String inputDoUsuario) {
        // Normaliza as strings para uma comparação justa
        String gabaritoNormalizado = NormalizadorDeTexto.removerAcentos(fraseCorreta.toLowerCase());
        String respostaNormalizada = NormalizadorDeTexto.removerAcentos(inputDoUsuario.toLowerCase());

        char[] gabarito = gabaritoNormalizado.toCharArray();
        char[] resposta = respostaNormalizada.toCharArray();

        boolean[] gabaritoUtilizado = new boolean[gabarito.length];
        boolean[] respostaUtilizada = new boolean[resposta.length];

        int pontuacao = 0;

        // 1ª Passada: Verificar acertos perfeitos (+2 pontos)
        for (int i = 0; i < gabarito.length && i < resposta.length; i++) {
            if (gabarito[i] == resposta[i]) {
                pontuacao += 2;
                gabaritoUtilizado[i] = true;
                respostaUtilizada[i] = true;
            }
        }

        // 2ª Passada: Verificar letras corretas em posições erradas (+1 ponto)
        for (int i = 0; i < resposta.length; i++) {
            if (!respostaUtilizada[i]) {
                for (int j = 0; j < gabarito.length; j++) {
                    if (!gabaritoUtilizado[j] && resposta[i] == gabarito[j]) {
                        pontuacao += 1;
                        gabaritoUtilizado[j] = true;
                        respostaUtilizada[i] = true;
                        break;
                    }
                }
            }
        }

        // 3ª Passada: Penalizar letras erradas e extras (-1 e -2 pontos)
        for (int i = 0; i < resposta.length; i++) {
            if (!respostaUtilizada[i]) {
                if (i < gabarito.length) {
                    pontuacao -= 1;
                } else {
                    pontuacao -= 2;
                }
            }
        }

        return Math.max(0, pontuacao);
    }
}