package net.yeyito.connections;

import com.beust.jcommander.internal.Nullable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.yeyito.Main;
import net.yeyito.roblox.ItemManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DiscordBot extends ListenerAdapter {
    JDA jda;
    Commands commands = new Commands();
    public List<TextChannel> registeredTextChannels = new ArrayList<TextChannel>();
    public static final String High_Role = "<@&1086796391361298472>";
    public static final String Extreme_Role = "<@&1086796484596469880>";
    public static final String Ludicrous_Role = "<@&1086796519048482996>";
    public static boolean registered = false;
    public DiscordBot(String token, @Nullable Activity activity) {
        this.jda = JDABuilder.createDefault(token).setActivity(activity)
                .setEventPassthrough(true)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        this.jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(";")) {
            String[] arguments = event.getMessage().getContentRaw().split(" ");
            String command = arguments[0];

            switch (command) {
                case ";register_channel" -> {
                    if (event.getAuthor().getId().equals("310543961825738754")) {
                        commands.register_channel(event, arguments);
                    }
                }
            }
        }
    }

    @Override
    public void onGenericMessage(@NotNull GenericMessageEvent event) {
        // Info about the message but not the message itself
        super.onGenericMessage(event);
    }

    public void sendMessageOnRegisteredChannels(String message,int maxSecondsBeforeQueue) {
        TimeUnit timeUnit = TimeUnit.SECONDS;
        if (maxSecondsBeforeQueue == 0) {timeUnit = TimeUnit.MICROSECONDS; maxSecondsBeforeQueue = 1;}

        for (TextChannel textChannel : registeredTextChannels) {
            textChannel.sendMessage(message).queueAfter(new Random().nextInt(0, maxSecondsBeforeQueue), timeUnit);
        }
    }
}
class Commands {
    @Deprecated public void register_channel(MessageReceivedEvent event,String[] args) {
        // Deprecated, use webhooks instead.
        if (args.length > 1) {event.getChannel().sendMessage("Invalid syntax!").queue();}
        else {
            if (!DiscordBot.registered) {
                DiscordBot.registered = true;
                Main.discordBot.registeredTextChannels.add(event.getMessage().getChannel().asTextChannel());
                event.getMessage().getChannel().sendMessage(event.getMessage().getChannel().asTextChannel().getName() + " is now visible to Yeyito!").queue();
            }
        }
    }
}
