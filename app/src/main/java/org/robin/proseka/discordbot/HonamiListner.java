package org.robin.proseka.discordbot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HonamiListner extends ListenerAdapter 
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (content.equals("こんにちは"))
        {
            MessageChannelUnion channel = event.getChannel();
            channel.sendMessage("こんにちは！" + event.getAuthor().getAsMention() + " さん！").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        if (event.getUser().isBot()) return; // Botに対しては返答処理を行わないこと (Bot同士のメッセージのやり取りで無限ループがあり得るため)
        TextChannel channel = event.getGuild().getSystemChannel(); // そのサーバーのシステムチャンネル (ウェルカムメッセージが出るチャンネル) を取得
        String message = event.getUser().getAsMention() + " さん！いらっしゃい！ゆっくりしていってね";
        if (channel != null) 
            channel.sendMessage(message).queue(); // メッセージを送信 (非同期処理なのでqueueを呼ぶ必要あり)
        else
            System.out.println("[ProsekaDiscordBot] System Channel is null");
    }
}