# UltimateLinks

UltimateLinks is a lightweight Minecraft plugin that allows server admins and players to create, manage, and share custom links in-game. It is designed for simplicity, flexibility, and multilingual support.

**Tested on Minecraft 1.21.X, but likely compatible with other versions.**

## Features

- Add, remove, and reload links directly in-game
- Customizable prefixes for each link
- Supports tab completion for commands
- Multilingual messages automatically detected from the player's client language
- Permissions support:
  - `ultimatelinks.use` – allows players to use link commands
  - `ultimatelinks.op` – allows admins to manage links
- Short command alias `/ul` for faster access

## Supported Languages

Currently supported languages (automatic detection):
- English (US) – `en_us.json` (default)
- German – `de_de.json`

You can easily add more languages by creating a new JSON file in the `lang/` folder following the same structure.

## Usage

### Commands
| Command | Description |
|---------|-------------|
| `/ultimatelinks reload` or `/ul reload` | Reload the plugin configuration and links |
| `/ultimatelinks add <name> <link> [prefix]` | Add a new link. Optional prefix sets the chat message before the link |
| `/ultimatelinks remove <name>` | Remove an existing link |
| `/[linkname]` | Send the link in chat (e.g., `/dc` for Discord) |

### Config Example
```yaml
prefix:
  enabled: true        # Enable or disable prefixes globally
  default: "&aLink: &f" # Default prefix for all links

# Link commands
dc: "https://discord.gg/deinserver"      # Command name "dc" sends this link
dc-prefix: "&bJoin our Discord: &f"      # Optional: custom prefix for this command
vote: "https://example.com/vote"
vote-prefix: "&6Vote here: &f"
