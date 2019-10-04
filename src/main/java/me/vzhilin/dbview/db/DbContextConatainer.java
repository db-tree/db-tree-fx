package me.vzhilin.dbview.db;

public class DbContextConatainer {
    private DbContext context;

    public void setContext(DbContext context) {
        this.context = context;
    }

    public DbContext getContext() {
        return context;
    }
}
