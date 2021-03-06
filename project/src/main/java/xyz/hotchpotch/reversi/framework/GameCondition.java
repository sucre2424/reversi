package xyz.hotchpotch.reversi.framework;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.hotchpotch.reversi.core.Color;

/**
 * ゲームの実施条件を表す不変クラスです。<br>
 * 
 * @since 2.0.0
 * @author nmby
 */
public class GameCondition implements Condition<Game>, Serializable {
    
    // [static members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    /*package*/ static final String KEY_PLAYER_BLACK = "player.black";
    /*package*/ static final String KEY_PLAYER_WHITE = "player.white";
    /*package*/ static final String KEY_MILLIS_PER_TURN = "givenMillisPerTurn";
    /*package*/ static final String KEY_MILLIS_IN_GAME = "givenMillisInGame";
    
    private static final long serialVersionUID = 1L;
    
    /**
     * {@link GameCondition} のシリアライゼーションプロキシです。<br>
     * 
     * @serial include
     * @author nmby
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** @serial 全パラメータが格納されたマップ */
        private final Map<String, String> params;
        
        private SerializationProxy(GameCondition gameCondition) {
            params = gameCondition.params;
        }
        
        /**
         * 復元された {@code SerializationProxy} に対応する {@link GameCondition} オブジェクトを返します。<br>
         * 
         * @serialData 復元された {@link #params} を用いて {@link GameCondition} オブジェクトを構築して返します。<br>
         *             {@link GameCondition} オブジェクト構築の過程で例外が発生した場合は例外をスローして復元を中止します。
         * @return 復元された {@code SerializationProxy} オブジェクトに対応する {@code GameCondition} オブジェクト
         * @throws ObjectStreamException {@link GameCondition} オブジェクト構築の過程で例外が発生した場合
         */
        private Object readResolve() throws ObjectStreamException {
            try {
                return of(params);
            } catch (RuntimeException e) {
                throw new InvalidObjectException(
                        String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }
    
    /**
     * 個々の必須パラメータを指定してゲーム実施条件を生成します。<br>
     * 
     * @param playerBlack 黒プレーヤーのクラス
     * @param playerWhite 白プレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @return ゲーム実施条件
     * @throws NullPointerException {@code playerBlack}、{@code playerWhite} のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame} のいずれかが正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame) {
            
        return of(playerBlack, playerWhite, givenMillisPerTurn, givenMillisInGame, new HashMap<>());
    }
    
    /**
     * 個々の必須パラメータと追加のパラメータを指定してゲーム実施条件を生成します。<br>
     * {@code params} に必須パラメータが含まれる場合は、個別に引数で指定した値が優先されます。<br>
     * 
     * @param playerBlack 黒プレーヤーのクラス
     * @param playerWhite 白プレーヤーのクラス
     * @param givenMillisPerTurn 一手あたりの制限時間（ミリ秒）
     * @param givenMillisInGame ゲーム全体での持ち時間（ミリ秒）
     * @param params 追加のパラメータが格納された {@code Map}
     * @return ゲーム実施条件
     * @throws NullPointerException {@code playerBlack}、{@code playerWhite}、{@code params}
     *                              のいずれかが {@code null} の場合
     * @throws IllegalArgumentException {@code givenMillisPerTurn}、{@code givenMillisInGame} のいずれかが正の整数でない場合
     */
    public static GameCondition of(
            Class<? extends Player> playerBlack,
            Class<? extends Player> playerWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Map<String, String> params) {
            
        Objects.requireNonNull(playerBlack);
        Objects.requireNonNull(playerWhite);
        Objects.requireNonNull(params);
        
        if (givenMillisPerTurn <= 0 || givenMillisInGame <= 0) {
            throw new IllegalArgumentException(
                    String.format("正の整数値が必要です。givenMillisPerTurn=%d, givenMillisInGame=%d",
                            givenMillisPerTurn, givenMillisInGame));
        }
        
        Map<String, String> copy = new HashMap<>(params);
        copy.put(KEY_PLAYER_BLACK, playerBlack.getName());
        copy.put(KEY_PLAYER_WHITE, playerWhite.getName());
        copy.put(KEY_MILLIS_PER_TURN, String.valueOf(givenMillisPerTurn));
        copy.put(KEY_MILLIS_IN_GAME, String.valueOf(givenMillisInGame));
        
        return new GameCondition(
                playerBlack,
                playerWhite,
                givenMillisPerTurn,
                givenMillisInGame,
                copy);
    }
    
    /**
     * パラメータを一括指定してゲーム実施条件を生成します。<br>
     * {@code params} は以下の必須パラメータを含む必要があります。<br>
     * <table border="1">
     *   <caption>必須パラメータ</caption>
     *   <tr><th>パラメータ名</th><th>内容</th><th>例</th></tr>
     *   <tr><td>{@code player.black}</td><td>黒プレーヤーの完全修飾クラス名</td><td>{@code xyz.hotchpotch.reversi.aiplayers.SimplestAIPlayer}</td></tr>
     *   <tr><td>{@code player.white}</td><td>白プレーヤーの完全修飾クラス名</td><td>{@code xyz.hotchpotch.reversi.aiplayers.RandomAIPlayer}</td></tr>
     *   <tr><td>{@code givenMillisPerTurn}</td><td>一手あたりの制限時間（ミリ秒）</td><td>{@code 1000}</td></tr>
     *   <tr><td>{@code givenMillisInGame}</td><td>ゲーム全体での持ち時間（ミリ秒）</td><td>{@code 15000}</td></tr>
     * </table>
     * 
     * @param params パラメータが格納された {@code Map}
     * @return ゲーム実施条件
     * @throws NullPointerException {@code params} が {@code null} の場合
     * @throws IllegalArgumentException 必須パラメータが設定されていない場合やパラメータ値が不正な場合
     */
    public static GameCondition of(Map<String, String> params) {
        Objects.requireNonNull(params);
        
        Map<String, String> copy = new HashMap<>(params);
        
        Class<? extends Player> playerBlack = ConditionUtil.getPlayerClass(copy, KEY_PLAYER_BLACK);
        Class<? extends Player> playerWhite = ConditionUtil.getPlayerClass(copy, KEY_PLAYER_WHITE);
        long givenMillisPerTurn = ConditionUtil.getLongPositiveValue(copy, KEY_MILLIS_PER_TURN);
        long givenMillisInGame = ConditionUtil.getLongPositiveValue(copy, KEY_MILLIS_IN_GAME);
        
        return new GameCondition(
                playerBlack,
                playerWhite,
                givenMillisPerTurn,
                givenMillisInGame,
                copy);
    }
    
    // [instance members] ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    // 不変なメンバ変数は直接公開してしまう。
    // http://www.ibm.com/developerworks/jp/java/library/j-ft4/
    
    /** プレーヤークラスが格納された {@code Map} */
    public transient final Map<Color, Class<? extends Player>> playerClasses;
    
    /** 一手あたりの制限時間（ミリ秒） */
    public transient final long givenMillisPerTurn;
    
    /** ゲーム全体での持ち時間（ミリ秒） */
    public transient final long givenMillisInGame;
    
    private transient final Map<String, String> params;
    
    private GameCondition(
            Class<? extends Player> playerClassBlack,
            Class<? extends Player> playerClassWhite,
            long givenMillisPerTurn,
            long givenMillisInGame,
            Map<String, String> params) {
            
        assert playerClassBlack != null;
        assert playerClassWhite != null;
        assert 0 < givenMillisPerTurn;
        assert 0 < givenMillisInGame;
        assert params != null;
        
        Map<Color, Class<? extends Player>> playerClasses = new EnumMap<>(Color.class);
        playerClasses.put(Color.BLACK, playerClassBlack);
        playerClasses.put(Color.WHITE, playerClassWhite);
        this.playerClasses = Collections.unmodifiableMap(playerClasses);
        this.givenMillisPerTurn = givenMillisPerTurn;
        this.givenMillisInGame = givenMillisInGame;
        this.params = Collections.unmodifiableMap(params);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getParams() {
        // Collections#unmodifiableMap でラップしているので直接返して問題ない
        return params;
    }
    
    /**
     * この {@code GameCondition} オブジェクトの代わりに、{@link SerializationProxy GameCondition.SerializationProxy} オブジェクトを直列化します。<br>
     * 
     * @return この {@code GameCondition} オブジェクトの代理となる {@link SerializationProxy GameCondition.SerializationProxy} オブジェクト
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    /**
     * {@code GameCondition} オブジェクトを直接復元することはできません。<br>
     * {@code GameCondition} オブジェクトの復元は {@link SerializationProxy GameCondition.SerializationProxy} を通して行う必要があります。<br>
     * 
     * @serialData 例外をスローして復元を中止します。
     * @param stream オブジェクト入力ストリーム
     * @throws InvalidObjectException 直接 {@code GameCondition} オブジェクトの復元が試みられた場合
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
