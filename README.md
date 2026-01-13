# Manus Groups Pro  
### PlayLegend Application Task – Group & Permission System

A high-performance, database-driven group & authorization system for Minecraft servers, built as part of the PlayLegend application process.  
Designed with clean architecture, async I/O, and scalability in mind.

---

## 📌 Project Goal

Create a plugin similar to **LuckPerms / PermissionsEx** that supports:

- Group management  
- Player group assignment (temporary & permanent)  
- Prefix display in chat & join messages  
- Database persistence  
- Customizable messages  
- Sign-based player info display  
- Permission handling (including `*` wildcard)

---

## ✨ Features

- 🧱 **Group System**
  - Create, edit, and delete groups in-game  
  - Custom group prefixes  
- 👤 **Player Assignments**
  - Permanent & time-limited group assignments  
  - Supports complex durations (e.g. `4d 7m 23s`)  
- 💬 **Chat Integration**
  - Prefix shown in chat and on join  
- 🔄 **Live Updates**
  - Group changes apply instantly (no relog required)  
- 🪧 **Sign Support**
  - Display player name & rank on signs  
- 🗄️ **Relational Database Storage**
  - All player & group data stored via JDBC  
- ⚙️ **Customizable Messages**
  - Fully configurable language system  
- 🔐 **Permission System**
  - Group-based permissions  
  - Wildcard (`*`) support  
  - Works with Bukkit’s `hasPermission()`  

---

## 🧠 Architecture & Design

- **Async I/O** for database operations  
- **HikariCP** connection pooling  
- Cache layers for:
  - Groups  
  - Player assignments  
  - Prefixes  
  - Permissions  
- Modular command system  
- Clean separation of:
  - Commands  
  - Services  
  - Caches  
  - Database access  


---

## 🛠 Tech Stack

- Java 21  
- Spigot / Paper API  
- MariaDB / MySQL  
- HikariCP  
- Maven  

---

## 📸 Screenshots (Placeholders)

## language.yml
<img width="1431" height="1255" alt="39516c49-3b97-40f1-8d1d-36d519761a9f" src="https://github.com/user-attachments/assets/cd4f6c6f-3677-4f61-b113-d6218193f8ba" />

## Tab list
![faa8195e-a239-49d7-bfd6-8f067cad0ab2](https://github.com/user-attachments/assets/3dfdb13d-532a-4a0a-b561-00bc94220c78)

## Dynamic Signs
<img width="2560" height="1440" alt="2026-01-13_22 12 56" src="https://github.com/user-attachments/assets/80767614-8b73-44c9-a298-5c5bbc286ed5" />
<img width="2560" height="1440" alt="2026-01-13_22 12 37" src="https://github.com/user-attachments/assets/b56e559d-f24c-4242-a2ac-6c303f059fde" />


---

## 🚀 Why This Project Matters

This plugin demonstrates:

- Backend architecture skills  
- Async programming  
- Database design  
- Performance-oriented Minecraft development  
- Clean, maintainable code  
- Real-world system design  

---

## 📄 License

Private / Application Task  
All rights reserved by Manuel Franz Gogg.

