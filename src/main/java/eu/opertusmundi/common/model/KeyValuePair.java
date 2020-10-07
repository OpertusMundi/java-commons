package eu.opertusmundi.common.model;

public class KeyValuePair<K, V> {

    private K key;

    private V value;

    public K getKey() {
        return this.key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return this.value;
    }

    public void setValue(V value) {
        this.value = value;
    }

}
