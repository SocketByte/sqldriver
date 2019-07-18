package pl.socketbyte.sqldriver.test;

import pl.socketbyte.sqldriver.orm.annotation.*;
import pl.socketbyte.sqldriver.query.SqlDataType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SqlObject(tableName = "clans")
public class Clan {

    public String tag;
    public String name;

    @SqlTransient
    @SqlReference(reference = User.class, rule = {"clanTag=tag", "membershipStatus=4"})
    public List<User> members;

    public int x;
    public int y;
    public int z;

    @Override
    public String toString() {
        return "Clan{" +
                "tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", members=" + Arrays.toString(members.toArray()) +
                '}';
    }
}

