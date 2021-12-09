<div align="center">
    <img width="200px" src="wormicon.png" />
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
```java
User user = new User(); //User is a class that extends WormTable
user.Get(2);
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
        <url>https://repo.gump.dev/snapshots/</url>
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
        <version>1.0-SNAPSHOT</version>
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
        url = 'https://repo.gump.dev/snapshots/'
    }
}
```

Then, add Worm's dependency too:
```
    dependencies {
        ...
        compile 'dev.gump:worm:1.0-SNAPSHOT'
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
}
```

Then create a class that extends WormTable:
```java
  public class User extends WormTable {
    public String User_id, Name, Email;

    static List<WormColumn> columns = Arrays.asList(
      new WormColumn("User_id", "VARCHAR(36)", true), //First param is column name, secound is mysql creation and last is if the column is id or not
      new WormColumn("Name", "VARCHAR(36)"),
      new WormColumn("Email", "VARCHAR(36")
    );

    public User() {
      super(columns); //use this for declare the class columns in mysql
    }
  }
```

Now you can use:
```java
  User user = new User();
  user.Get("87bf4ec7-9051-47a1-b9f4-eea9b9ed8959")
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
