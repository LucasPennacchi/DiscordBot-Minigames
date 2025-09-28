package br.com.bot.games.forca;

import br.com.bot.core.GameManager;
import br.com.bot.shared.Game;
import br.com.bot.utils.NormalizadorDeTexto;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ForcaGame extends Game {
    private final String palavraSecreta;
    private final int maxErros;
    private final Set<Character> letrasCorretas = new HashSet<>();
    private final Set<Character> letrasErradas = new HashSet<>();
    // NOVO: Guarda as tentativas de palavras erradas
    private final Set<String> palavrasErradas = new HashSet<>();
    private String messageId;

    public ForcaGame(long tempoLimiteMs, String palavraSecreta, int maxErros, String issuerId) {
        super(tempoLimiteMs, issuerId);
        // Normaliza a palavra secreta para minúsculas e sem acentos
        this.palavraSecreta = NormalizadorDeTexto.removerAcentos(palavraSecreta.toLowerCase());
        this.maxErros = maxErros;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPalavraSecreta() {
        return palavraSecreta;
    }

    public MessageEmbed buildGameEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(" Jogo da Forca ");
        embed.setColor(Color.CYAN);

        // --- ALTERAÇÃO 1: Lógica para exibir a palavra ---
        // Agora, caracteres que não são letras (como espaço e hífen) são revelados desde o início.
        String palavraExibida = palavraSecreta.chars()
                .mapToObj(cInt -> {
                    char c = (char) cInt;
                    if (!Character.isLetter(c)) {
                        return String.valueOf(c); // Revela espaços, hífens, etc.
                    } else if (letrasCorretas.contains(c)) {
                        return String.valueOf(c); // Revela letras corretas
                    } else {
                        return "_"; // Esconde letras não adivinhadas
                    }
                })
                .collect(Collectors.joining(" "));
        embed.addField("Palavra:", "`" + palavraExibida + "`", false);

        String letrasErradasStr = letrasErradas.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        if (letrasErradasStr.isEmpty()) {
            letrasErradasStr = "Nenhuma ainda.";
        }
        // Exibe também as palavras erradas que foram tentadas
        if (!palavrasErradas.isEmpty()) {
            letrasErradasStr += "\n**Palavras erradas:** " + String.join(", ", palavrasErradas);
        }
        embed.addField("Tentativas Erradas:", letrasErradasStr, true);

        // O total de erros agora é a soma de letras erradas e palavras erradas
        int errosAtuais = letrasErradas.size() + palavrasErradas.size();
        StringBuilder errosDisplay = new StringBuilder();
        for (int i = 0; i < maxErros; i++) {
            errosDisplay.append(i < errosAtuais ? "❌" : "⬜");
        }
        embed.addField("Erros:", errosDisplay.toString() + String.format(" (%d/%d)", errosAtuais, maxErros), true);

        double tempoEmSegundos = getTempoLimiteMs() / 1000.0;
        embed.addField("Tempo Limite:", String.format("**%.1f** segundos", tempoEmSegundos), true);

        embed.setFooter("Digite uma letra ou a palavra completa no chat para adivinhar!");

        return embed.build();
    }

    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager) {
        String tentativa = NormalizadorDeTexto.removerAcentos(event.getMessage().getContentRaw().toLowerCase());

        // --- ALTERAÇÃO 2: Lógica para processar a resposta ---
        // Decide se a tentativa é uma letra ou uma palavra inteira.
        if (tentativa.length() > 1) {
            // É uma tentativa de palavra inteira
            if (tentativa.equals(palavraSecreta)) {
                // VITÓRIA!
                gameManager.finalizarJogo(event.getChannel().getId());
                event.getChannel().sendMessage("🎉 **VITÓRIA!** " + event.getAuthor().getAsMention() + " acertou a palavra completa: `" + palavraSecreta + "`!").queue();
                return; // Encerra o processamento
            } else {
                // Palavra errada, adiciona à lista e conta como 1 erro
                palavrasErradas.add(tentativa);
            }
        } else if (tentativa.length() == 1 && Character.isLetter(tentativa.charAt(0))) {
            // É uma tentativa de letra
            char letra = tentativa.charAt(0);
            if (letrasCorretas.contains(letra) || letrasErradas.contains(letra)) {
                return; // Ignora letras repetidas
            }

            if (palavraSecreta.indexOf(letra) >= 0) {
                letrasCorretas.add(letra);
            } else {
                letrasErradas.add(letra);
            }
        } else {
            // Input inválido (ex: um número, um caractere especial), simplesmente ignora.
            return;
        }

        // Após cada tentativa, atualiza o tabuleiro do jogo
        event.getChannel().editMessageEmbedsById(messageId, buildGameEmbed()).queue();

        // Verifica condição de vitória (todas as letras foram adivinhadas)
        boolean vitoriaPorLetras = palavraSecreta.chars()
                .filter(Character::isLetter)
                .allMatch(c -> letrasCorretas.contains((char) c));
        if (vitoriaPorLetras) {
            gameManager.finalizarJogo(event.getChannel().getId());
            event.getChannel().sendMessage("🎉 **Parabéns!** " + event.getAuthor().getAsMention() + " adivinhou a última letra e vocês venceram! A palavra era `" + palavraSecreta + "`.").queue();
            return;
        }

        // Verifica condição de derrota (atingiu o número máximo de erros)
        int errosAtuais = letrasErradas.size() + palavrasErradas.size();
        if (errosAtuais >= maxErros) {
            gameManager.finalizarJogo(event.getChannel().getId());
            event.getChannel().sendMessage("💀 **Fim de jogo!** Vocês foram enforcados! A palavra era `" + palavraSecreta + "`.").queue();
        }
    }
}