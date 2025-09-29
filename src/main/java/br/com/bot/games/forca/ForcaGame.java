package br.com.bot.games.forca;

import br.com.bot.core.ConfigManager;
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

/**
 * Representa o estado e a lógica de um Jogo da Forca.
 * <p>
 * Esta classe gerencia a palavra secreta, as tentativas corretas e incorretas (tanto de letras
 * quanto de palavras inteiras), e é responsável por construir a representação visual
 * do jogo (o "tabuleiro") através de um MessageEmbed.
 *
 * @author Lucas
 */
public class ForcaGame extends Game {
    private final String palavraSecreta;
    private final int maxErros;
    private final Set<Character> letrasCorretas = new HashSet<>();
    private final Set<Character> letrasErradas = new HashSet<>();
    private final Set<String> palavrasErradas = new HashSet<>();
    private String messageId;

    /**
     * Constrói uma nova instância do Jogo da Forca.
     * A palavra secreta é normalizada (convertida para minúsculas e sem acentos) no momento da criação.
     *
     * @param tempoLimiteMs O tempo limite total para o jogo em milissegundos.
     * @param palavraSecreta A palavra que os jogadores devem adivinhar.
     * @param maxErros O número máximo de tentativas incorretas permitidas.
     * @param issuerId O ID do usuário que iniciou o jogo.
     */
    public ForcaGame(long tempoLimiteMs, String palavraSecreta, int maxErros, String issuerId) {
        super(tempoLimiteMs, issuerId);
        this.palavraSecreta = NormalizadorDeTexto.removerAcentos(palavraSecreta.toLowerCase());
        this.maxErros = maxErros;
    }

    /**
     * Define o ID da mensagem do Discord que contém o tabuleiro do jogo.
     * Essencial para que o bot possa editar a mensagem e atualizar o estado do jogo.
     * @param messageId O ID da mensagem a ser editada.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Retorna a palavra secreta e normalizada do jogo.
     * @return A palavra secreta.
     */
    public String getPalavraSecreta() {
        return palavraSecreta;
    }

    /**
     * Constrói e retorna a representação visual do estado atual do jogo em um MessageEmbed.
     * Inclui a palavra com as letras ocultas, tentativas erradas, contador de erros e tempo limite.
     * @return Um {@link MessageEmbed} representando o tabuleiro do jogo.
     */
    public MessageEmbed buildGameEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(" Jogo da Forca ");
        embed.setColor(Color.CYAN);

        // Constrói a exibição da palavra (ex: G U A R D A - C H U V A)
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
        if (!palavrasErradas.isEmpty()) {
            letrasErradasStr += "\n**Palavras erradas:** " + String.join(", ", palavrasErradas);
        }
        embed.addField("Tentativas Erradas:", letrasErradasStr, true);

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

    /**
     * {@inheritDoc}
     * <p>
     * Processa a tentativa de um jogador. Verifica se a tentativa é uma letra ou uma palavra inteira,
     * atualiza o estado do jogo (letras corretas/erradas) e edita a mensagem do tabuleiro.
     * Também verifica as condições de vitória ou derrota após cada jogada válida.
     */
    @Override
    protected void processarRespostaDoJogo(MessageReceivedEvent event, GameManager gameManager, ConfigManager configManager) {
        String tentativa = NormalizadorDeTexto.removerAcentos(event.getMessage().getContentRaw().toLowerCase());

        if (tentativa.isEmpty()) return;

        // Lógica para tentativa de palavra inteira
        if (tentativa.length() > 1) {
            if (tentativa.equals(palavraSecreta)) {
                gameManager.finalizarJogo(event.getChannel().getId());
                event.getChannel().sendMessage("🎉 **VITÓRIA!** " + event.getAuthor().getAsMention() + " acertou a palavra completa: `" + palavraSecreta + "`!").queue();
                return;
            } else {
                palavrasErradas.add(tentativa);
            }
            // Lógica para tentativa de letra
        } else if (Character.isLetter(tentativa.charAt(0))) {
            char letra = tentativa.charAt(0);
            if (letrasCorretas.contains(letra) || letrasErradas.contains(letra)) {
                return;
            }
            if (palavraSecreta.indexOf(letra) >= 0) {
                letrasCorretas.add(letra);
            } else {
                letrasErradas.add(letra);
            }
        } else {
            return; // Ignora inputs que não são letras ou palavras
        }

        // Atualiza o tabuleiro e verifica condições de fim de jogo
        event.getChannel().editMessageEmbedsById(messageId, buildGameEmbed()).queue();

        boolean vitoriaPorLetras = palavraSecreta.chars()
                .filter(Character::isLetter)
                .allMatch(c -> letrasCorretas.contains((char) c));
        if (vitoriaPorLetras) {
            gameManager.finalizarJogo(event.getChannel().getId());
            event.getChannel().sendMessage("🎉 **Parabéns!** " + event.getAuthor().getAsMention() + " adivinhou a última letra e vocês venceram! A palavra era `" + palavraSecreta + "`.").queue();
            return;
        }

        int errosAtuais = letrasErradas.size() + palavrasErradas.size();
        if (errosAtuais >= maxErros) {
            gameManager.finalizarJogo(event.getChannel().getId());
            event.getChannel().sendMessage("💀 **Fim de jogo!** Vocês foram enforcados! A palavra era `" + palavraSecreta + "`.").queue();
        }
    }
}