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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DiscordBot extends ListenerAdapter {
    JDA jda;
    List<TextChannel> registeredTextChannels = new ArrayList<TextChannel>();

    public DiscordBot(String token, @Nullable Activity activity) {
        this.jda = JDABuilder.createDefault(token).setActivity(activity)
                .setEventPassthrough(true)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        this.jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals(";register_channel")) {
            registeredTextChannels.add(event.getMessage().getChannel().asTextChannel());
            event.getMessage().getChannel().sendMessage(event.getMessage().getChannel().asTextChannel().getName() + " is now visible to Yeyito!").queue();
        }
    }

    @Override
    public void onGenericMessage(@NotNull GenericMessageEvent event) {
        // Info about the message but not the message itself
        super.onGenericMessage(event);
    }

    public void sendMessageOnRegisteredChannel(String channelName, String message,int maxSecondsBeforeQueue) {
        for (TextChannel textChannel : registeredTextChannels) {
            if (textChannel.getName().equals(channelName)) {
                textChannel.sendMessage(message).queueAfter(new Random().nextInt(0,maxSecondsBeforeQueue), TimeUnit.SECONDS);
            }
        }
    }
}
