# ReducedDebugBackport

📖 [Русская версия (README_RU.md)](README_RU.md)

A small Spigot/Paper **1.16** plugin that hides invisible players from the
`visualize_entity_supporting_blocks` debug renderer that newer Minecraft clients
(1.21.x) have, when they connect to a legacy server through **ViaVersion**.

> ⚠️ **Important:** `reducedDebugInfo` is all-or-nothing. For every affected client it
> also hides **F3 coordinates**, **hitboxes (F3 + B)** and **chunk borders (F3 + G)** —
> not just the entity-supporting-blocks renderer. There is no way to disable only that
> one renderer. Limit who is affected with `min-protocol-version` and the
> `reduceddebugbackport.bypass` permission.

## The problem

Since 1.21.x the vanilla client exposes its internal debug renderers in a UI
(`F3 + F6` → *Debug Options* → *Debug Renderers*). One of them,
`visualize_entity_supporting_blocks`, can be set to **Always** and draws a marker
on the block under every entity — **including players hidden by invisibility**.
It renders from data the client already has, so the server can't simply "turn the
button off".

The vanilla lever against it is the **`reducedDebugInfo`** flag
(gamerule *"Limits contents of debug screen"*). When active, the client disables
these debug renderers. The flag is delivered to the client in the login packet and
toggled at runtime via the **Entity Status / Entity Event** packet (status `22` =
enable, `23` = disable) targeting the player's own entity.

## Compatibility

Works on Spigot/Paper servers **1.16 and newer**.

The `reducedDebugInfo` game rule has existed since **1.8**, so on any server this
plugin supports you can instead simply run:

```
/gamerule reducedDebugInfo true
```

for the same effect **server-wide**. Use this plugin only when you want
**per-player control** — e.g. hide F3 only for the newer ViaVersion clients that
actually have the `visualize_entity_supporting_blocks` renderer, while leaving
native players' F3 (coordinates, hitboxes) untouched. The
`reduceddebugbackport.bypass` permission gives you that granularity; the plain
game rule does not.

## Requirements

- A **1.16** Spigot/Paper server.
- **ViaVersion** (used to detect the real client protocol version).
- **PacketEvents** *or* **ProtocolLib** (for sending the packet).

All three are soft dependencies; the plugin disables itself with a clear log
message if no packet backend is found, and warns if ViaVersion is missing.

## Installation

1. Drop `ReducedDebugBackport-1.0.jar` into `plugins/` next to ViaVersion and
   PacketEvents (or ProtocolLib).
2. Start the server once to generate `plugins/ReducedDebugBackport/config.yml`.
3. Adjust `min-protocol-version` if needed and restart.

## Configuration

`config.yml`:

```yaml
# Client protocol version FROM which to start sending reducedDebugInfo.
# Clients BELOW this value are left untouched (they keep F3 coordinates/hitboxes).
min-protocol-version: 774   # 774 = 1.21.11

# Log every flag send to the console.
log-actions: false

# Verbose log: for EACH player prints the detected protocol version and the
# decision (SKIP / SENT / BYPASS).
debug: false
```

Common protocol numbers:

| Protocol | Version | Protocol | Version |
|---|---|---|---|
| 754 | 1.16.5 | 767 | 1.21 / 1.21.1 |
| 763 | 1.20 / 1.20.1 | 768 | 1.21.2 / 1.21.3 |
| 764 | 1.20.2 | 769 | 1.21.4 |
| 765 | 1.20.3 / 1.20.4 | 770 | 1.21.5 |
| 766 | 1.20.5 / 1.20.6 | 772 | 1.21.7 / 1.21.8 |
| 773 | 1.21.9 | 774 | 1.21.11 |


## Command

`/rdb [off]` — operators only. Diagnostic helper:

- Prints the detected protocol version, the threshold, the bypass state and the
  "normal decision" (SEND / SKIP).
- Force-sends the packet **ignoring the threshold**: `/rdb` sends status `22`
  (enable), `/rdb off` sends status `23` (disable). Useful to confirm the packet
  itself works regardless of version detection.

## Permission

| Permission | Default | Effect |
|---|---|---|
| `reduceddebugbackport.bypass` | `false` | Player is ignored — `reducedDebugInfo` is not sent, full F3 is kept. |

The bypass is checked at apply time (join / respawn / world change), so changing it
for an online player takes effect after their next respawn or relog.
