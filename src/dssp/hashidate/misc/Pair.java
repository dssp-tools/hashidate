package dssp.hashidate.misc;

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

        int pos = text.indexOf(SEPARATOR);

        String name = (0 > pos ? text : text.substring(0, pos));
        String val = (0 > pos ? null : text.substring(pos + 1));
        return new Pair(name.trim(), val);
    }
}
