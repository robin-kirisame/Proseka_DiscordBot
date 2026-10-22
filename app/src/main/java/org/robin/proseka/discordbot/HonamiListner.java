package org.robin.proseka.discordbot;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        Matcher dice = DICE_PATTERN.matcher(content.replaceAll("<@!?" + api.getSelfUser().getId() + ">", "").trim());
        if (message.getMentions().isMentioned(api.getSelfUser()) && dice.matches())
        {
            //振り出すサイコロの個数
            int count = 0;
            //サイコロの面数
            int sides = 0 ;
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
                event.getChannel().sendMessage(
                    event.getAuthor().getAsMention()+"さん。ごめんね、その条件でサイコロ振れないみたい…"
                ).queue();
                return;
            }
            if (count < 1)
            {
                //1回未満の回数でダイスロールをしようとしたときに出力されるエラーメッセージ。
                event.getChannel().sendMessage(
                    event.getAuthor().getAsMention()+"さん。1回はサイコロ振らないといけないんだ…"
                    ).queue();
                return;
            }
            if (count > 100)
            {
                //100回より多くの回数でダイスロールをしようとしたときに出力されるエラーメッセージ
                event.getChannel().sendMessage(
                    event.getAuthor().getAsMention()+"さん。100個より多くはサイコロ振れないんだ…"
                ).queue();
                return;
            }
            if (sides < 1)
            {
                //面数が1未満でダイスロールをしようとしたときに出力されるエラーメッセージ。
                event.getChannel().sendMessage(
                   event.getAuthor().getAsMention()+ "さん。1より小さい面数は振り出せないんだ…"
                ).queue();
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
            event.getChannel().sendMessage(
                event.getAuthor().getAsMention() +" さん。"+ " " +result.toString()
            ).queue();
            return;
        }
        // getContentRaw() is an atomic getter
        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
        if (isMentioned(message.getMentions().getUsers()) && content.contains("おはよう")
            ||content.contains("こんにちは")
            ||content.contains("こんばんわ")
        )
        {
            //現在時刻を取得する
            int hours = message.getTimeCreated()
                //日本時間で設定
                .atZoneSameInstant(ZoneId.of("Asia/Tokyo"))
                .getHour();

            String response_hello;

            boolean hello = true;

            if (hours >=5 && hours < 11)
            {
                //「おはよう！」と返す。
               response_hello = "おはよう！";

                //helloにtrue で返す。
                hello = true;
            }

            else if (hours >=11 && hours < 16)
            {
                //「こんにちは！」と返す。
               response_hello = "こんにちは！";

                //helloにtrue で返す。
                hello = true;
            }

            else if (hours >=16 && hours < 23)
            {
                //「こんばんわ！」と返す。
               response_hello = "こんばんわ！";

                //helloにtrue で返す。
                hello = true;
            }
            else
            {
                //「今から食べるのはよくないんじゃないかな？」と返す
                response_hello = "えっと…寝たほうがいいんじゃないかな？" ;

                //false で返す。
                hello = false;

                 //falseの時にはメッセージを取得したチャンネルで
                event.getChannel().sendMessage(
                    //メンションをつけて
                    event.getAuthor().getAsMention() + " さん！"
                    //「えっと…寝たほうがいいんじゃないかな？」を返す。
                    +  response_hello).queue();
            }
            //helloがtrueの時
            if (hello)
            {
            MessageChannelUnion channel = event.getChannel();
            channel.sendMessage(response_hello + event.getAuthor().getAsMention() + " さん！").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
            }
        }
        //「わんわん」がテキストに含まれるなら
        if (content.contains("わんわん～"))
        {
            //対象のチャンネルに対してイベントを起こす。
            MessageChannelUnion channel = event.getChannel();
            //「わんだほい！！」のテキストメッセージを返す。
            channel.sendMessage("わんだほ～い！！").queue(); // メッセージを送信 (非同期処理なのでqueueを呼ぶ必要あり)
        }

        //おやつダイス「今日のおやつは？」とメンションで聞かれたときに返す。
        if (message.getMentions().isMentioned(api.getSelfUser()) && content.contains("今日のおやつは？"))
         {
            //現在時刻を取得する
            int hour = message.getTimeCreated()
                //日本時間で設定
                .atZoneSameInstant(ZoneId.of("Asia/Tokyo"))
                .getHour();

            String response;

            //true の時に処理を行う。
            boolean diceroll = true;
            
            //午前5時~午前11時59分までの間
            if (hour >=5 && hour < 12)
            {
                //「早い時間だけど、ちょっとならいいよね。」と返す。
                response = "早い時間だけど、ちょっとならいいよね。";

                //true で返す。
                diceroll = true;
            }

            //午後0時~午後3時59分までの間
            else if (hour >=12 && hour < 16)
            {
                //「何を食べようかな…？」と返す
                response = "何を食べようかな…？";

                //true で返す。
                diceroll = true;
            }

            //午後4時~午後10時59分までの間
            else if (hour >=16 && hour < 23)
            {
                //「晩御飯もあるから、少しだけだよ。」と返す
                response = "晩御飯もあるから、少しだけだよ。";

                //true で返す。
                diceroll = true;
            }
            else
            {
                //「今から食べるのはよくないんじゃないかな？」と返す
                response = "今から食べるのはよくないんじゃないかな？" ;

                //false で返す。
                diceroll = false;

                //falseの時にはメッセージを取得したチャンネルで
                event.getChannel().sendMessage(
                    //メンションをつけて
                    event.getAuthor().getAsMention() 
                    //「今から食べるのはよくないんじゃないかな？」を返す。
                    + response).queue();
            }
 
            //dicerollがtrueの時
            if (diceroll)
             {
                //long型で1~20のダイスロールを行う。
                long roll = ThreadLocalRandom.current().nextLong(1,21);

                //ダイスロールの出目が15以下の時は
                String responsed ;
                if (roll <= 15)
                {
                    //「アップルパイ」と返す。
                    responsed = "アップルパイ";
                }

                //ダイスロールの出目が16の時は
                else if (roll == 16)
                {
                    //「チョコレート」と返す。
                    responsed = "チョコレート";
                }

                //ダイスロールの出目が17の時は
                else if (roll == 17)
                {
                    //「ポテトチップス」と返す。
                    responsed = "ポテトチップス";
                }

                //ダイスロールの出目が18の時は
                else if (roll == 18)
                {
                    //「ショートケーキ」と返す。
                    responsed = "ショートケーキ";
                }

                //ダイスロールの出目が19の時は
                else if (roll == 19)
                {
                    //「シュークリーム」と返す。
                    responsed = "シュークリーム";
                }

                //ダイスロールの出目が20の時は
                else 
                {
                    //「プリン」と返す。
                    responsed = "プリン";
                }

                //ダイスロールで取得した結果をメッセージを取得したチャンネルで
                event.getChannel().sendMessage(

                    //メンションをつけて
                    event.getAuthor().getAsMention() + " さん！"

                    //diceroll = true　の時のメッセージと
                    + response

                    //ダイスロールの出目の結果を返す。
                    + "今日は" + responsed + "がいいんじゃないかな？"
                ).queue();
                return;
            }
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
