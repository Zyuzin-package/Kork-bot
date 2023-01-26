package org.example.domains;

public class KorkUser {

    String id;
    int count;

    public KorkUser(String id, int count) {
        this.id = id;
        this.count = count;
    }

    public KorkUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
