<div align="center">
    <img width="200px" src="worm.png" />
    <h1>Worm</h1>
    <p>It's a Java ORM for MySQL</p>
</div>

<br>

## Topics
- [🪱 What is Worm?](#whatismars)
- [🔧 Installation](#installation)
- [📝 Getting Started](#getting-started)
- [🤔 FAQ](#faq)
- [🙏 Thanks](#thanks)

<br>
<a id="whatismars"></a>

## 🪱 What is Worm?

Worm is a Java ORM for Mysql, you can make simple query using:


User.java:
```java
public class User extends Entity {
    @Field(unique = true, autoGenerate = true)
    public UUID userId;
    @Field
    public String name;
}
```


Main.java:
```java
User user = Worm.of(User.class).findUnique("07e09b83-83bb-413c-bfb5-d64e8742f8f0");
```


<br>
<a id="installation"></a>

## 🔧 Installation

### Installation with maven
First, add Worm's repository into ```pom.xml```:
```xml
<repositories>
    ...
    <repository>
        <id>worm</id>
        <url>https://repo.gump.dev/releases/</url>
    </repository>
</repositories>
```

Then, add Worm's dependency too:
```xml
<dependencies>
    ...
    <dependency>
        <groupId>dev.gump</groupId>
        <artifactId>worm</artifactId>
        <version>1.1.2</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

<br>

### Installation with Gradle
First, add Worm's repository into ```build.gradle```:
```
repositories {
    ...
    mavenCentral()
    maven {
        name = 'worm'
        url = 'https://repo.gump.dev/releases/'
    }
}
```

Then, add Worm's dependency too:
```
    dependencies {
        ...
        compile 'dev.gump:worm:1.1.2'
    }
```
(i don't know if this is right, i don't use Gradle :P)

<br>
<a id="getting-started"></a>

## 📝 Getting Started

To get started we need to start Worm and declare your connection, use in main class:
```java
public static void main(String[] args) {
    WormConnection connection = new WormConnection(host, port, database, user, password);
    WormConnector.init(connection);
}
```

Then create a class that extends Entity:
```java
public class User extends Entity {
    @Field(unique=true, autoGenerate = true)
    public UUID userId; 
    @Field
    public String name;
    @Field(length=50)
    public String email;
}
```

Now we register the table in Worm:
```java
public static void main(String[] args) {
    Worm.getRegistry().registerTable(User.class); // needs be before Worm.init()
        
    WormConnection connection = new WormConnection(host, port, database, user, password);
    Worm.init(connection);
}
```

Now you can use:
```java
  User user = Worm.of(User.class).findUnique("87bf4ec7-9051-47a1-b9f4-eea9b9ed8959");
```

If you wanna learn more [Click here to see documentation](https://github.com/GumpDev/worm/wiki) 

<br>
<a id="faq"></a>

## 🤔 FAQ

- **Why you created that?** *Cuz is so boring to create SQL querys and treat on Java, and I'm lazy*
- **I Found a BUG!** *[Click here](https://github.com/GumpDev/worm/issues) and open an issue*
- **Can I help with the project?** *Sure! just send your PR :D*
- **Can I contact you?** *Yep, send email to contact@gump.dev*

<br>
<a id="thanks"></a>

## 🙏 Thanks
Thanks to [HikariCP](https://mvnrepository.com/artifact/com.zaxxer/HikariCP), [Mysql Connector Java](https://mvnrepository.com/artifact/mysql/mysql-connector-java) and [slf4j](https://mvnrepository.com/artifact/org.slf4j)
