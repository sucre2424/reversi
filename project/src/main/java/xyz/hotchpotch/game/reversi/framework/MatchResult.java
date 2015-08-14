package xyz.hotchpotch.game.reversi.framework;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.framework.Match.Entrant;

/**
 * マッチの結果を表す不変クラスです。<br>
 * 
 * @author nmby
 */
public class MatchResult implements Result<Match> {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    /**
     * マッチ条件とゲーム結果からマッチ結果を生成します。<br>
     * 
     * @param matchCondition マッチ条件
     * @param gameResults ゲーム結果が格納された {@code Map}
     * @return マッチ結果
     * @throws NullPointerException {@code matchCondition}、{@code gameResults} のいずれかが {@code null} の場合
     */
    public static MatchResult of(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        return new MatchResult(
                Objects.requireNonNull(matchCondition),
                Objects.requireNonNull(gameResults));
    }
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public final MatchCondition matchCondition;
    public final Entrant winner;
    private final String description;
    
    private MatchResult(MatchCondition matchCondition, Map<Entrant, List<GameResult>> gameResults) {
        this.matchCondition = matchCondition;
        
        int winA = 0;
        int winB = 0;
        int draw = 0;
        for (Entrant entrant : Entrant.values()) {
            List<GameResult> results = gameResults.get(entrant);
            for (GameResult result : results) {
                if ((entrant == Entrant.A && result.winner == Color.BLACK)
                        || (entrant == Entrant.B && result.winner == Color.WHITE)) {
                    winA++;
                } else if ((entrant == Entrant.A && result.winner == Color.WHITE)
                        || (entrant == Entrant.B && result.winner == Color.BLACK)) {
                    winB++;
                } else {
                    draw++;
                }
            }
        }
        if (winB < winA) {
            winner = Entrant.A;
        } else if (winA < winB) {
            winner = Entrant.B;
        } else {
            winner = null;
        }
        
        description = String.format("%s %sの勝ち:%d, %sの勝ち:%d, 引き分け:%d",
                winner == null ? "引き分けです。" : String.format("%s:%s の勝ちです。",
                        winner, matchCondition.playerClasses.get(winner).getSimpleName()),
                Entrant.A, winA, Entrant.B, winB, draw);
    }
    
    /**
     * このマッチ結果の文字列表現を返します。<br>
     * 
     * @return このマッチ結果の文字列表現
     */
    @Override
    public String toString() {
        return description;
    }
}