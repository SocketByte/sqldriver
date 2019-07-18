package pl.socketbyte.sqldriver;

import java.util.List;

public class SelectionResult<T> {
    private String tableName;
    private List<T> result;

    public SelectionResult(String tableName, List<T> result) {
        this.tableName = tableName;
        this.result = result;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<T> getObjects() {
        return result;
    }
}
