package gui;

import java.util.List;

import common.data.Agent;
import common.data.Vote;

/**
 * ソケット通信で使える形に整形する
 *
 * @author nmihara
 */
public class TextConverter {

    /**
     * 名前設定用に変換
     * N$1$The Agent
     * 
     * @return ソケット通信用データ
     */
    public String name(int agentNum, String str) {
        String line = "N";
        line += "$" + agentNum;
        line += "$" + str;
        return line;
    }

    /**
     * 会話用に変換
     * 例 T$0$こんにちは。
     * 
     * @return ソケット通信用データ
     */
    public String talk(int agentNum, String str) {
        String line = "T";
        line += "$" + agentNum;
        line += "$" + str;
        return line;
    }

    /**
     * 投票結果を変換
     * 投票者->被投票者の順 V$1$2
     * 
     * @return ソケット通信用データ
     */
    public String vote(Vote vote) {
        int agentNum = vote.getAgent().getAgentIdx();
        int targetNum = vote.getTarget().getAgentIdx();
        // 送信用データを整形
        String line = "V";
        line += "$" + agentNum + "$" + targetNum;

        return line;
    }

    /**
     * 昨晩死んだエージェントを変換
     * 例: D$1$4
     * 
     * @return ソケット通信用データ
     */
    public String dead(List<Agent> deads) {
        StringBuilder line = new StringBuilder("D");
        for (Agent dead : deads) {
            line.append("$").append(dead.getAgentIdx());
        }
        return line.toString();
    }

    /**
     * 日数の送信 (これが送信されたときは次の日に進んだとみなす)
     * 
     * @return ソケット通信用データ
     */
    public String nextDay(int day) {
        String line = "DAY";
        line += "$" + day;
        return line;
    }

    /**
     * 結果の送信
     */
    public String result(String str) {
        String line = "RES";
        line += "$" + str;
        return line;
    }
}