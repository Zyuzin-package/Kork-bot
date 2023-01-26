package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class App extends ListenerAdapter {
    private static String guildId = "";
    private final Emoji korkovik = Emoji.fromFormatted("<:korkovik:1063009366292308008>");
    private final Emoji alex = Emoji.fromFormatted("<:alex:1010892349150335027>");
    private final Emoji ya = Emoji.fromFormatted("<:ya:1010892345564205096>");
    private final Emoji mnePohui = Emoji.fromFormatted("<:19:1061190142250983444>");
    private final Emoji seven = Emoji.fromUnicode("7️⃣");
    private final Emoji eight = Emoji.fromUnicode("8️⃣");
    static Map<String, Integer> users = new HashMap<>();// Список всех пользователей на сервере
    static Map<String, Integer> usersWhoBet = new HashMap<>();// Список пользователей участвующих в пари
    static String pariId;
    static boolean betStatus = false;// существует ли текущее пари или нет
    static String userMessageRepeat = "";// айди пользователя, который послдений отправлял сообщение, для проверки на спам

    //https://jda.wiki/using-jda/interactions/
    public static void main(String[] args) throws InterruptedException {
        StringBuilder token = new StringBuilder();
        StringBuilder guildId = new StringBuilder();

        try (FileReader reader = new FileReader("D:/Java/Project/Java Project/KorkBot2.0/KorkBotV2/token.properties")) {
            int c;
            while ((c = reader.read()) != -1) {
                token.append((char) c);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        try (FileReader reader = new FileReader("D:/Java/Project/Java Project/KorkBot2.0/KorkBotV2/guildid.properties")) {
            int c;
            while ((c = reader.read()) != -1) {
                guildId.append((char) c);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES
        );

        JDA jda = JDABuilder.createLight(String.valueOf(token), intents)
                .addEventListeners(new App())
                .setActivity(Activity.watching("deadinside✓ emo✓ drain✓ epileptic✓ paranoid✓ toxic✓ bipolar✓ depressed✓"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("status", "Вывод статуса"),
                Commands.slash("start_betting", "Сделать ставку")
                        .addOption(OptionType.STRING, "message", "На что ставим", true, false),
                Commands.slash("bet", "Поддержать ставку")
                        .addOption(OptionType.STRING, "bet_count", "Количество корковиков", true, false),
                Commands.slash("rules", "Вывести правила"),
                Commands.slash("save", "Сохранить статистику"),
                Commands.slash("load", "Загрузить статистику")
        );

        jda.awaitReady();

        Guild guild = jda.getGuildById(String.valueOf(guildId));

        assert guild != null;
        guild.updateCommands().addCommands(Commands.context(Command.Type.USER, "Количество корковиков")).queue();
        try (FileReader reader = new FileReader("src/main/resources/folder/korkovikFolder.txt")) {
            // читаем посимвольно
            readData(reader);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        commands.queue();
    }

    /**
     * Проверка на количество корковиков
     *
     * @param event
     */
    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        switch (event.getName()) {
            case ("Количество корковиков") -> {
                event.reply("Количество корковиков у " + event.getTarget().getName() + ": " + users.get(event.getTarget().getId()) + " " + korkovik.getFormatted()).queue();
            }
        }

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("done")) {
            if (!betStatus) {
                event.reply(event.getUser().getName() + ", пари закончилось").queue();
                event.getInteraction().editButton(event.getButton().withId("done").asDisabled()).queue();
                return;
            }

            if (usersWhoBet.isEmpty()) {
                event.reply("Никто не поддержал пари").queue();
                return;
            }

            EmbedBuilder test = new EmbedBuilder();
            test.setColor(new Color(248, 32, 32));
            test.setDescription("Пари сыграло!\n");

            for (Map.Entry<String, Integer> entry : usersWhoBet.entrySet()) {
                int beforeCount = users.get(entry.getKey());
                users.put(entry.getKey(), beforeCount + entry.getValue());
                test.appendDescription(event.getJDA().getUserById(entry.getKey()).getName() + "выиграл "
                        + (entry.getValue() * 2) + " " + korkovik.getFormatted() + "\nТеперь у него "
                        + users.get(entry.getKey()) + korkovik.getFormatted() + "\n");
            }

            event.replyEmbeds(test.build()).queue();
            event.getInteraction().editButton(event.getButton().withId("done").asDisabled()).queue();

            betStatus = false;
            usersWhoBet = new HashMap<>();
        } else if (event.getComponentId().equals("loose")) {
            if (!betStatus) {
                event.getChannel().sendMessage(event.getUser().getName() + ", пари закончилось").queue();
                event.getInteraction().editButton(event.getButton().withId("loose").asDisabled()).queue();
                return;
            }
            if (usersWhoBet.isEmpty()) {
                event.reply("Никто не поддержал пари").queue();
                return;
            }

            EmbedBuilder test = new EmbedBuilder();
            test.setColor(new Color(248, 32, 32));
            test.setDescription("Пари не сыграло!\n");

            for (Map.Entry<String, Integer> entry : usersWhoBet.entrySet()) {

                int beforeCount = users.get(entry.getKey());
                users.put(entry.getKey(), beforeCount - entry.getValue());

                test.appendDescription(event.getJDA().getUserById(entry.getKey()).getName() + "проиграл "
                        + entry.getValue() + " " + korkovik.getFormatted() + "\nТеперь у него "
                        + users.get(entry.getKey()) + korkovik.getFormatted() + "\n");
            }

            event.replyEmbeds(test.build()).queue();
            event.getInteraction().editButton(event.getButton().withId("loose").asDisabled()).queue();

            betStatus = false;
            usersWhoBet = new HashMap<>();
        } else if (event.getComponentId().equals("cancel")) {
            if (!betStatus) {
                event.getChannel().sendMessage(event.getUser().getName() + ", пари закончилось").queue();
                event.getInteraction().editButton(event.getButton().withId("cancel").asDisabled()).queue();
                return;
            }

            event.reply("Пари отменено").queue();
            event.getInteraction().editButton(event.getButton().withId("cancel").asDisabled()).queue();

            betStatus = false;
            usersWhoBet = new HashMap<>();
        }
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case ("start_betting") -> {
                if (!event.getOption("message").getAsString().equals("")) {
                    System.out.println("Message: " + event.getOption("message").getAsString());

                    EmbedBuilder test = new EmbedBuilder();
                    test.setColor(new Color(248, 32, 32));
                    test.setDescription("Пользователь : "
                            + event.getUser().getName() + "\nЗаключил пари: "
                            + Objects.requireNonNull(event.getOption("message")).getAsString());

                    MessageEmbed me = test.build();
                    event.replyEmbeds(me).addActionRow(
                                    Button.success("done", "Пари сыграло"),
                                    Button.primary("loose", "Пари проиграло"),
                                    Button.danger("cancel", "Пари отменено"))
                            .queue();


                    pariId = event.getChannel().getLatestMessageId();
                    System.out.println("id: " + pariId);
                    betStatus = true;
                } else {
                    System.out.println("Error!");
                }
            }
            case ("bet") -> {
                if (betStatus) {
                    String userId = event.getUser().getId();
                    if (usersWhoBet.get(userId) == null) {
                        int betCount = event.getOption("bet_count").getAsInt();

                        if (users.get(userId) < betCount) {
                            event.reply("У пользователя " + event.getUser().getName() + " не хватает корковиков " + ya.getFormatted()).queue();
                            return;
                        }

                        int tempCount = users.get(event.getUser().getId());

                        usersWhoBet.put(userId, betCount);

                        event.reply(event.getUser().getName() + " поставил " + betCount + " " + korkovik.getFormatted() + "\nУ него осталось: " + (tempCount - betCount) + korkovik.getFormatted()).queue();
                    } else {
                        event.reply(event.getUser().getName() + " ты уже сделал ставку " + ya.getFormatted()).queue();
                    }
                } else {
                    event.reply("Никто не заключал пари " + alex.getFormatted()).queue();
                }
            }
            case ("rules") -> {
                EmbedBuilder test = new EmbedBuilder();
                test.setColor(new Color(99, 255, 0));
                test.setDescription("Команда start_betting создает пари\n" +
                        "Далее, необходимо что бы  несколько пользователей ввели команду bet указав количество корковиков\n" +
                        "Как пари приобретет новый статус, необходимо выбрать одну из кнопок на сообщение после команды start_betting:\n" +
                        "-Пари сыграло - количество корковиков у всех поставивших умножается вдвое\n" +
                        "-Пари проиграло - количество корковиков у всех поставивших вычитается из суммы корковиков пользователя\n" +
                        "-Пари отменено - количество корковиков у всех поставивших пользователей не изменилось\n" +
                        "Корковики начислаются в количестве 7-8 штук, когда пользователь пишет сообщение в чат. Абузить не пытайтесь - стоит хитрая защита\n" +
                        "Что бы узнать количество корковиков, необходимо кликнуть пкм на пользователя, выбрать вкладку приложения и после - количество корковиков");

                MessageEmbed me = test.build();
                event.replyEmbeds(me).queue();
            }
            case ("save") -> {
                try (FileWriter writer = new FileWriter("src/main/resources/folder/korkovikFolder.txt", false)) {

                    for (Map.Entry<String, Integer> entry : users.entrySet()) {
                        writer.write(entry.getKey() + "|" + entry.getValue() + "\n");
                    }

                    event.reply("Данные о количестве корковиков сохранены").queue();
                } catch (IOException ex) {
                    event.reply("Ошибка! Данные о количестве корковиков не сохранены").queue();
                    System.out.println(ex.getMessage());
                }

            }
            case ("load") -> {
                try (FileReader reader = new FileReader("src/main/resources/folder/korkovikFolder.txt")) {
                    readData(reader);
                    event.reply("Данные о количестве корковиков загружены").queue();
                } catch (IOException ex) {
                    event.reply("Ошибка! Данные о количестве корковиков не загружены").queue();
                    System.out.println(ex.getMessage());
                }
            }
        }

    }

    private static void readData(FileReader reader) throws IOException {
        int c;
        String temp = "";
        String user = "";
        String count = "";
        List<String> list = new ArrayList<>();
        while ((c = reader.read()) != -1) {
            if (String.valueOf((char) c).equals("|")) {
                user = temp;
                list.add(user);
                temp = "";
                continue;
            }
            if (String.valueOf((char) c).equals("\n")) {
                count = temp;
                list.add(count);
                temp = "";
                continue;
            }
            temp = temp + (char) c;
        }
        for (int i = 0; i <= list.size() - 1; i++) {
            users.put(list.get(i), Integer.valueOf(list.get(i + 1)));
            i++;
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals("1010713967993299006")) {
            if (!event.getAuthor().getId().equals(userMessageRepeat)) {
                int b = new Random().nextInt(100);
                System.out.println(b);
                if (b <= 15) {
                    int temp = (int) (Math.random() * (9 - 7)) + 7;
                    users.put(event.getAuthor().getId(), users.get(event.getAuthor().getId()) + temp);
                    switch (temp) {
                        case (7) -> {
                            event.getMessage().addReaction(seven).queue();
                        }
                        case (8) -> {
                            event.getMessage().addReaction(eight).queue();
                        }
                    }
                    event.getMessage().addReaction(korkovik).queue();
                    event.getMessage().addReaction(mnePohui).queue();

                    userMessageRepeat = event.getAuthor().getId();
                }
            } else {
                System.out.println("Repeat!");
            }
        }
    }
}
