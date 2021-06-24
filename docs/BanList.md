# Banning players by name or IP address.
```java
Crimson.getServer().getBanList(BanListType.IP).addBan(null, "127.0.0.1", "Banned by an admin.", null);
```
Similarly with player names,

```java
Crimson.getServer().getBanList(BanListType.NAME).addBan("player1", null, "Banned by an admin.", null);
```
