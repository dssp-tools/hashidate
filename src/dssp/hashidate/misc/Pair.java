package dssp.hashidate.misc;

import java.util.Objects;

/*
 * 名前とオブジェクトの組
 */
public class Pair {
    private String name;
    private Object value;
    public static final String SEPARATOR = "=";

    public Pair(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        return String.format("%s%s%s", this.name.toString(), SEPARATOR,
                (null == this.value ? "" : this.value.toString()));
    }

    public static Pair fromString(String text) {
        if (null == text) {
            return null;
        }
        String name = null;
        StringBuilder buf = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Objects.isNull(name) && SEPARATOR.equals(String.valueOf(c))) {
                name = buf.toString();
                buf.delete(0, buf.length());
                continue;
            }
            buf.append(c);
        }
        return new Pair(name.trim(), buf.toString());
    }
}
