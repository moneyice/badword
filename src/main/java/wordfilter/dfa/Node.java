package wordfilter.dfa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auth bruce-sha
 * @date 2015/6/23
 */
public class Node {
    private final char word;
    private final Node parent;
    private final Map<Character, Node> children;

    private boolean isEnd;
    // private String sensitive;

    //关键词包含非中文字符
    // private boolean isChildrenContainsChinese;

    public Node(final Node parent, final char word) {
        this.parent = parent;
        this.word = word;
        this.children = new ConcurrentHashMap<>(1);
        this.isEnd = false;
    }

    public char word() {
        return word;
    }

    public Node parent() {
        return parent;
    }

    public Map<Character, Node> children() {
        return children;
    }


    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public String sensitive() {
        if (parent == NONE) return "";
        else return parent.sensitive() + word;
    }

    @Override
    public String toString() {
//        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
        return word + "(" + isEnd + ")-> {" +
                children.values().stream()
                        .map(e -> e.toString())
                        .reduce("", (x, y) -> x + "," + y)
                + "}";
    }

    public static final Node NONE = new Node(null, (char) 0);
}
