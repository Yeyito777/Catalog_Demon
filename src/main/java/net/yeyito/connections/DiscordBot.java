package net.yeyito.connections;

import com.beust.jcommander.internal.Nullable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class DiscordBot extends ListenerAdapter {
    JDA jda;

    public DiscordBot(String token, @Nullable Activity activity) {
        this.jda = JDABuilder.createDefault(token).setActivity(activity)
                .setEventPassthrough(true)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        this.jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getContentRaw().charAt(0) == ';') {
            if (message.getContentRaw().equals(";hello")) {message.getChannel().sendMessage("hi :3").queue();}
            if (message.getContentRaw().equals(";thought's on black lives matter?")) {message.getChannel().sendMessage("YOU NI---   CE PERSON!").queue();}
            if (message.getContentRaw().equals(";am I gay?")) {
                if (new Random().nextInt(0,2) == 0) {
                    message.getChannel().sendMessage("You're straight!").queue();
                } else {
                    message.getChannel().sendMessage("We all know you're the gayest most submissive femboy twink out there!").queue();
                }
            }
        }
    }

    @Override
    public void onGenericMessage(@NotNull GenericMessageEvent event) {
        // Info about the message but not the message itself
        super.onGenericMessage(event);
    }
}
