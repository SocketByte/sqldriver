# SqlDriver
Small all-in-one ORM solution with built-in HikariCP as a connection pool.
It has a simple referencing system and it's based mainly on annotations.
It has built-in FST serialization for saving unsaveable field data to Base64 (as string)

Keep in mind that it is not finished.

## Installation
Please use Gradle or Maven.
```gradle
repositories {
    maven {
        url "https://repo.socketbyte.pl/nexus/content/repositories/releases/"
    }
}

dependencies {
    compile group: 'pl.socketbyte', name: 'sqldriver', version: '1.0.5'
}
```

## Usage
```java
public class HelloWorldSqlDriver {
    
    // This annotation is required (for now I guess)
    @SqlObject(tableName="clans")
    class Clan {
        public String tag;
        public String name;
        
        // Makes the field unreadable for SqlDriver ORM
        @SqlTransient
        // Makes a simple reference.
        // The SqlDriver puts all loaded User objects matching the rules
        // provided below onto the List<User> members; field below.
        @SqlReference(reference = User.class, rule = {"clanTag=tag", "membershipStatus=4"})
        public List<User> members;
    }
    
    @SqlObject(tableName="users")
    class User {
        // This annotation automatically generates
        // WHERE conditions from fields annotated with it
        // Used for delete(), update() operations
        // Mostly these things never change and are
        // treated like static ids/primary keys
        @SqlPrimary
        public UUID uniqueId;
        
        // You do not need @SqlField (its optional)
        // SqlDriver automatically detects the type and name
        public String clanTag;
        
        // Makes the field nullable (doesnt get NOT NULL flag in SQL)
        @SqlNullable
        public String nullableString;
        
        // Custom settings for a SQL record (like type and name)
        @SqlField(name = "membership", type = SqlDataType.TINYINT)
        public int membershipStatus = 2;
        
        @SqlTransient
        @SqlReference(reference = Clan.class, rule = "tag=clanTag")
        public Clan clan;
        
        @SqlTransient
        public Object dontSaveMe;
    }
    
    public static void main(String[] args) {
        SqlDriver driver = SqlDriver.create();
        driver.useFastReflections(); // optional to use reflectasm
        driver.register(Clan.class);
        driver.register(User.class); // optional but makes it faster
        
        try (SqlConnection connection = driver.borrow()) {
            // you can also create normal preparedstatements
            connection.createStatement(query);
            
            // creating tables
            connection.createTable(Clan.class);
            connection.createTable(User.class);
            
            User user = new User();
            user.uniqueId = UUID.randomUUID();
            user.clanTag = "TAG";
            
            Clan clan = new Clan();
            clan.name = "SomeClan";
            clan.tag = "TAG";
            
            // inserting SqlObjects
            connection.insert(user);
            connection.insert(clan);
            
            // selecting objects from the database
            // keep in mind, to make referencing work you need
            // to load all classes (which use @SqlReference) at the exact same time!
            Map<Class<?>, SelectionResult> map =
                    connection.selectAll(Clan.class, User.class);
            
            SelectionResult<Clan> clanSelection = map.get(Clan.class);
            SelectionResult<User> userSelection = map.get(User.class);
            
            // your objects
            List<Clan> clans = clanSelection.getObjects();
            List<User> users = userSelection.getObjects();
            
            // etc
        }
    }
    
}
```
*SqlDriver* uses properties file to connect to MySQL/SQLite/whatever.
The default path is `db.properties`, but you can change it like this:
`SqlDriver.create("your path to properties file");`

This is an example of properties file:
```properties
dataSourceClassName=org.mariadb.jdbc.MariaDbDataSource
dataSource.serverName=localhost
dataSource.portNumber=3306
dataSource.user=root
dataSource.password=
dataSource.databaseName=database
```
More about HikariCP's properties file here: https://github.com/brettwooldridge/HikariCP

More detailed documentation coming soon (when it's finished)

## Performance
It has an overhead of approx. 5-10% comparing to a standard usage. (raw PreparedStatements)

Selection alghoritms will take a bit longer due to referencing system. It's not much though, but expect 
less performance compared to a clean, non-ORM usage, which is of course still possible with this API.