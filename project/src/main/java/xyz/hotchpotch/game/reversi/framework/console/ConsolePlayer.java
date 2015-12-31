package xyz.hotchpotch.game.reversi.framework.console;

import xyz.hotchpotch.game.reversi.aiplayers.AIPlayerUtil;
import xyz.hotchpotch.game.reversi.core.Board;
import xyz.hotchpotch.game.reversi.core.Color;
import xyz.hotchpotch.game.reversi.core.Move;
import xyz.hotchpotch.game.reversi.core.Point;
import xyz.hotchpotch.game.reversi.core.Rule;
import xyz.hotchpotch.game.reversi.framework.GameCondition;
import xyz.hotchpotch.game.reversi.framework.Player;
import xyz.hotchpotch.util.ConsoleScanner;

/**
 * 標準入力からの入力により手を指定する {@link Player} の実装です。<br>
 * <br>
 * 動作制御のために、次のオプションパラメータを与えることができます。
 * <table border="1">
 *   <caption>指定可能なオプションパラメータ</caption>
 *   <tr><th>キー</th><th>型</th><th>内容</th><th>デフォルト値</th></tr>
 *   <tr><td>safety</td><td>boolean</td><td>ユーザがルール違反の手を指定した際に再入力を求めるか</td><td>true</td></tr>
 * </table>
 * 
 * @since 1.0.0
 * @author nmby
 */
public class ConsolePlayer implements Player {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    private final boolean safety;
    
    private final ConsoleScanner<String> sc = ConsoleScanner
            .stringBuilder("[a-h][1-8]|PASS")
            .prompt("手を選択してください（\"a1\"～\"h8\" または \"PASS\"）" + System.lineSeparator() + "> ")
            .build();
            
    /**
     * {@code ConsolePlayer} のインスタンスを生成します。<br>
     * 
     * @param color このプレーヤーの駒の色
     * @param gameCondition ゲーム実施条件
     */
    public ConsolePlayer(Color color, GameCondition gameCondition) {
        safety = AIPlayerUtil.getBooleanParameter(gameCondition, "safety").orElse(true);
    }
    
    /**
     * {@inheritDoc}
     * <br>
     * この実装は、標準入力からの入力により手を選びます。<br>
     * 他スレッドからの割り込みが検知された場合は {@code null} を返して終了します。<br>
     */
    @Override
    public Point decide(Board board, Color color, long givenMillisPerTurn, long remainingMillisInGame) {
        boolean isFirst = true;
        Point point;
        
        do {
            if (isFirst) {
                isFirst = false;
            } else {
                System.out.print("不正な手です。");
            }
            String strPoint = sc.get();
            point = "PASS".equals(strPoint) ? null : Point.of(strPoint);
            
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
        } while (safety && !Rule.canApply(board, Move.of(color, point)));
        
        return point;
    }
}
