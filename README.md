
<img width="1909" height="824" alt="VenturaClassroom" src="https://github.com/user-attachments/assets/df65d2d8-aeca-409f-a8c4-deca11b482bc" />

# VenturaClassroom

Run real lessons on your server. Schedule classes by real-world day and time (or on a repeating timer), let students join with one click, then teleport everyone in, teach, and grade and dismiss them — with optional money and XP rewards. Comes with a month calendar, a click-through setup wizard, and a clean in-game help menu.

---

## Requirements

- **Spigot or Paper 1.21 or newer**
- **Java 21**

Optional, only if you want the extra features:

- **[Vault](https://www.spigotmc.org/resources/vault.34315/)** + any economy plugin — for money rewards on grades
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** — for `%venturaclasses_...%` placeholders

VenturaClassroom runs perfectly fine without either — those features just stay switched off.

---

## Installation

1. Drop `VenturaClassroom.jar` into your `plugins` folder.
2. (Optional) Install Vault + an economy plugin and/or PlaceholderAPI.
3. Restart the server.

---

## Getting started

The fastest way is the guided wizard:

1. Run `/class setup`
2. Type the class name in chat (spaces are fine, e.g. `Maths 101`)
3. Click a player to be the teacher
4. Stand where students should arrive and click **[Set Warp Here]**
5. Click the day(s) and a time, then a duration
6. Done — it gives you **[Start now] [Info] [Calendar]** buttons

Prefer to do it by hand? It's just as quick:

1. `/class create Maths 101` — creates the class (it's given a number as its id)
2. `/class setteacher 1 Steve` — assign the teacher
3. `/class setwarp 1` — set where students arrive (stand there first)
4. `/class settime 1 weekdays 9am` — schedule it

You can refer to a class by its **id** (`1`) or its **name** (`Maths 101`) in any command.

---

## Running a lesson

Classes start on their own at the scheduled time, or a teacher can start one manually. There are two steps so students have a moment to join:

1. `/class start 1` — announces the class and opens it for joining. Everyone online gets a clickable **[Join]** message.
2. Students click to join (or run `/class join 1`).
3. `/class begin 1` — teleports everyone who joined to the warp and locks the class so no one else wanders in.

When you're finished:

- `/class dismiss <player>` — opens a menu to pick a grade for one student; they get that grade's rewards and leave.
- `/class dismiss all 1` — dismisses everyone still in the class.

---

## Setting times

Classes use **real-world** time, not in-game time.

Set a day and a time:

```
/class settime 1 monday 9am
/class settime 1 weekdays 2:30pm
/class settime 1 weekend 14:00
```

Days can be `monday`-`sunday`, or `daily`, `weekdays`, `weekend`. Times can be written `9am`, `2:30pm`, `14:00`, `noon` or `midnight`.

Or run it on a **repeating timer** instead — every hour, every 30 minutes, every 90 seconds:

```
/class settime 1 every 1h
/class settime 1 every 30m
/class settime 1 every 90s
```

Set how long a lesson lasts and it'll end on its own:

```
/class setduration 1 1h
/class setduration 1 90m
```

---

## Grades and rewards

Grades are defined in `config.yml`. Each grade can hand out money, XP, a message, and run **any console commands** — so you can plug rewards into any other plugin:

```yaml
grades:
  A:
    money: 500.0
    xp: 30
    message: "&aExcellent work! You earned an A in %class%."
    commands:
      - "give %player% diamond 2"
```

Placeholders you can use anywhere in a grade: `%player%`, `%class%`, `%grade%`, `%money%`, `%xp%`.

Grade a student when you dismiss them with `/class dismiss <player>`, or pre-set grades during the lesson with `/class grade 1 Steve A` and apply them all at once with `/class dismiss all 1`.

---

## The calendar

`/class calendar` opens a month view. Today is highlighted green, and days with classes are highlighted — hover a day to see its classes, or click it to open that day and click a class for its details. Use the arrows to flip between months.

---

## Commands

You can use a class **id** or **name** anywhere `<name>` appears.

**Players**

| Command | What it does |
|---------|--------------|
| `/class help` | Open the help menu |
| `/class list` | List all classes |
| `/class info <name>` | Show a class's details |
| `/class calendar` | Open the month calendar |
| `/class next` | See which class is on next |
| `/class join <name>` | Join a class that's open |
| `/class leave <name>` | Leave a class |
| `/class submit` | Hand in the item you're holding |

**Teachers**

| Command | What it does |
|---------|--------------|
| `/class start <name>` | Announce the class and open joining |
| `/class begin <name>` | Teleport everyone in and lock the class |
| `/class end <name>` | End the class |
| `/class warp <name>` | Teleport yourself to the class location |
| `/class lock` / `unlock <name>` | Stop / allow new students joining |
| `/class capacity <name> <n>` | Set the student limit |
| `/class giveitem <name>` | Give the item in your hand to every student |
| `/class grade <name> <player> <grade>` | Pre-set a student's grade |
| `/class dismiss <player>` | Grade & dismiss one student via a menu |
| `/class dismiss all <name>` | Dismiss everyone left |
| `/class submissions <name>` | View what students handed in |

**Admins**

| Command | What it does |
|---------|--------------|
| `/class setup` | Guided step-by-step class setup |
| `/class create <name...>` | Create a class (name can have spaces) |
| `/class delete <name>` | Delete a class |
| `/class setname <name> <display...>` | Rename a class |
| `/class setteacher <name> <player>` | Assign the teacher |
| `/class addsub` / `removesub <name> <player>` | Add / remove an assistant teacher |
| `/class setwarp <name>` | Set the warp to where you stand |
| `/class settime <name> <day> <time>` | Add a day & time |
| `/class settime <name> every <interval>` | Run on a repeating timer (e.g. `every 1h`) |
| `/class deltime <name> <day> <time>` | Remove a day & time |
| `/class setduration <name> <length>` | Set how long a lesson runs (e.g. `1h`, `90m`) |
| `/class reload` | Reload `config.yml` |

The command also answers to `/classes`, `/classroom`, `/vclass` and `/vclassroom`.

---

## Good to know

Each class gets a **number** as its id, so you can have two classes with the same name (`Maths` id `1` and `Maths` id `2`). Refer to a class by its number or its name — if a name is shared by more than one class, the plugin will ask you to use the number.

Scheduling runs on the **server's real-world clock**, so a class set for "Monday 9am" starts at 9am server time every Monday. Repeating timers (`every 1h`) count from when the class was set or the server started, not from the top of the hour.

Money rewards need **Vault** and an economy plugin; without them, the money part of a grade is simply skipped and everything else still works.

Permissions: `venturaclasses.use` (join and view, default on), `venturaclasses.teacher` (run your own classes), `venturaclasses.admin` (create and configure).

---

<!-- Replace the line below with a screenshot of your own -->
<img width="700" alt="VenturaClassroom" src="REPLACE_WITH_YOUR_IMAGE_URL" />

Need Support, Assistance or Help?
Join the discord below or submit a report at my github!

https://discord.gg/rRAXRbaJxz OR https://github.com/Ash10744/VenturaClassroom/issues

---
