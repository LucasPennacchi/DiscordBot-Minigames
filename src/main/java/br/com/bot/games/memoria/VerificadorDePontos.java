package br.com.bot.games.memoria;

import br.com.bot.utils.NormalizadorDeTexto;

public class VerificadorDePontos {

    public int calcularPontuacao(String fraseCorreta, String inputDoUsuario) {

        // Normaliza ambas as strings para remover acentos e converte para minúsculas
        // antes de qualquer outra operação.
        String gabaritoNormalizado = NormalizadorDeTexto.removerAcentos(fraseCorreta.toLowerCase());
        String respostaNormalizada = NormalizadorDeTexto.removerAcentos(inputDoUsuario.toLowerCase());

        // O resto do metodo continua exatamente igual, mas agora usando as strings normalizadas
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