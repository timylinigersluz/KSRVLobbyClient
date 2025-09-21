# KSRLobbyClient

KSRLobbyClient ist das **Paper-Plugin**, das gemeinsam mit dem Velocity-Plugin  
[KSRVLobby](https://github.com/deinuser/KSRVLobby) verwendet wird.

Es sorgt dafür, dass der Proxy immer die aktuelle Welt eines Spielers kennt,  
um bei `/hub` oder `/lobby` Befehlen prüfen zu können, ob ein Teleport erlaubt ist.

---

## Features
- Registriert den PluginMessage-Channel `ksr:lobby`
- Sendet **WORLD_UPDATE** Nachrichten an den Proxy:
    - beim **Join**
    - beim **Wechsel der Welt**
- Enthält einen Testbefehl `/echotest`, um die Proxy-Kommunikation zu prüfen
- Empfängt Antworten vom Proxy (z. B. `ECHO_REPLY`)
- Einfaches Logging zur Fehleranalyse

---

## Installation
1. **Build**:
   ```bash
   mvn package
   ```
   → erzeugt `KSRLobbyClient-x.y.z.jar` im `target/`-Ordner.

2. **Deploy**:
    - Kopiere die JAR in den `plugins/` Ordner jedes Paper-Servers im Netzwerk.
    - Starte die Server neu.

---

## Commands & Permissions

### `/echotest`
- Sendet eine `ECHO_START` Nachricht an den Proxy.
- Proxy antwortet mit `ECHO_REPLY`, das als Chat-Nachricht angezeigt wird.
- Permission:
  ```
  ksrlobbyclient.echotest
  ```

---

## Kommunikation mit Proxy
- **Client → Proxy:**
    - `WORLD_UPDATE` → Spieler-UUID + aktuelle Welt
    - `ECHO_START` → zum Testen

- **Proxy → Client:**
    - `ECHO_REPLY` → Antwort auf den Echo-Test

---

## Beispiel-Logs

### Beim Weltwechsel
```
[KSRLobbyClient] WORLD_UPDATE Trigger: WORLD_CHANGE -> helomen@1vs1
[KSRLobbyClient] Sending WORLD_UPDATE: helomen uuid=ce621eff-a17f-... world=1vs1 bytes=64
```

### Echo-Test
```
[KSRLobbyClient] Welt gesendet: helomen -> ECHO_START
[KSRLobbyClient] Echo-Test von helomen ausgeführt.
[KSRLobbyClient] Echo-Reply empfangen für helomen: Hallo helomen, Echo erfolgreich empfangen!
```

---

## Debug
Das Plugin loggt standardmäßig alle wichtigen Vorgänge in die Paper-Konsole.  
Zusätzlich kann man auf Proxy-Seite (`KSRVLobby`) den Debug-Modus aktivieren, um  
die gesamte Kommunikation Schritt für Schritt nachzuverfolgen.

---

## Lizenz
Dieses Projekt wurde für das KSR Minecraft Netzwerk entwickelt.  
Verwendung und Anpassungen sind erlaubt, solange die ursprüngliche Herkunft (`ksrminecraft`) erkennbar bleibt.
