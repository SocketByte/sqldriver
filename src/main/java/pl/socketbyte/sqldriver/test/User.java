package pl.socketbyte.sqldriver.test;

import pl.socketbyte.sqldriver.orm.annotation.*;
import pl.socketbyte.sqldriver.query.SqlDataType;

import java.util.UUID;

@SqlObject(tableName = "users")
public class User {

    public UUID uniqueId;
    public String clanTag;

    @SqlNullable
    public String nullableString;

    @SqlField(name = "membership", type = SqlDataType.TINYINT)
    public int membershipStatus = 2;

    @SqlTransient
    @SqlReference(reference = Clan.class, rule = "tag=clanTag")
    public Clan clan;

    @SqlTransient
    public Object dontSaveMe;

    @Override
    public String toString() {
        return "User{" +
                "uniqueId=" + uniqueId +
                ", clanTag='" + clanTag + '\'' +
                ", clan=" + (clan == null ? "null" : clan.tag) +
                '}';
    }
}
