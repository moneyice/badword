package wordfilter.dfa;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @auth bruce-sha
 * @date 2015/6/23
 */
public class Sensitives {
    private Logger logger = LoggerFactory.getLogger(getClass());
    final Path dicHome;

    Map<String, Node> dics = new ConcurrentHashMap<>();
//    Node root;

    private Sensitives(String dicHome) {
        this.dicHome = Paths.get(Objects.requireNonNull(dicHome));
//        this.root = new Node(Node.NONE, (char) 0);
    }

    public static Sensitives singleton(String dicHome) {
        final Sensitives sens = new Sensitives(dicHome);
        sens.builds();
        new Thread(() -> sens.watchDog()).start();
        return sens;
    }

    public Sensitives builds() {
        try {
            Files.list(dicHome).parallel().filter(p -> p.toString().endsWith(".dic")).forEach(p -> {
                try {
                    Node root = new Node(Node.NONE, (char) 0);
                    build(root, p);
                    dics.put(p.getName(p.getNameCount() - 1).toString(), root);
                } catch (IOException e) {
                    logger.error("build sensitive tree error：", e);
                }
            });
        } catch (IOException e) {
            logger.error("build sensitive tree error：", e);
        }
        return this;
    }

    private Node build(Node root, Path path) throws IOException {
        final AtomicInteger counter = new AtomicInteger();
        Files.lines(path, StandardCharsets.UTF_8)
                .parallel()
                .map(line -> line.trim())
                .filter(line -> !line.isEmpty())
                .peek(e -> counter.incrementAndGet())
                .forEach(line -> buildTree(root, line, 0));

        logger.info(counter.get() + " words loaded from dictionary " + path.getName(path.getNameCount() - 1));

        return root;
    }

    public void rebuilds() {
        throw new NotImplementedException();
    }

    public void addOne(String newWord) {
        Objects.requireNonNull(newWord);
//        buildTree(root, newWord, 0);

        throw new NotImplementedException();
    }

    public void removeOne(String word) {
        Objects.requireNonNull(word);

        throw new NotImplementedException();
    }

    private void buildTree(Node parentNode, String line, int index) {
        if (outRange(line, index)) return;

        char cha = uniChar(line, index);

        final Map<Character, Node> children = parentNode.children();
        if (!children.containsKey(cha)) {
            Node child = new Node(parentNode, cha);
            children.putIfAbsent(cha, child);
        }

        final Node childNode = children.get(cha);
        childNode.setEnd(childNode.isEnd() || (line.length() == index + 1));
        buildTree(childNode, line, ++index);
    }

    private boolean outRange(String str, int index) {
        return str == null ? true : index > str.length() - 1;
    }

    private char uniChar(String str, int index) {
        return Character.toLowerCase(str.charAt(index));
    }

    /**
     * 判定模式：性能较好，同时规则非常严格
     */
    public Set<String> judge(final String level, final String content) {
        Objects.requireNonNull(content);
        Collection<Node> dics = dics(level);
        return IntStream.range(0, content.length())
                .parallel()
//                .mapToObj(index -> doJudge(root, content, index))
                .mapToObj(index -> doJudges(dics, content, index))
                .flatMap(es -> es.parallelStream())
                .filter(node -> node != Node.NONE)
                .map(node -> node.sensitive())
                .collect(Collectors.toSet());
    }

    private Collection<Node> dics(final String level) {
        final String dic = System.getProperty("wordfilter.level." + level.toLowerCase());
        if (dic == null) return dics.values();
        return Stream.of(dic.split(",")).map(e -> dics.get(e)).collect(Collectors.toList());
    }

    private List<Node> doJudges(Collection<Node> parentNodes, String content, int index) {
        return parentNodes.stream().parallel().map(e -> doJudge(e, content, index)).collect(Collectors.toList());
    }

    private Node doJudge(Node parentNode, String content, int index) {
        if (parentNode.isEnd()) return parentNode;
        if (outRange(content, index)) return Node.NONE;

        char cha = uniChar(content, index);

        final Map<Character, Node> children = parentNode.children();
        if (!children.containsKey(cha)) {
            if (!isChinese(cha)) return doJudge(parentNode, content, ++index);
            else return Node.NONE;
        }

        final Node childNode = children.get(cha);
        return doJudge(childNode, content, ++index);
    }

    /**
     * 马赛克模式：性能一般，规则比较松
     */
    public Optional<String> mosaic(final String level, final String content) {
        Objects.requireNonNull(content);
        Collection<Node> dics = dics(level);
        final char[] chas = content.toCharArray();
        boolean[] isLegal = {true};
        IntStream.range(0, content.length())
                .parallel()
                .mapToObj(index -> doMosaics(dics, content, index))
                .flatMap(ns -> ns.parallelStream())
                .filter(nodeMarks -> nodeMarks != NodeMarks.NONE)
                .peek(m -> isLegal[0] = false)
                .forEach(m -> IntStream.range(m.start, m.end).parallel().forEach(i -> chas[i] = '*'));
        if (isLegal[0]) return Optional.empty();
        return Optional.of(new String(chas));
    }

    private List<NodeMarks> doMosaics(Collection<Node> parentNodes, String content, int index) {
        return parentNodes.stream().parallel().map(e -> doMosaic(e, content, index, new NodeMarks(index))).collect(Collectors.toList());
    }

    private NodeMarks doMosaic(Node parentNode, String content, int index, NodeMarks nodeMarks) {
        if (parentNode.isEnd()) return nodeMarks.end(index);
        if (outRange(content, index)) return NodeMarks.NONE;

        char cha = uniChar(content, index);

        final Map<Character, Node> children = parentNode.children();
        if (!children.containsKey(cha)) return NodeMarks.NONE;

        final Node childNode = children.get(cha);
        return doMosaic(childNode, content, ++index, nodeMarks);
    }

    /**
     * 严格模式：<p>
     * P.c.\\\P气枪.网 -> pcp气枪网
     * 也会造成误判
     * are you ok ? i'm very ok. 种子  -> AV种子
     * 但是，如下不会
     * are you ok ? i'm very ok. 求种子
     * <p>
     * http://kernaling-wong.iteye.com/blog/2079091
     */
    private boolean isChinese(char a) {
        final int v = (int) a;
        return 19968 <= v && v <= 40869;
        // return (v >= 19968 && v <= 171941);
    }

    private void watchDog() {
        try {
            WatchService watcher = dicHome.getFileSystem().newWatchService();
            dicHome.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey watchKey = watcher.take();
                if (!watchKey.pollEvents().isEmpty()) {
                    builds();
                    logger.info("sensitive tree rebuild.");
                }
                if (!watchKey.reset()) break;
            }
        } catch (Exception e) {
            logger.error("watch error: ", e);
        }
    }

    static class NodeMarks {
        int start;
        int end;

        NodeMarks(int start) {
            this.start = start;
        }

        NodeMarks end(int end) {
            this.end = end;
            return this;
        }

        static final NodeMarks NONE = new NodeMarks(Integer.MIN_VALUE);
    }
}
