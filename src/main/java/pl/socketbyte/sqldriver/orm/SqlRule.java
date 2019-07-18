package pl.socketbyte.sqldriver.orm;

public class SqlRule {

    private SqlRuleBehaviour behaviour;

    public SqlRule(SqlRuleBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    public SqlRuleBehaviour getBehaviour() {
        return this.behaviour;
    }

    interface SqlRuleBehaviour {
        void rule();
    }
}
