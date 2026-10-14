package org.robin.proseka.discordbot;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.hash.HashingOutputStream;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HonamiListner extends ListenerAdapter 
{
    private static final Pattern DICE_PATTERN = Pattern.compile("(-?[0-9]+)[dD](-?[0-9]+)");
    private JDA api = null;

    public HonamiListner(JDA api)
    {
        this.api = api;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        // We don't want to respond to other bot accounts, including ourself
        Message message = event.getMessage();
        String content = message.getContentRaw();
        Matcher dice = DICE_PATTERN.matcher(content.trim());
        if (dice.matches())
        {
            //振り出すサイコロの個数
            int count;
            //サイコロの面数
            int sides;
            try
            {
                //振り出すサイコロの個数をダイスグループ1と定義
                count = Integer.parseInt(dice.group(1));
                //振り出すサイコロの面数をダイスグループ2と定義
                sides = Integer.parseInt(dice.group(2));
            }
            catch (NumberFormatException e)
            {
                //面数がint型の最大値より大きいか、下記3つ以外のダイスロールエラーの時に出力されるエラーメッセージ。
                event.getChannel().sendMessage("ごめんね、その条件でサイコロ振れないみたい…").queue();
                return;
            }
            if (count < 1)
            {
                //1回未満の回数でダイスロールをしようとしたときに出力されるエラーメッセージ。
                event.getChannel().sendMessage("1回はサイコロ振らないとできないんだ…").queue();
                return;
            }
            if (count > 100)
            {
                //100回より多くの回数でダイスロールをしようとしたときに出力されるエラーメッセージ
                event.getChannel().sendMessage("100個より多くはサイコロ振れないんだ…").queue();
                return;
            }
            if (sides < 1)
            {
                //面数が1未満でダイスロールをしようとしたときに出力されるエラーメッセージ。
                event.getChannel().sendMessage("1より小さい面数はできないんだ…").queue();
                return;
            }

            //ダイスロール成功時に出力するメッセージ「D → [] 合計: 」
            StringBuilder result = new StringBuilder(count + "D" + sides + " → [");
            //long形で合計値を出力するための準備。
            long total = 0;
            //count分のループ処理。1回ごとに終了時にインクリメントで1ずつ増えて、count数まで行う。
            for (int i = 0; i < count; i++)
            {
                //指示された数の出目をランダムに生成し、桁あふれしないためにlong型に変換
                long roll = ThreadLocalRandom.current().nextLong(1, (long) sides + 1);
                if (i > 0) result.append(", ");
                result.append(roll);
                //出た値のtotalも計算する。
                total += roll;
            }
            result.append("] 合計: ").append(total);
            event.getChannel().sendMessage(result.toString()).queue();
            return;
        }
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (isMentioned(message.getMentions().getUsers()) && content.contains("こんにちは"))
        {
            MessageChannelUnion channel = event.getChannel();
            channel.sendMessage("こんにちは！" + event.getAuthor().getAsMention() + " さん！").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
        }
        //「わんわん」がテキストに含まれるなら
        if (content.contains("わんわん"))
        {
            //対象のチャンネルに対してイベントを起こす。
            MessageChannelUnion channel = event.getChannel();
            //「わんだほい！！」のテキストメッセージを返す。
            channel.sendMessage("わんだほい！！").queue(); // メッセージを送信 (非同期処理なのでqueueを呼ぶ必要あり)
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

    private boolean isMentioned(List<User> users) 
    {
        return users.contains(api.getSelfUser());
    } 
}
